#version 120

in vec3 Position;

varying vec2 texCoord0;

void main(void){
    gl_Position = vec4(Position, 1.0);
    texCoord0 = Position.xy * 0.5 + 0.5;
}
