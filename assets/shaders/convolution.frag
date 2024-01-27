#ifdef GL_ES
    #define PRECISION mediump
    precision PRECISION float;
#else
    #define PRECISION
#endif

#ifndef MAT_SIDE
#error Please define a MAT_SIDE
#endif

const int INDEX_SHIFT = int(floor(float(MAT_SIDE) / 2.0));

varying vec2 v_texCoords;

uniform sampler2D u_texture0;
uniform vec2 u_texelSize;
uniform float u_convMat[MAT_SIDE * MAT_SIDE];

void main() {
    vec4 resultColor = vec4(0);
    for (int x = 0; x < MAT_SIDE; x++) {
        for (int y = 0; y < MAT_SIDE; y++) {
            int xShift = x - INDEX_SHIFT;
            int yShift = y - INDEX_SHIFT;
            vec2 coord = v_texCoords + vec2(
                u_texelSize.x * -float(xShift),
                u_texelSize.y * +float(yShift));
            vec4 pixelColor = texture2D(u_texture0, coord);
            resultColor += pixelColor * u_convMat[y * MAT_SIDE + x];
        }
    }
    gl_FragColor = resultColor;
}
