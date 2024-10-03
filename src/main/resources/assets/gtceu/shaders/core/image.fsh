#version 120

varying vec2 texCoord0;

uniform sampler2D Sampler0;

void main(void) {
    gl_FragColor = texture2D(Sampler0, texCoord0).rgba;
}
