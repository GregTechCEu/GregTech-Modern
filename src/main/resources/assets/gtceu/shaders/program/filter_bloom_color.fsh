#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MainSampler;
uniform bool EnableFilter;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    fragColor = texture(DiffuseSampler, texCoord);
    if (EnableFilter){
        vec4 mainColor = texture(MainSampler, texCoord);
        if (distance((mainColor.rgb * fragColor.a), fragColor.rgb) > 0.01){
            fragColor = vec4(0.0);
        }
    }
}
