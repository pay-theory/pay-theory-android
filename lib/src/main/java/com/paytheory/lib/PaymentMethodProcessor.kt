package com.paytheory.lib

import android.annotation.SuppressLint
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.paytheory.lib.api.ApiService
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.reactors.ConnectionReactors
import com.paytheory.lib.reactors.MessageReactors
import com.paytheory.lib.websocket.WebsocketMessageHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Abstract class that serves as the base for processing different payment methods.
 * It handles communication with the Pay Theory platform, including websocket connections
 * and integrity checks.
 *
 * @param payable The payable client interface.
 * @param configuration The configuration for Pay Theory.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class PaymentMethodProcessor (
    open val payable: Payable,
    open val payTheoryData: HashMap<Any, Any>? = hashMapOf(),
    open val configuration : PayTheoryConfiguration,
    open val viewModel: PaymentViewModel
) : WebsocketMessageHandler {
    // We no longer initialize here, this is moved into the ptTokenApiCall
    var isWarm: Boolean = false
    var resetCounter = 0
    var ptResetCounter = 0
    var integrityTokenProvider: StandardIntegrityTokenProvider? = null

    val integrity by lazy {
        Timber.tag("DEBUG_PAYTHEORY").d("Integrity lazy initializer started. isWarm: %s", isWarm)
        // Check configuration here for safety, though triggering is conditional
        if (!configuration.isTestMode) {
            if (!isWarm) {
                // Call the function that needs configuration
                initializeAndPrefetchIntegrityToken()
                isWarm = true
                Timber.tag("DEBUG_PAYTHEORY").d("Integrity check done, isWarm set to true.")
            }
        } else {
            Timber.tag("DEBUG_PAYTHEORY").d("Skipping integrity initialization in test mode (lazy block).")
            isWarm = false // Define state for test mode
        }
        // Decide what ready state means here
        updatePayableReadyState(false)
        Timber.tag("DEBUG_PAYTHEORY").d("Integrity lazy initializer finished.")
        Unit // Return Unit as the value of the lazy property isn't the focus
    }
    val headerMap: MutableMap<String, String> by lazy {
        if (configuration.isTestMode) {
            mutableMapOf("Content-Type" to "application/json")
        } else {
            mutableMapOf("Content-Type" to "application/json", "X-API-Key" to configuration.apiKey)
        }
    }
    var publicKey: String? = null
    var sessionKey: String? = null
    var hostToken: String? = null
    private val disposables = CompositeDisposable()
    /**
     * Initializes and prefetches the Google Play Integrity token, storing the token provider for later use.
     */
    private fun initializeAndPrefetchIntegrityToken() {
        if (configuration.isTestMode != true) {
            val googleProjectNumber: Long = getGoogleProjectNumber()


            val context = payable.getContext()
                ?: throw IllegalStateException("Context is required for non-test environments")
            val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)

            // Prepare integrity token. Can be called once in a while to keep internal
            // state fresh.
            standardIntegrityManager.prepareIntegrityToken(
                PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(googleProjectNumber)
                    .build()
            )
                .addOnSuccessListener { tokenProvider ->
                    integrityTokenProvider = tokenProvider
                    ptTokenApiCall(payable)
                }
                .addOnFailureListener { exception ->
                    Logger.getLogger("warmUpPlayIntegrity").log(Level.WARNING,exception.message.toString())
                }
        }
//        if (!configuration.isTestMode) {
//            Timber.tag("DEBUG_PAYTHEORY").d("Initializing and prefetching integrity token.")
//            val standardIntegrityManager = IntegrityManagerFactory.createStandard(payable.getContext())
//            val googleProjectNumber = getGoogleProjectNumber()
//
//            val prepareIntegrityTokenTask = standardIntegrityManager.prepareIntegrityToken(
//                PrepareIntegrityTokenRequest.builder().setCloudProjectNumber(googleProjectNumber).build()
//            )
//
//            prepareIntegrityTokenTask.addOnSuccessListener { tokenProvider ->
//                Timber.tag("DEBUG_PAYTHEORY").d("Integrity token preparation successful.")
//                integrityTokenProvider = tokenProvider // Store the provider
//            }.addOnFailureListener { exception ->
//                Timber.tag("DEBUG_PAYTHEORY").e(exception, "Integrity token preparation failed.")
//                Logger.getLogger("warmUpPlayIntegrity").log(
//                    Level.WARNING,
//                    exception.message.toString()
//                )
//                handleApiError(exception,payable)
//            }
//        }
    }

    /**
     * Companion object to hold shared properties and constants.
     */
    companion object {

        /**
         * Flag indicating if the session is dirty and needs a new session key.
         */
        var sessionIsDirty = true
        var messageReactors: MessageReactors? = null
        var connectionReactors: ConnectionReactors? = null

        /**
         * Constant representing a successful connection to the socket.
         */
        const val CONNECTED = "connected to socket"

        /**
         * Constant representing a disconnection from the socket.
         */
        const val DISCONNECTED = "disconnected from socket"
        /**
         * Constant representing an internal server error.
         */
        const val INTERNAL_SERVER_ERROR = "Internal server error"
        /**
         * Constant representing the result of a host token request.
         */
        const val HOST_TOKEN_RESULT = "host_token"
        /**
         * Constant representing the action for the first part of a transfer.
         */
        const val TRANSFER_PART_ONE_ACTION = "host:transfer_part1"

        /**
         * Constant representing the result of a tokenization.
         */
        const val TOKENIZE_RESULT = "tokenize_complete"
        /**
         * Constant representing the action for a barcode scan.
         */
        const val BARCODE_ACTION = "host:barcode"
        /**
         * Constant representing the result of a barcode scan.
         */
        const val BARCODE_RESULT = "barcode_complete"

        /**
         * Constant representing the action for a wallet transaction (e.g., Google Pay).
         */
        const val WALLET_TRANSACTION_ACTION = "host:wallet_transaction"

        /**
         * Constant representing the result of a wallet transaction.
         */
        const val WALLET_TRANSACTION_RESULT = "wallet_transaction_complete"

        const val TOKENIZE = "host:tokenize"
        /**
         * Constant representing the result of the first part of a transfer.
         */
        const val TRANSFER_PART_ONE_RESULT = "transfer_confirmation"
        /**
         * Constant representing the completion of a transfer.
         */
        const val COMPLETED_TRANSFER = "transfer_complete"
        /**
         * Constant representing an unknown state or action.
         */
        const val UNKNOWN = "unknown"
        /**
         * Constant representing a cash payment.
         */
        const val CASH = "cash"
    }


    /**
     * Resets the Pay Theory token, attempting to reconnect to the server.
     * This method is called when there is an issue with the existing token and a new one needs to be obtained. It uses a counter to limit the number of reconnect attempts.
     */
    private fun attemptReconnectToPtToken() {
        if (ptResetCounter < 2000) {
            disconnect()
//            println("PT Token Reconnect Counter: $ptResetCounter")
            ptResetCounter++
            ptTokenApiCall(this.payable)
        } else {
            messageReactors?.onError("NETWORK_ERROR: Please check device connection", this)
        }
    }

    /**
     * Resets the socket connection in case of network failures.
     * This method attempts to reconnect to the websocket server if the connection is lost. It uses a counter to limit the number of reconnect attempts before reporting a network error to the user. It's called when the socket experiences a disconnection.
     */
    fun resetSocket() {
        if (resetCounter < 50) {
//            println("Reconnect Counter: $resetCounter")
            resetCounter++
            ptTokenApiCall(this.payable)
        } else {
            messageReactors?.onError("NETWORK_ERROR: Please check device connection", this)
        }
    }

    /**
     * Initiates the Pay Theory token API call to obtain a PT-Token.
     * This method makes a network request to the Pay Theory API to retrieve a PT-Token, which is necessary for establishing a secure websocket connection.
     * @param context The application context.
     */
    @SuppressLint("CheckResult")
    fun ptTokenApiCall(context: Payable) {
        if (sessionIsDirty) {
            headerMap["x-session-key"] = UUID.randomUUID().toString()
            sessionIsDirty = false
        }

        if (!configuration.isTestMode) {
//            if(integrityTokenProvider == null)
//            {
//                initializeAndPrefetchIntegrityToken()
//            }
            // Now that the token provider is ready, make the API call
            val observable =
                ApiService(configuration.apiBasePath).ptTokenApiCall().doToken(headerMap)
            val disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ ptTokenResponse ->
                    ptResetCounter = 0
                    initiateGooglePlayIntegrityCheck(ptTokenResponse, integrityTokenProvider) // Pass the tokenProvider
                }, { error ->
                    handleApiError(error, context)
                })
            disposables.add(disposable)

        } else {
            val observable =
                ApiService(configuration.apiBasePath).ptTokenApiCall().doToken(headerMap)
            val disposable = observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ ptTokenResponse ->
                    ptResetCounter = 0
                    initiateGooglePlayIntegrityCheck(ptTokenResponse, integrityTokenProvider)
                }, { error ->
                    handleApiError(error, context)
                })
            disposables.add(disposable)
        }
    }

    /**
     * Helper method to handle API errors and report them to the Payable interface.
     *
     * @param error The error that occurred.
     * @param context The Payable interface for reporting errors.
     */
    private fun handleApiError(error: Throwable, context: Payable) {
        if (error.message.toString().contains("Unable to resolve host")) {
            disconnect()
            attemptReconnectToPtToken()
        } else if (error.message.toString().contains("HTTP 500")) {
            disconnect()
            resetSocket()
        } else if (error.message == "HTTP 404 ") {
            context.handleError(PTError(ErrorCode.InvalidAPIKey, "Access Denied"))
        } else {
            println("ptTokenApiCall " + error.message)
            context.handleError(
                PTError(
                    ErrorCode.AttestationFailed,
                    error.message.toString()
                )
            )
        }
    }

