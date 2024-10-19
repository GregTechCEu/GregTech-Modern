#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform vec2 BlurDir;
uniform float Radius;

in vec2 texCoord;
out vec4 fragColor;

float gaussianPdf(in float x, in float sigma) {
    return 0.39894 * exp( -0.5 * x * x/( sigma * sigma))/sigma;
}

void main(){
    vec2 invSize = 1.0 / OutSize;
    float fSigma = Radius;
    float weightSum = gaussianPdf(0.0, fSigma);
    vec3 diffuseSum = texture(DiffuseSampler, texCoord).rgb * weightSum;
    for( int i = 1; i < int(Radius); i ++) {
        float x = float(i);
        float w = gaussianPdf(x, fSigma);
        vec2 uvOffset = BlurDir * invSize * x;
        vec3 sample1 = texture(DiffuseSampler, texCoord + uvOffset).rgb;
        vec3 sample2 = texture(DiffuseSampler, texCoord - uvOffset).rgb;
        diffuseSum += (sample1 + sample2) * w;
        weightSum += 2.0 * w;
    }
    fragColor = vec4(diffuseSum/weightSum, 1.0);
}
