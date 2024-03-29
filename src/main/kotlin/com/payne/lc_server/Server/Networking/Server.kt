package Networking

import java.io.*
import java.net.*
import java.nio.channels.*


class Server(private val portNum: Int) {

    val udpSocketChannel: DatagramChannel = DatagramChannel.open()
    private val serverSocketChannel = ServerSocketChannel.open()

    private val selector = Selector.open()

    val tcpHandler = TCPHandler(this);
    val udpHandler = UDPHandler(this);

    init {
        val host = InetAddress.getLocalHost().hostAddress
        println("Host IP: "+host);
        serverSocketChannel.configureBlocking(false)
        serverSocketChannel.bind(InetSocketAddress(host, portNum))
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT)

        udpSocketChannel.configureBlocking(false)
        udpSocketChannel.bind(InetSocketAddress(host, portNum))
        udpSocketChannel.register(selector, SelectionKey.OP_READ)
    }

    /*
    Check if the sockets are readable and if so notify the TCP or UDP handler to deal with them
     */
    fun update() {

        selector.selectNow()
        var selectedKeys = selector.selectedKeys()

        try {
            for (key in selectedKeys) {


                if (key.isAcceptable) {
                    val client: SocketChannel = serverSocketChannel.accept()
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }

                try {
                    if (key.isReadable) {
                        //Handles both TCP and UDP input so see what we are dealing with
                        if (key.channel() is SocketChannel) {
                            tcpHandler.getPacket(key.channel() as SocketChannel)
                        } else if (key.channel() is DatagramChannel) {
                            udpHandler.getUDPPackets(key.channel() as DatagramChannel);
                        }
                    }
                } catch (e: IOException) {

                    println("Error: $e")

                    key.channel().close()
                }

            }

            selectedKeys.clear();
        } catch (e: IOException) {
            println("Error: $e");
        }

        selector.selectNow()
        selectedKeys = selector.selectedKeys()
    }

    fun closeServer(){
        udpSocketChannel.close();
        serverSocketChannel.close();
        tcpHandler.closeAllSockets();
        udpHandler.terminateAll();
    }


}
