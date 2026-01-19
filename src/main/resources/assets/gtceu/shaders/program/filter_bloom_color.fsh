#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DiffuseDepthSampler;
uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform bool EnableFilter;

// from GameRenderer.PROJECTION_Z_NEAR
uniform float DepthNear = 0.05;
// from GameRenderer#getDepthFar
uniform float DepthFar = 32.0;

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
        // clear bloom color fragment if the main buffer's color is off by too much
        if (distance((mainColor.rgb * fragColor.a), fragColor.rgb) > 0.05) {
            fragColor = vec4(0.0);
        } else {
            // calculate linear depth
            float mainDepth = linearizeDepth(texture(MainDepthSampler, texCoord).r);
            float diffuseDepth = linearizeDepth(texture(DiffuseDepthSampler, texCoord).r);
            // also clear if the main buffer's depth isn't the same as the bloom buffer's depth
            if (abs(mainDepth - diffuseDepth) > 0.001) {
                fragColor = vec4(0.0);
            }
        }
    }
}
