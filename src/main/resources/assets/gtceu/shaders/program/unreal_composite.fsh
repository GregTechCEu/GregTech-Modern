#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HighlightSampler;
uniform sampler2D BlurTexture1;
uniform sampler2D BlurTexture2;
uniform sampler2D BlurTexture3;
uniform sampler2D BlurTexture4;
uniform float BloomRadius;
uniform float BloomStrength;
uniform float BaseBrightness;
uniform float MaxBrightness;
uniform float MinBrightness;

in vec2 texCoord;
out vec4 fragColor;

float lerpBloomFactor(float factor) {
    float mirrorFactor = 1.2 - factor;
    return mix(factor, mirrorFactor, BloomRadius);
}

// from https://www.shadertoy.com/view/4dBcD1, explanation(s) there
vec3 jodieReinhardTonemap(vec3 color) {
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    vec3 tc = color / (color + 1.0);

    return mix(color / (luma + 1.0), tc, tc);
}
vec3 jodieReinhard2Tonemap(const vec3 color) {
    float luma = dot(color, vec3(.2126, .7152, .0722));

    // tonemap curve goes on this line
    // (I used reinhard here)
    vec4 rgbl = vec4(color, luma) / (luma + 1.);

    vec3 mappedColor = rgbl.rgb;
    float mappedLuma = rgbl.a;

    float channelMax = max(max(max(mappedColor.r, mappedColor.g), mappedColor.b), 1.0);

    // this is just the simplified/optimised math of the more human readable version linked above
    return (
        (mappedLuma * mappedColor - mappedColor) -
        (channelMax * mappedLuma - mappedLuma)
    ) / (mappedLuma - channelMax);
}

void main() {
    vec4 bloom = BloomStrength * (
    lerpBloomFactor(1.0) * texture(BlurTexture1, texCoord) +
    lerpBloomFactor(0.8) * texture(BlurTexture2, texCoord) +
    lerpBloomFactor(0.6) * texture(BlurTexture3, texCoord) +
    lerpBloomFactor(0.4) * texture(BlurTexture4, texCoord));
    bloom.rgb = jodieReinhardTonemap(bloom.rgb);

    vec4 background = texture(DiffuseSampler, texCoord);
    vec4 highlight = texture(HighlightSampler, texCoord);
    background.rgb = background.rgb * (1 - highlight.a) + highlight.rgb * highlight.a;

    float min = min(background.r, min(background.g, background.b));
    float max = max(background.r, max(background.g, background.b));
    float backgroundBrightness = (max + min) / 2.0;

    fragColor = vec4(background.rgb + bloom.rgb * (MinBrightness + BaseBrightness + (1.0 - backgroundBrightness) * (MaxBrightness - MinBrightness)), bloom.a);
}
