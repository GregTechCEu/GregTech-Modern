#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D HighLight;
uniform sampler2D BlurTexture1;
uniform sampler2D BlurTexture2;
uniform sampler2D BlurTexture3;
uniform sampler2D BlurTexture4;
uniform float BloomRadius;
uniform float BloomIntensive;
//uniform float BloomBase;
//uniform float BloomThresholdUp;
//uniform float BloomThresholdDown;

in vec2 texCoord;
out vec4 fragColor;

float lerpBloomFactor(const in float factor) {
    float mirrorFactor = 1.2 - factor;
    return mix(factor, mirrorFactor, BloomRadius);
}

vec3 aces(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

vec3 aces_tonemap(vec3 color){
    mat3 m1 = mat3(
    0.59719, 0.07600, 0.02840,
    0.35458, 0.90834, 0.13383,
    0.04823, 0.01566, 0.83777
    );
    mat3 m2 = mat3(
    1.60475, -0.10208, -0.00327,
    -0.53108, 1.10813, -0.07276,
    -0.07367, -0.00605, 1.07602
    );
    vec3 v = m1 * color;
    vec3 a = v * (v + 0.0245786) - 0.000090537;
    vec3 b = v * (0.983729 * v + 0.4329510) + 0.238081;
    return pow(clamp(m2 * (a / b), 0.0, 1.0), vec3(1.0 / 2.2));
}

vec3 jodieReinhardTonemap(vec3 c){
    float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    vec3 tc = c / (c + 1.0);

    return mix(c / (l + 1.0), tc, tc);
}

void main() {
    vec4 bloom = BloomIntensive * (lerpBloomFactor(1.) * texture(BlurTexture1, texCoord) +
    lerpBloomFactor(0.8) * texture(BlurTexture2, texCoord) +
    lerpBloomFactor(0.6) * texture(BlurTexture3, texCoord) +
    lerpBloomFactor(0.4) * texture(BlurTexture4, texCoord));

    vec4 background = texture(DiffuseSampler, texCoord);
    vec4 highLight = texture(HighLight, texCoord);
    background.rgb = background.rgb * (1 - highLight.a) + highLight.a * highLight.rgb;
    fragColor = vec4(background.rgb + jodieReinhardTonemap(bloom.rgb), 1.);
}
