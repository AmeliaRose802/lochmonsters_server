import java.io.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.*
import java.sql.Timestamp
import kotlin.random.Random


class Server(private val portNum: Int) {

    val udpSocketChannel = DatagramChannel.open()
    private val serverSocketChannel = ServerSocketChannel.open()

    private val selector = Selector.open()

    protected val tcpHandler = TCPHandler(this);
    public val udpHandler = UDPHandler(this);

    init {

        val host = InetAddress.getByName("localhost")

        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress(host, portNum))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

        udpSocketChannel.configureBlocking(false)
        udpSocketChannel.bind(InetSocketAddress(host, portNum))
        udpSocketChannel.register(selector, SelectionKey.OP_READ)

        //TODO: Remove later, just for testing
        var id = Game.getNextID();
        //Game.gameManager.snakeManager.snakes[id] = Snake(id, "Test", Color(255, 17, 19), 3, Vector2(0.5f, 1.5f), Vector2(0.0f, 0.0f))
        //id = Game.getNextID();
        Game.gameManager.snakeManager.snakes[id] = Snake(id, "Test2", Color(0, 255, 1), 4, Vector2(0.5f, 1.5f), Vector2(0.6f, 0.5f))
    }

    /*
    Check if the sockets are readable and if so notify the TCP or UDP handler to deal with them
     */
    fun update() {

        selector.selectNow()
        val selectedKeys  = selector.selectedKeys()
        //val keysToRemove = List<SelectionKey>();

        try {


            for (key in selectedKeys) {
                if (key.isAcceptable) {
                    println("New connection");
                    val client: SocketChannel = serverSocketChannel.accept()
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }

                try {
                    if (key.isReadable) {
                        //Handles both TCP and UDP input so see what we are dealing with
                        if (key.channel() is SocketChannel) {
                            tcpHandler.getTCPPacket(key.channel() as SocketChannel)
                        } else if (key.channel() is DatagramChannel) {
                            udpHandler.getUDPPackets(key.channel() as DatagramChannel);
                        }
                    }
                } catch (e: IOException) {

                    println("Error: $e")

                    key.channel().close()
                }

                //selectedKeys.remove(key);
            }

            selectedKeys.clear();
        } catch (e: IOException) {
            println("Error: $e");
        }
    }
}
