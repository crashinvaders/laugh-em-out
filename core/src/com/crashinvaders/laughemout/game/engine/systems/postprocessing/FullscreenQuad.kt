package com.crashinvaders.laughemout.game.engine.systems.postprocessing

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Mesh
import com.badlogic.gdx.graphics.VertexAttribute
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.utils.Disposable

/**
 * Encapsulates a fullscreen quad, geometry is aligned to the screen corners.
 */
class FullscreenQuad : Disposable {

    private val quad: Mesh = createFullscreenQuad()

    override fun dispose() {
        quad.dispose()
    }

    /** Renders the quad with the specified shader program.  */
    fun render(program: ShaderProgram) {
        quad.render(program, GL20.GL_TRIANGLE_FAN, 0, 4)
    }

    private fun createFullscreenQuad(): Mesh {
        val mesh = Mesh(
            Mesh.VertexDataType.VertexArray, true, 4, 0,
            VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
            VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")
        )
        mesh.setVertices(verts)
        return mesh
    }

    companion object {
        private const val VERT_SIZE = 16
        private val verts = FloatArray(VERT_SIZE)
        private const val X1 = 0
        private const val Y1 = 1
        private const val U1 = 2
        private const val V1 = 3
        private const val X2 = 4
        private const val Y2 = 5
        private const val U2 = 6
        private const val V2 = 7
        private const val X3 = 8
        private const val Y3 = 9
        private const val U3 = 10
        private const val V3 = 11
        private const val X4 = 12
        private const val Y4 = 13
        private const val U4 = 14
        private const val V4 = 15

        init {
            // vertex coord
            verts[X1] = -1f
            verts[Y1] = -1f

            verts[X2] = 1f
            verts[Y2] = -1f

            verts[X3] = 1f
            verts[Y3] = 1f

            verts[X4] = -1f
            verts[Y4] = 1f

            // tex coords
            verts[U1] = 0f
            verts[V1] = 0f

            verts[U2] = 1f
            verts[V2] = 0f

            verts[U3] = 1f
            verts[V3] = 1f

            verts[U4] = 0f
            verts[V4] = 1f
        }
    }
}
