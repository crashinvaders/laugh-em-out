@file:JvmName("Lwjgl3Launcher")

package com.crashinvaders.laughemout.lwjgl3

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.crashinvaders.common.IniParser
import com.crashinvaders.laughemout.App
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

/** Launches the desktop (LWJGL3) application. */
fun main() {
    // This handles macOS support and helps on Windows.
    if (StartupHelper.startNewJvmIfRequired())
      return

    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Laugh'em Out")
        setWindowedMode(800, 480)
        setWindowIcon(*(arrayOf(128, 64, 32, 16).map { "libgdx$it.png" }.toTypedArray()))
    }

    val params = App.Params()

    applyParamsFromIni(config, params, "desktop.ini")

    Lwjgl3Application(App(params), config)
}


@Throws(IOException::class)
private fun applyParamsFromIni(config: Lwjgl3ApplicationConfiguration, params: App.Params, filePath: String) {
    val sectionCommon = "common"
    val ini = IniParser()

    val localFile = File(filePath)
    if (localFile.exists()) {
        ini.load(localFile)
    }

    if (!ini.isLoaded) {
        val classLoader: ClassLoader = Any::class::class.java.classLoader
        val classPathResource = classLoader.getResourceAsStream(filePath)
        if (classPathResource != null) {
            ini.load(InputStreamReader(classPathResource))
        }
    }

    if (ini.isLoaded) {
        println("Config file \"$filePath\" found")

        val fullscreen: Boolean = ini.getBoolean(sectionCommon, "fullscreen", false)
        if (fullscreen) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode())
        } else {
            config.setWindowPosition(
                ini.getInt(sectionCommon, "x", -1),
                ini.getInt(sectionCommon, "y", -1)
            )
            config.setWindowedMode(
                ini.getInt(sectionCommon, "width", 800),
                ini.getInt(sectionCommon, "height", 480)
            )
        }

        params.isDebug = ini.getBoolean(sectionCommon, "debug", params.isDebug)
    }
}
