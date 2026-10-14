#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DownTexture;
uniform vec2 OutSize;

in vec2 texCoord;

out vec4 fragColor;

vec2 outTexel = 1.0 / OutSize;
vec2 outTexelX = vec2(outTexel.x, 0.0);
vec2 outTexelY = vec2(0.0, outTexel.y);
vec2 outTexelInv = vec2(-outTexel.x, outTexel.y);

vec4 average(vec4 center, vec4 left, vec4 right, vec4 up, vec4 down, bool accountCenterColor) {
    float totalAlpha = abs(center.a - left.a) + abs(center.a - right.a) + abs(center.a - up.a) + abs(center.a - down.a);
    vec3 totalColor = (left.rgb * left.a)
                    + (right.rgb * right.a)
                    + (up.rgb * up.a)
                    + (down.rgb * down.a)
                    + (center.rgb * center.a * float(accountCenterColor));

    return vec4(totalColor, totalAlpha);
}

void main() {
    vec4 center = texture(DiffuseSampler, texCoord) * 4.0; //  0  0

    vec4 tr = texture(DiffuseSampler, texCoord + outTexel);    //  1  1
    vec4 tl = texture(DiffuseSampler, texCoord - outTexelInv); //  1 -1
    vec4 br = texture(DiffuseSampler, texCoord + outTexelInv); // -1  1
    vec4 bl = texture(DiffuseSampler, texCoord - outTexel);    // -1 -1

    vec4 right = texture(DiffuseSampler, texCoord + outTexelX) * 2.0; //  1  0
    vec4 left  = texture(DiffuseSampler, texCoord - outTexelX) * 2.0; // -1  0
    vec4 up    = texture(DiffuseSampler, texCoord + outTexelY) * 2.0; //  0  1
    vec4 down  = texture(DiffuseSampler, texCoord - outTexelY) * 2.0; //  0 -1

    vec4 colorSum = average(center, left, right, up, down, true);
    colorSum += average(center, tl, tr, bl, br, false);

    colorSum = (colorSum / 16.0 + texture(DownTexture, texCoord)) * 0.8;

    fragColor = clamp(colorSum, 0.0, 1.0);
}
