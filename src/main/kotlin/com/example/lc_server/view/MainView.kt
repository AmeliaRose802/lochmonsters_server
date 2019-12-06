package com.example.lc_server.view

import Game
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.net.InetAddress
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class MainView : View() {

    private val result = SimpleStringProperty("NOT RUNNING")
    private val serverInfo = SimpleStringProperty("NOT RUNNING")


    override val root = form {
        fieldset {
            button("Start Server") {
                action {
                    println("Starting Server")

                    thread() {
                        Game.loop()
                    }

                    isDisable = true
                    result.set("RUNNING")
                    serverInfo.set("IP : "+ InetAddress.getLocalHost().hostAddress + " Port: "+ Game.PORT_NUM)

                }
            }

            field {
                text = "Status:"
                label().bind(result)
            }

            field{
                text = "Info: "
                label().bind(serverInfo);
            }
        }

    }

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            exitProcess(0);
        }
    }


}