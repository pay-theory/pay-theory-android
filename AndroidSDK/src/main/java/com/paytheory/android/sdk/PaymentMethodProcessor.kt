package com.paytheory.android.sdk

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.paytheory.android.sdk.api.ApiService
import com.paytheory.android.sdk.api.PTTokenResponse
import com.paytheory.android.sdk.reactors.ConnectionReactors
import com.paytheory.android.sdk.reactors.MessageReactors
import com.paytheory.android.sdk.websocket.WebServicesProvider
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import com.paytheory.android.sdk.websocket.WebsocketMessageHandler
import com.paytheory.android.sdk.websocket.WebsocketRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * @param context The context of the activity or fragment.
 * @param configuration The configuration for Pay Theory.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class PaymentMethodProcessor (
    open val context: Payable,
    open val partner: String,
    open val stage: String,
    open val constants: Constants,
    open val payTheoryData: HashMap<Any, Any>? = hashMapOf(),
    open val configuration : PayTheoryConfiguration
) : WebsocketMessageHandler {
    var isWarm: Boolean = false
    var integrityTokenProvider: StandardIntegrityTokenProvider? = null
    var resetCounter = 0
    var ptResetCounter = 0
    lateinit var viewModel: WebSocketViewModel
    var originalConfirmation: ConfirmationMessage? = null
    var headerMap =
        mutableMapOf("Content-Type" to "application/json", "X-API-Key" to configuration.apiKey)

    var publicKey: String? = null
    var sessionKey: String? = null
    var hostToken: String? = null

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
        var webServicesProvider: WebServicesProvider? = null
        var webSocketRepository: WebsocketRepository? = null
        var webSocketInteractor: WebsocketInteractor? = null

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
         * Constant representing the action for the second part of a transfer.
         */
        const val TRANSFER_PART_TWO_ACTION = "host:transfer_part2"
        /**
         * Constant representing the action to tokenize a payment method.
         */
        const val TOKENIZE = "host:tokenize"
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
     * Initializes the `Payment` instance, including warming up the Play Integrity API.
     */
    init {
        if (!isWarm) {
            prepareAndPrefetchIntegrityToken()
            isWarm = true
        }
        updatePayableReadyState(false)
    }




    /**
     * Resets the Pay Theory token, attempting to reconnect to the server.
     * This method is called when there is an issue with the existing token and a new one needs to be obtained. It uses a counter to limit the number of reconnect attempts.
     */
    private fun resetPtToken() {
        if (ptResetCounter < 2000) {
//            println("PT Token Reconnect Counter: $ptResetCounter")
            ptResetCounter++
            ptTokenApiCall(this.context as Context)
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
            ptTokenApiCall(this.context as Context)
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
    fun ptTokenApiCall(context: Context) {
        if (sessionIsDirty) {
            headerMap.put("x-session-key",UUID.randomUUID().toString())
            sessionIsDirty = false
        }


        val observable = ApiService(constants.apiBasePath).ptTokenApiCall().doToken(headerMap)
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            // handle success pt-token request
            .subscribe({ ptTokenResponse: PTTokenResponse ->
                ptResetCounter = 0
                googlePlayIntegrity(ptTokenResponse)
                // handle failed pt-token request
            }, { error ->
                if (context is Payable) {
                    // error "Unable to resolve host "evolve.paytheorystudy.com": No address associated with hostname"
                    if (error.message.toString().contains("Unable to resolve host")) {
//                        println(error.message.toString())
                        disconnect()
                        resetPtToken()
                    } else if (error.message.toString().contains("HTTP 500")) {
//                        println(error.message.toString())
                        disconnect()
                        resetSocket()
                    } else if (error.message == "HTTP 404 ") {
                        context.handleError(PTError(ErrorCode.SocketError,"Access Denied"))
                    } else {
                        println("ptTokenApiCall " + error.message)
                        context.handleError(PTError(ErrorCode.SocketError,error.message.toString()))
                    }
                }
            }
            )
    }

    /**
     * Prepares the Google Play Integrity API by initializing and potentially pre-fetching an integrity token.
     * This is done to reduce latency when requesting an integrity token later during the payment process.
     * It initializes the IntegrityManager and prepares an integrity token.
     */
    private fun prepareAndPrefetchIntegrityToken() {
        val googleProjectNumber: Long = (context as Context).resources.getString(R.string.google_project_number).toLong()
        val standardIntegrityManager = IntegrityManagerFactory.createStandard(context as Context?)


        // Prepare integrity token. Can be called once in a while to keep internal
        // state fresh.
        standardIntegrityManager.prepareIntegrityToken(
            PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(googleProjectNumber)
                .build()
        )
            .addOnSuccessListener { tokenProvider ->
                integrityTokenProvider = tokenProvider
                ptTokenApiCall(context as Context)
            }
            .addOnFailureListener { exception ->
                Logger.getLogger("warmUpPlayIntegrity").log(Level.WARNING,exception.message.toString())
            }
    }

    /**
     * Initiates the Google Play Integrity check and proceeds to establish the websocket connection
     * if the integrity check is successful.
     * @param ptTokenResponse The response containing the Pay Theory token.
     */
    private fun googlePlayIntegrity(ptTokenResponse: PTTokenResponse) {

        // See above how to prepare integrityTokenProvider.

        // Request integrity token by providing a user action request hash. Can be called
        // several times for different user actions.
        val digest = MessageDigest.getInstance("SHA-256")
        val requestHash = digest.digest(ptTokenResponse.challengeOptions.challenge.toByteArray(Charsets.UTF_8))

        val integrityTokenResponse: Task<StandardIntegrityToken> =
            integrityTokenProvider!!.request(
                StandardIntegrityTokenRequest.builder()
                    .setRequestHash(Base64.getEncoder().encodeToString(requestHash))
                    .build()
            )
        integrityTokenResponse
            .addOnSuccessListener(OnSuccessListener { response ->
                establishViewModel(ptTokenResponse, response.token()) })
            .addOnFailureListener(OnFailureListener { exception ->
                if (context is Payable) {
                    if (exception.message?.contains("Network error") == true) {
                        println("Google Play Integrity API Network Error. Retrying...")
                        disconnect()
                        resetSocket()
                    } else {
                        context.handleError(PTError(ErrorCode.SocketError,exception.message!!))
                    }
                }
            })
    }

    /**
     * Signals whether the payment process is ready to begin.
     * This method updates the Payable interface to indicate if the payment
     * process is ready to start accepting user input.
     * @param isReady True if ready, false otherwise.
     */
    fun updatePayableReadyState(isReady: Boolean) {
       context.handleReady(isReady)
    }

    /**
     * Establishes the websocket connection and initializes the necessary components for communication.
     * @param ptTokenResponse The response containing the Pay Theory token.
     * @param attestationResult The result of the Google Play Integrity check (optional).
     */
    abstract fun establishViewModel(
        ptTokenResponse: PTTokenResponse,
        attestationResult: String? = ""
    )
}