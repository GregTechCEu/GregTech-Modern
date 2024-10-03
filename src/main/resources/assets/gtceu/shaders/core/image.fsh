#version 150

out vec2 texCoord0;

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    fragColor = texture2D(Sampler0, texCoord0).rgba;
}
