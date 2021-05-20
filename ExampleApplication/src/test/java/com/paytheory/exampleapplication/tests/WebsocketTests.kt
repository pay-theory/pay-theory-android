package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.api.*
import com.paytheory.android.sdk.reactors.ConnectionReactors
import com.paytheory.android.sdk.reactors.MessageReactors
import com.paytheory.android.sdk.websocket.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString
import org.junit.Test
import org.mockito.Mockito

/**
 * Class that is used to test websockets
 */
class WebsocketTests  {

    private var messageReactors: MessageReactors? = null
    private var connectionReactors: ConnectionReactors? = null
    val socketException = SocketAbortedException()

    lateinit var viewModel: WebSocketViewModel
    private var webServicesProvider: WebServicesProvider? = null
    private var webSocketRepository: WebsocketRepository? = null
    var webSocketInteractor: WebsocketInteractor? = null

    var ptToken ="eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MjEzNjYyNjUsIl" +
            "VJRCI6IjkzYjQ1MmVjLTIzMGEtNGEyZC1iNDU5LTA2NzFlNTBkZWRjNCIsIm1lcmNoYW50Ijp7ImZpeGVkX2ZlZSI6eyJmZWUiOjE" +
            "5NSwibWVyY2hhbnQiOiJJRG1SRXNidW41VFJYQkxmVktpMXZuNU4ifSwiYmFzaXNfcG9pbnRzIjp7ImZlZSI6MzgwLCJtZXJjaGFu" +
            "dCI6IklEZVY2MVVDSGFReFhUWDJUZHNpQ2ZkcyJ9LCJzdXJjaGFyZ2UiOnsiZmVlIjowLCJtZXJjaGFudCI6IklEa040RlJrWTRwb" +
            "zlFOG55VFlRN2I4OCJ9fSwiY2hhbGxlbmdlIjoibVVZYnA2M1dXblNqS3FHM3l6SXJqRWx2OW9LbmVYc2hwa2xqeENEajQ2UUpoQT" +
            "NSa2VybTNYWHJZM2VZR1J6YkZaTUJXT2pES09rWVNuR253WGhabXUtcnlKcloyeFB0eWNqV2I5WEhQLUtiem1Sdjk2Uk50Q0xBY3M3" +
            "OFZHSGZvSzFqR29xSkxCREpqby1Jd3lEVGJfMnZzbUpYcTAzWnVpMW9TYUpidFBNPSIsIm9yaWdpbiI6Im5hdGl2ZSIsImFwaUtleSI" +
            "6InB0LXNhbmRib3gtemlwcHNsaXAtOTcwY2M1ZDliNjI5MWI1MzU0YTYzYTFhNDUzYmE5NDIifQ.Ik3IHpMoAcpSMqEeXTeAkBX6s" +
            "9pn8BAemNnq9NrBYHA"
    var environment = "test"



    private fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String = "") {
        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)

        viewModel = WebSocketViewModel(webSocketInteractor!!, ptTokenResponse.ptToken, environment)
        connectionReactors = ConnectionReactors(ptTokenResponse.ptToken, attestationResult, viewModel, webSocketInteractor!!)
        messageReactors = MessageReactors(viewModel, webSocketInteractor!!)
    }

    /**
     *
     */
    @Test
    fun webSocketTests() {
        establishViewModel(PTTokenResponse(ptToken, origin="native", ChallengeOptions(challenge="mUYbp63WWnSjKqG3yzIrjElv9oKneXshpkljx" +
                "CDj46QJhA3Rkerm3XXrY3eYGRzbFZMBWOjDKOkYSnGnwXhZmu-ryJrZ2xPtycjWb9XHP-KbzmRv96RNtCLAcs78VGHfoK1jGoqJLBDJjo" +
                "-IwyDTb_2vsmJXq03Zui1oSaJbtPM=", Rp(name="Pay Theory SDK", amount="native"), User(id="IDeV61UCHaQxXTX2TdsiCfds",
            name="Pay Theory SDK", displayName="Pay Theory SDK Merchant"), arrayListOf<PubKeyCredParam>(PubKeyCredParam(alg=-7, type="public-key")),
            AuthenticatorSelection(authenticatorAttachment="platform", userVerification="required"), timeout=300000, attestation="none")))
    }

    /**
     *
     */
    @ExperimentalCoroutinesApi
    @Test
    fun webServicesProviderTests() {

        val socketUpdate = SocketUpdate("test text", Mockito.mock(ByteString::class.java), socketException)

        val webSocketListener = WebSocketListener()
        val webServicesProvider = WebServicesProvider()

        webServicesProvider.startSocket(ptToken, environment)
        webServicesProvider.sendMessage("test message")
        webServicesProvider.stopSocket()


        webServicesProvider.startSocket(webSocketListener, ptToken, environment)
        webServicesProvider.sendMessage("test message")
        webServicesProvider.stopSocket()


        assert(WebServicesProvider.NORMAL_CLOSURE_STATUS == 1000)
        assert(webServicesProvider is WebServicesProvider)
        assert(socketException is SocketAbortedException)
        assert(socketUpdate is SocketUpdate)
        assert(socketUpdate.exception is SocketAbortedException)
        assert(socketUpdate.text == "test text")
        assert(socketUpdate.byteString is ByteString)

        }

    /**
     *
     */
    @ExperimentalCoroutinesApi
    @Test
    fun webSocketViewModelTests() {

        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)

        val viewModel = WebSocketViewModel(webSocketInteractor!!, "test token", "paytheory")

        viewModel.subscribeToSocketEvents(Mockito.mock(WebsocketMessageHandler::class.java))
        viewModel.sendSocketMessage("test message")

        assert(viewModel.connected)

        viewModel.disconnect()

        assert(WebServicesProvider.NORMAL_CLOSURE_STATUS == 1000)

    }

    /**
     *
     */
    @ExperimentalCoroutinesApi
    @Test
    fun webSocketListenerTests() {

        val webSocketListener = WebSocketListener()
        val webSocket = Mockito.mock(WebSocket::class.java)
        val response = Mockito.mock(Response::class.java)
        webSocketListener.onOpen(webSocket, response)

        webSocketListener.onMessage(webSocket, "test text")

        webSocketListener.onClosing(webSocket, 1, "test reason")

        webSocketListener.onFailure(webSocket, socketException, response)

        //need assert

    }
}

