#ifdef GL_ES
    #define PRECISION mediump
    precision PRECISION float;
#else
    #define PRECISION
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform vec2 u_texelSize;

varying vec2 v_texCoords;

void main() {
    v_texCoords = a_texCoord0;
    gl_Position = a_position;
}
