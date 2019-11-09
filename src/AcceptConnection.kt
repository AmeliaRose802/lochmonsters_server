import java.io.*
import java.net.*
const val portNum = 5555

//Client and server exchange messages of set types each of which has a set format and length
val messageLengths = mapOf('c' to 39);

data class Vector2(val x : Float, val y : Float);

data class Color(val r : Short, val b: Short, val g: Short);

data class Client(val name: String, val color: Color, val pos : Vector2, val dir : Vector2);

val clients = mutableMapOf<Int, Client>()

var nextID : Int = 0;

fun main(args: Array<String>) {
    println("Server is listening...")

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
}

//Add a new player to the game
fun AddClient(dis : DataInputStream) : Int{
    val r = dis.readShort();
    val g = dis.readShort();
    val b = dis.readShort();
    println("The color is $r $g $b");

    val name : ByteArray = ByteArray(32);

    dis.read(name)

    val playerName = String(name).trim();
    println(playerName);

    val currentID = nextID;
    clients.put(nextID, Client(playerName, Color(r, g, b), Vector2(0f,0f), Vector2(0f,0f)))
    println(clients[nextID]);

    nextID++;

    return currentID;
}

fun SendConnectReply(clientSocket : Socket){
    val dos = DataOutputStream(clientSocket.getOutputStream())
    dos.writeChar('c'.toInt())
    dos.writeFloat(0f)
    dos.writeFloat(0f)
    dos.flush()
}