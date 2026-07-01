#version 150

#define MAX_DEPTH_DIFFERENCE 1.0e-5

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D MainDepthSampler;

// these should be #defines, but adding those dynamically doesn't exist in vanilla MC until 26.1.
// GameRenderer.PROJECTION_Z_NEAR
uniform float DepthNear;
// GameRenderer#getDepthFar
uniform float DepthFar;

in vec2 texCoord;

out vec4 fragColor;

float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0; // back to NDC
    return (2.0 * DepthNear * DepthFar) / (DepthFar + DepthNear - z * (DepthFar - DepthNear));
}

void main() {
    // calculate linear depth
    float mainDepth = linearizeDepth(texture(MainDepthSampler, texCoord).r);
    float diffuseDepth = linearizeDepth(texture(DiffuseDepthSampler, texCoord).r);
    // clear bloom color fragment if the main sampler's depth isn't the same as the bloom sampler's depth
    if (abs(mainDepth - diffuseDepth) > MAX_DEPTH_DIFFERENCE) {
        fragColor = vec4(0.0);
        //discard;
    } else {
        fragColor = texture(DiffuseSampler, texCoord);
    }
}
