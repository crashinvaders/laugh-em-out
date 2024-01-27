package com.crashinvaders.common

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import ktx.app.gdxError
import ktx.log.debug

object ShaderLoader {
    var pedantic: Boolean = true
    var logCompilation: Boolean = true

    fun fromFile(vertexFile: FileHandle, fragmentFile: FileHandle, defines: String = ""): ShaderProgram {
        if (logCompilation) {
            var logText = "\"${vertexFile.name()}/${fragmentFile.name()}\""
            if (defines.isNotEmpty()) {
                logText += " w/ (" + defines.replace("\n", ", ") + ")"
            }
            logText += "..."
            debug { "Compiling $logText" }
        }

        val vpSrc = vertexFile.readString()
        val fpSrc = fragmentFile.readString()

        val program = fromString(vpSrc, fpSrc, defines)
        return program
    }

    fun fromString(vertex: String, fragment: String, defines: String = ""): ShaderProgram {
        ShaderProgram.pedantic = pedantic
        val vertComposed = "$defines\n$vertex"
        val fragComposed = "$defines\n$fragment"
        val shader = ShaderProgram(vertComposed, fragComposed)
        if (!shader.isCompiled) {
            gdxError("Failed to compile the shader: ${shader.log}\n#### Defines:\n$defines\n#### Vertex:\n$vertex\n#### Fragment:\n$fragment")
        }
        return shader
    }
}