//    /**
//     * Prepares the Google Play Integrity API by initializing and potentially pre-fetching an integrity token.
//     * This is done to reduce latency when requesting an integrity token later during the payment process.
//     * It initializes the IntegrityManager and prepares an integrity token.
//     */
//    private fun initializeAndPrefetchIntegrityToken() {
//        if (configuration.isTestMode != true) {
//            val googleProjectNumber: Long = getGoogleProjectNumber()
//
//
//            ptTokenApiCall(payable)
//
//            val context = payable.getContext()
//                ?: throw IllegalStateException("Context is required for non-test environments")
//            val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)
//
//            // Prepare integrity token. Can be called once in a while to keep internal
//            // state fresh.
//            standardIntegrityManager.prepareIntegrityToken(
//                PrepareIntegrityTokenRequest.builder()
//                    .setCloudProjectNumber(googleProjectNumber)
//                    .build()
//            )
//                .addOnSuccessListener { tokenProvider ->
//                    integrityTokenProvider = tokenProvider
//                    ptTokenApiCall(payable)
//                }
//                .addOnFailureListener { exception ->
//                    Logger.getLogger("warmUpPlayIntegrity").log(Level.WARNING,exception.message.toString())
//                }
//        }
//    }

    /**
     * Initiates the Google Play Integrity check and proceeds to establish the websocket connection
     * if the integrity check is successful.
     * @param ptTokenResponse The response containing the Pay Theory token.
     */
    private fun initiateGooglePlayIntegrityCheck(ptTokenResponse: PTTokenResponse, integrityTokenProvider: StandardIntegrityTokenProvider?) {
        if (configuration.isTestMode == true) {
            establishViewModel(ptTokenResponse)
        } else {
            // See above how to prepare integrityTokenProvider.

            // Request integrity token by providing a user action request hash. Can be called
            // several times for different user actions.
            val digest = MessageDigest.getInstance("SHA-256")
            val requestHash =
                digest.digest(ptTokenResponse.challengeOptions.challenge.toByteArray(Charsets.UTF_8))

            val integrityTokenResponse: Task<StandardIntegrityToken> =
                integrityTokenProvider!!.request(
                    StandardIntegrityTokenRequest.builder()
                        .setRequestHash(Base64.getEncoder().encodeToString(requestHash))
                        .build()
                )
            integrityTokenResponse
                .addOnSuccessListener(OnSuccessListener { response ->
                    establishViewModel(ptTokenResponse, response.token())
                }
                )
                .addOnFailureListener(OnFailureListener { exception ->

                    if (exception.message?.contains("Network error") == true) {
                        println("Google Play Integrity API Network Error. Retrying...")
                        disconnect()
                        resetSocket()
                    } else {
                        payable.handleError(
                            PTError(
                                ErrorCode.AttestationFailed,
                                exception.message!!
                            )
                        )
                    }

                })
        }
    }

    /**
     * Signals whether the payment process is ready to begin.
     * This method updates the Payable interface to indicate if the payment
     * process is ready to start accepting user input.
     * @param isReady True if ready, false otherwise.
     */
    fun updatePayableReadyState(isReady: Boolean) {
        payable.handleReady(isReady)
    }



    /**
     * Establishes the websocket connection and initializes the necessary components for communication.
     * @param ptTokenResponse The response containing the Pay Theory token.
     * @param attestationResult The result of the Google Play Integrity check (optional).
     */
    abstract fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String? = "")

    abstract fun process(payment: PaymentDetail)

    /**
     * Sets the connection credentials for the payment processor.
     * This method is called by MessageReactors to safely set the credentials.
     *
     * @param pubKey The public key for encryption
     * @param sessKey The session key for the connection
     * @param hToken The host token for authentication
     */
    fun setConnectionCredentials(pubKey: String, sessKey: String, hToken: String) {
        this.publicKey = pubKey
        this.sessionKey = sessKey
        this.hostToken = hToken
    }

    /**
     * Checks if the connection credentials are set.
     *
     * @return true if all credentials are set, false otherwise
     */
    fun hasCredentials(): Boolean {
        return !publicKey.isNullOrEmpty() && !sessionKey.isNullOrEmpty() && !hostToken.isNullOrEmpty()
    }

    abstract fun createInitialActionRequest(payment: PaymentDetail): ActionRequest

    /**
     * Disposes of any active RxJava subscriptions to prevent memory leaks.
     */
    override fun disconnect() {
        disposables.clear()
    }
}

private fun PaymentMethodProcessor.getGoogleProjectNumber(): Long
{
    var googleProjectNumber: Long = 0L
    if (configuration.isTestMode == true) {
        googleProjectNumber = 12345678L
    } else {
        val context = payable.getContext()
            ?: throw IllegalStateException("Context is required for non-test environments")
        googleProjectNumber = context.resources.getString(R.string.google_project_number).toLong()
    }

    return googleProjectNumber
}