import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class SocketClient(serverURI: URI?) : WebSocketClient(serverURI) {

    override fun onOpen(handshakedata: ServerHandshake) {
        send("Hello, it is me. Mario :)")
        println("new connection opened")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("closed with exit code $code additional info: $reason")
    }

    override fun onMessage(message: String) {
        println("received message: $message")
    }

    override fun onMessage(message: ByteBuffer) {
        println("received ByteBuffer")
    }

    override fun onError(ex: Exception) {
        System.err.println("an error occurred:$ex")
    }

    companion object {
        @Throws(URISyntaxException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client: WebSocketClient = SocketClient(URI("ws://localhost:8887"))
            client.connect()
        }
    }
}