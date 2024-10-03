#version 150

in vec3 Position;

out vec2 texCoord0;

void main(void){
    float x = -1.0;
    float y = -1.0;
    if (Position.x > 0.001){
        x = 1.0;
    }
    if (Position.y > 0.001){
        y = 1.0;
    }
    gl_Position = vec4(x, y, 0.2, 1.0);
    texCoord0 = Position.xy;
}
