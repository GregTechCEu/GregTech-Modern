#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HighLight;
uniform sampler2D BlurTexture;
uniform float BloomIntensive;
uniform float BloomBase;
uniform float BloomThresholdUp;
uniform float BloomThresholdDown;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 bloom = texture(BlurTexture, texCoord).rgb * BloomIntensive;
    vec4 background = texture(DiffuseSampler, texCoord);
    vec4 highLight = texture(HighLight, texCoord);
    background.rgb = background.rgb * (1 - highLight.a) + highLight.a * highLight.rgb;
    float max = max(background.b, max(background.r, background.g));
    float min = min(background.b, min(background.r, background.g));
    fragColor = vec4(background.rgb + bloom.rgb * ((1. - (max + min) / 2.) * (BloomThresholdUp - BloomThresholdDown) + BloomThresholdDown + BloomBase), 1.);
}
