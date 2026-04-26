#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform bool EnableFilter;

// GameRenderer.PROJECTION_Z_NEAR
uniform float DepthNear = 0.05;
// GameRenderer#getDepthFar; 8 chunk render distance -> 8 * 16 * 4
uniform float DepthFar = 512.0;

in vec2 texCoord;

out vec4 fragColor;

float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0; // back to NDC
    return (2.0 * DepthNear * DepthFar) / (DepthFar + DepthNear - z * (DepthFar - DepthNear));
}

void main() {
    fragColor = texture(DiffuseSampler, texCoord);
    if (EnableFilter) {
        vec4 mainColor = texture(MainSampler, texCoord);

        // calculate linear depth
        float mainDepth = linearizeDepth(texture(MainDepthSampler, texCoord).r);
        float diffuseDepth = linearizeDepth(texture(DiffuseDepthSampler, texCoord).r);
        // clear bloom color fragment if the main sampler's depth isn't the same as the bloom sampler's depth
        if (abs(mainDepth - diffuseDepth) > 0.01) {
            fragColor = vec4(0.0);
        }
    }
}
