#version 120

varying vec2 texCoord0;

uniform sampler2D DiffuseSampler;

void main(void) {
    gl_FragColor = texture2D(DiffuseSampler, texCoord0).rgba;
}
