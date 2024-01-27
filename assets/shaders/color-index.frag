#ifdef GL_ES
    #define PRECISION highp
    precision PRECISION float;
#else
    #define PRECISION
#endif

#ifndef MAP_LAYERS
#error Please define a MAP_LAYERS
#endif

const float X_LAYER_STEP = 1.0 / float(MAP_LAYERS);
const float X_HALF_TEXEL_STEP = X_LAYER_STEP * X_LAYER_STEP * 0.5;

varying vec2 v_texCoords;

uniform sampler2D u_texture0;
uniform sampler2D u_palette;

void main(void) {
    vec3 rawColor = texture2D(u_texture0, v_texCoords).rgb;

    float coordR = max(0.0, (rawColor.r - X_HALF_TEXEL_STEP)) * X_LAYER_STEP;
    float coordG = rawColor.g;
    float coordB = floor(max(0.0, (rawColor.b - X_HALF_TEXEL_STEP)) / X_LAYER_STEP) * X_LAYER_STEP;
    vec2 palettePos = vec2(coordR + coordB, coordG);

    vec3 color = texture2D(u_palette, palettePos).rgb;
    gl_FragColor = vec4(color, 1.0);
}
