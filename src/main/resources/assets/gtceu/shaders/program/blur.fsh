#version 150

#define PI 3.141592653

uniform sampler2D DiffuseSampler;
uniform vec2 BlurDir;
uniform float Radius;
uniform vec2 OutSize;

in vec2 texCoord;

out vec4 fragColor;

const float INV_SQRT_2PI = 1 / sqrt(2 * PI);

// gaussian probability density function
float gaussianPdf(float x, float sigma) {
    // this is the same as (1 / sqrt(2 * PI * sigma)) * exp(-(x^2) / (2 * sigma * sigma))
    // but it's technically more efficient, since it only uses division once
    // and the inverse square root is stored as a constant
    float invSigma = 1 / sigma;
    return (INV_SQRT_2PI * invSigma) * exp(-(x * x) * (0.5 * invSigma * invSigma));
}

void main() {
    vec2 invSize = 1.0 / OutSize;

    float weightSum = gaussianPdf(0.0, Radius);
    vec4 diffuseSum = texture(DiffuseSampler, texCoord) * weightSum;

    for(float x = 1; x < Radius; x += 1.0) {
        float w = gaussianPdf(x, Radius);
        vec2 uvOffset = BlurDir * invSize * x;

        // sample both +x and -x offsets
        vec4 sample1 = texture(DiffuseSampler, texCoord + uvOffset);
        vec4 sample2 = texture(DiffuseSampler, texCoord - uvOffset);

        diffuseSum += (sample1 + sample2) * w;
        weightSum += 2.0 * w;
    }
    fragColor = diffuseSum / weightSum;
}