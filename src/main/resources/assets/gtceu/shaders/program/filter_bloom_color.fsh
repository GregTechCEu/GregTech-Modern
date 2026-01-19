#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform bool EnableFilter;

// from GameRenderer.PROJECTION_Z_NEAR
uniform float DepthNear = 0.05;
// from GameRenderer#getDepthFar
uniform float DepthFar = 482.0;

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
        // clear bloom color fragment if the main buffer's depth isn't the same as the bloom buffer's depth
        if (abs(mainDepth - diffuseDepth) > 0.01) {
            fragColor = vec4(0.0);
        } else if (distance((mainColor.rgb * fragColor.a), fragColor.rgb) > 0.05) {
            // also clear it if the main buffer's color is off by too much
            fragColor = vec4(0.0);
        }
    }
}
