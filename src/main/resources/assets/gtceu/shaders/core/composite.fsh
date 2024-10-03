#version 150

out vec2 texCoord0;

uniform sampler2D blurTexture1;
uniform sampler2D blurTexture2;
uniform sampler2D blurTexture3;
uniform sampler2D blurTexture4;
uniform sampler2D blurTexture5;
uniform float bloomStrength;
uniform float bloomRadius;
//uniform float bloomFactors[NUM_MIPS];
//uniform vec3 bloomTintColors[NUM_MIPS];

float lerpBloomFactor(const in float factor) {
    float mirrorFactor = 1.2 - factor;
    return mix(factor, mirrorFactor, bloomRadius);
}

out vec4 fragColor;

void main() {
    fragColor = bloomStrength * ( lerpBloomFactor(1.) * texture2D(blurTexture1, texCoord0) +
    lerpBloomFactor(0.8) * texture2D(blurTexture2, texCoord0) +
    lerpBloomFactor(0.6) * texture2D(blurTexture3, texCoord0) +
    lerpBloomFactor(0.4) * texture2D(blurTexture4, texCoord0) +
    lerpBloomFactor(0.2) * texture2D(blurTexture5, texCoord0) );
}
