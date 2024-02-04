package com.crashinvaders.laughemout.teavm

import com.github.xpenatan.gdx.backends.teavm.config.TeaBuildConfiguration
import com.github.xpenatan.gdx.backends.teavm.config.TeaBuilder
import com.github.xpenatan.gdx.backends.teavm.config.plugins.TeaReflectionSupplier
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass
import java.io.File

/** Builds the TeaVM/HTML application. */
@SkipClass
object TeaVMBuilder {
    @JvmStatic fun main(arguments: Array<String>) {
        val teaBuildConfiguration = TeaBuildConfiguration().apply {
            assetsPath.add(File("../assets"))
            webappPath = File("build/dist").canonicalPath
            // Register any extra classpath assets here:
            // additionalAssetsClasspathFiles += "com/crashinvaders/laughemout/asset.extension"
        }

        // Register any classes or packages that require reflection here:
         TeaReflectionSupplier.addReflectionClass("com.crashinvaders.laughemout.game.engine.systems.entityactions.actions")
         TeaReflectionSupplier.addReflectionClass("com.crashinvaders.laughemout.game.engine.systems.entityactions.actions.transform")

        val tool = TeaBuilder.config(teaBuildConfiguration)
        tool.mainClass = "com.crashinvaders.laughemout.teavm.TeaVMLauncher"
        // tool.isSourceMapsFileGenerated = true
        // tool.isDebugInformationGenerated = true
        TeaBuilder.build(tool)
    }
}
