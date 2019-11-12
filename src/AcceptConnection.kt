import java.io.*
import java.net.*
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.net.InetSocketAddress
import sun.nio.ch.IOUtil.configureBlocking
import java.net.InetAddress
import jdk.nashorn.internal.objects.ArrayBufferView.buffer
import java.nio.channels.*
import kotlin.random.Random


fun main() {
    val s = Server();
    s.init()
    s.listen()
}

class Server() {
    val portNum = 5555

    //Data classes
    data class Vector2(var x: Float, var y: Float);
    data class Color(val r: Short, val b: Short, val g: Short);
    data class Client(val name: String, val color: Color, var pos: Vector2, var dir: Vector2);

    //Currently Connected clients (eg monsters in the game)
    val clients = mutableMapOf<Int, Client>()

    //TODO: Assign uneque ID's instead of indexing by name
    var nextID: Int = 1;

    //Initalize the sockets
    // val udpSocket = DatagramSocket(portNum);
    //val tcpServerSocket = ServerSocket(portNum);

    val selector = Selector.open()
    val serverSocketChannel = ServerSocketChannel.open()
    val udpSocketChannel = DatagramChannel.open()
    var buffer = ByteBuffer.allocate(256)

    fun init() {
        val host = InetAddress.getByName("localhost")

        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress(host, portNum))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

        udpSocketChannel.configureBlocking(false)
        udpSocketChannel.bind(InetSocketAddress(host, portNum))
        udpSocketChannel.register(selector, SelectionKey.OP_READ)
        val key: SelectionKey? = null
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }

    fun listen() {
        println("Server is listening...")

        while (true) {
            try {
                selector.select()
                val selectedKeys = selector.selectedKeys()
                val iter = selectedKeys.iterator()
                while (iter.hasNext()) {

                    val key = iter.next()

                    if (key.isAcceptable) {
                        println("New connection avable");
                        val client = serverSocketChannel.accept()
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        println("Regestered new connection")
                    }
                    if (key.isReadable) {
                        println("Data to read");

                        if (key.channel() is SocketChannel) {
                            getTCPPacket(key.channel() as SocketChannel, key)
                        } else if (key.channel() is DatagramChannel) {
                            getUDPPackets(key.channel() as DatagramChannel);
                        }
                        buffer.clear();
                    }

                    iter.remove()
                }
            } catch (e: IOException) {
                println("Error: $e");
            }
        }
    }


    fun getUDPPackets(socket: DatagramChannel) {
        println("UDP");
        socket.receive(buffer)
        buffer.position(0);
        val type = buffer.getChar();
        println(String(buffer.array(), 0, buffer.array().size))
        println("type= $type")
        when (type) {
            'p' -> {
                println("Position update")
                handlePosUpdate(buffer);
            }
        }
    }

    fun getTCPPacket(socket: SocketChannel, key: SelectionKey) {
        println("TCP");
        socket.read(buffer);
        buffer.position(0);
        val type = buffer.getChar();
        println(String(buffer.array(), 0, buffer.array().size))
        println("type= $type")
        when (type) {
            'c' -> {
                println("Connection request")
                val id = AddClient(buffer)

                SendConnectReply(socket, id); //Probley should be checking if write will block but oh well

            }
        }
    }


    fun handlePosUpdate(data: ByteBuffer) {
        val id = data.getInt();
        println(id);

        if (clients[id] == null) {
            println("Unknown Client");
            return;
        }

        clients[id]!!.pos.x = data.float;
        clients[id]!!.pos?.y = data.float;

        clients[id]!!.dir?.x = data.float;
        clients[id]!!.dir?.y = data.float;

        println(clients[id]);
    }

    //Add a new player to the game
    fun AddClient(buffer: ByteBuffer): Int {
        val r = buffer.short
        val g = buffer.short
        val b = buffer.short
        println("The color is $r $g $b");

        val name: ByteArray = ByteArray(32);

        buffer.get(name);

        val playerName = String(name).trim();
        println(playerName);

        val currentID = nextID;
        clients.put(currentID, Client(playerName, Color(r, g, b), Vector2(0f, 0f), Vector2(0f, 0f)))
        println(clients[currentID]);

        nextID++;

        return currentID;
    }

    fun SendConnectReply(clientSocket: SelectableChannel, id: Int) {
        val reply: ByteArray = ByteArray(21)

        val x: Int = Random.nextInt(0, 100); //Eventually this will pick a spot not near other snakes
        val y: Int = Random.nextInt(0, 100);


        reply[0] = 'c'.toByte()
        reply[2] = id.toByte()
        reply[6] = x.toByte()
        reply[10] = y.toByte()


        val c = clientSocket as SocketChannel;
        c.write(ByteBuffer.wrap(reply))

        println("Data sent");
    }
}
