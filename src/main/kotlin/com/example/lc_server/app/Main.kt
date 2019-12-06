package com.example.lc_server.app

import com.example.lc_server.view.MainView
import javafx.application.Application
import tornadofx.App


object MainKt {
    @JvmStatic
    fun main(args: Array<String>) {
        Application.launch(MyApp::class.java, *args)
    }
}

class MyApp: App(MainView::class, Styles::class)