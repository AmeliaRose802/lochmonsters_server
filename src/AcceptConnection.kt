import java.io.*
import java.net.*
import java.net.DatagramPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun main() {
    Server().listen()
}

class Server() {
    val portNum = 5555

    //Data classes
    data class Vector2(var x: Float, var y: Float);
    data class Color(val r: Short, val b: Short, val g: Short);
    data class Client(val name: String, val color: Color, var pos: Vector2, var dir: Vector2);

    //Currently Connected clients (eg monsters in the game)
    val clients = mutableMapOf<String, Client>()

    //TODO: Assign uneque ID's instead of indexing by name
    var nextID: Int = 0;

    //Initalize the sockets
    val udpSocket = DatagramSocket(portNum);
    val tcpServerSocket = ServerSocket(portNum);

    fun main(args: Array<String>) {


        //Until accepting clients actually works adding one for testing pourpuses.


        /*
        try {
            val ss = ServerSocket(portNum)
            val s = ss.accept()//establishes connection
            println("Established Connection");
            val dis = DataInputStream(s.getInputStream())
            //val bis = BufferedInputStream(s.getInputStream())

            val type = dis.read().toChar();
            println("type= $type")
            val length = messageLengths[type]!!;


            //Yes, I will be figuring out something more elegent later
            if(type === 'c'){
                AddClient(dis);
                SendConnectReply(s);
            }

            ss.close()
        } catch (e: Exception) {
            println(e)
        }
        */

    }

    fun listen() {
        println("Server is listening...")
        clients.put("AAAA", Client("AAAA", Color(0, 0, 0), Vector2(0f, 0f), Vector2(0f, 0f)))
        getUDPPackets();
    }

    fun getUDPPackets() {
        while (true) {
            val buf = ByteArray(256)

            var packet = DatagramPacket(buf, buf.size)
            udpSocket.receive(packet)

            val address = packet.address
            val port = packet.port
            packet = DatagramPacket(buf, buf.size, address, port)

            val dataBuffer = ByteBuffer.wrap(packet.data);
            dataBuffer.order(ByteOrder.LITTLE_ENDIAN);

            //First byte of every packet indicates type
            val type = dataBuffer.char;


            println("First bit  $type");

            when (type) {
                'p' -> handlePosUpdate(dataBuffer);
                else -> {
                    println("Unknown packet type")
                }
            }
        }
    }


    fun handlePosUpdate(data: ByteBuffer) {
        val nameLength = data.short.toInt();
        val nameBuffer = ByteArray(nameLength);
        data.get(nameBuffer)

        val name = String(nameBuffer);

        println(name);


        if (clients[name] == null) {
            println("Unknown Client");
            return;
        }

        clients[name]!!.pos.x = data.float;
        clients[name]!!.pos?.y = data.float;

        clients[name]!!.dir?.x = data.float;
        clients[name]!!.dir?.y = data.float;

        println(clients[name]);
    }

    //Add a new player to the game
    fun AddClient(dis: DataInputStream): String {
        val r = dis.readShort();
        val g = dis.readShort();
        val b = dis.readShort();
        println("The color is $r $g $b");

        val name: ByteArray = ByteArray(32);

        dis.read(name)

        val playerName = String(name).trim();
        println(playerName);

        val currentID = nextID;
        clients.put(playerName, Client(playerName, Color(r, g, b), Vector2(0f, 0f), Vector2(0f, 0f)))
        println(clients[playerName]);

        nextID++;

        return playerName;
    }

    fun SendConnectReply(clientSocket: Socket) {
        val dos = DataOutputStream(clientSocket.getOutputStream())
        dos.writeChar('c'.toInt())
        dos.writeInt(10);
        dos.writeInt(10);
        println(dos.size());
        dos.flush()
        println("Data sent");
    }
}
