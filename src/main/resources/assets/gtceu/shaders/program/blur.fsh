#version 150

#define PI 3.141592653
const float INV_SQRT_2PI = 1 / sqrt(2 * PI);

uniform sampler2D DiffuseSampler;
uniform float Radius;
uniform float RadiusMultiplier;

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;


// gaussian probability density function
float gaussianPdf(float x, float sigma) {
    // this is the same as (1 / sqrt(2 * PI * sigma^2)) * exp(-(x^2) / (2 * sigma^2))
    // but it's technically more efficient, since the inverse square root is stored as a constant
    float invSigma = 1 / sigma;
    return (INV_SQRT_2PI * invSigma) * exp(-(x * x) / (2.0 * invSigma * invSigma));
}

void main() {
    float weightSum = gaussianPdf(0.0, Radius);
    vec4 diffuseSum = texture(DiffuseSampler, texCoord) * weightSum;

    float actualRadius = round(Radius * RadiusMultiplier);
    for(float x = 1; x < actualRadius; x += 1.0) {
        float w = gaussianPdf(x, Radius);
        vec2 uvOffset = sampleStep * x;

        // sample both +x and -x offsets
        vec4 sample1 = texture(DiffuseSampler, texCoord + uvOffset);
        vec4 sample2 = texture(DiffuseSampler, texCoord - uvOffset);

        diffuseSum += (sample1 + sample2) * w;
        weightSum += 2.0 * w;
    }
    fragColor = diffuseSum / weightSum;
}