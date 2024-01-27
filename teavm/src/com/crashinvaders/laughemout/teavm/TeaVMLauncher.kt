@file:JvmName("TeaVMLauncher")

package com.crashinvaders.laughemout.teavm

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration
import com.github.xpenatan.gdx.backends.teavm.TeaApplication
import com.crashinvaders.laughemout.App

fun main() {
    val config = TeaApplicationConfiguration("canvas").apply {
        width = 0
        height = 0
//        showDownloadLogs = true
    }

    val params = App.Params(isDebug = true) //TODO Extract from URL params.

    TeaApplication(App(params), config)
}
