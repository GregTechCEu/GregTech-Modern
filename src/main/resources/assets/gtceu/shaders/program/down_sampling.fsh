#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform vec2 InSize;

in vec2 texCoord;

out vec4 fragColor;

vec2 inTexel = 1.0 / InSize;
vec2 inTexelNegX = vec2(-inTexel.x, inTexel.y);
vec2 inTexelNegY = vec2(inTexel.x, -inTexel.y);

vec2 outTexel = 1.0 / OutSize;
vec2 outTexelX = vec2(outTexel.x, 0.0);
vec2 outTexelY = vec2(0.0, outTexel.y);
vec2 outTexelNegX = vec2(-outTexel.x, outTexel.y);
vec2 outTexelNegY = vec2(outTexel.x, -outTexel.y);

vec4 four_k(vec2 uv) {
    return 0.25 * (
      texture(DiffuseSampler, uv + inTexel)     //  1  1
    + texture(DiffuseSampler, uv + inTexelNegX) // -1  1
    + texture(DiffuseSampler, uv + inTexelNegY) //  1 -1
    + texture(DiffuseSampler, uv - inTexel));   // -1 -1
}

void main() {
    fragColor = 0.25 * 0.125 * (
      four_k(texCoord - outTexel)  // -1 -1
    + four_k(texCoord - outTexelX) // -1  0
    + four_k(texCoord - outTexelY) //  0 -1
    + four_k(texCoord));           //  0  0

    fragColor += 0.25 * 0.125 * (
      four_k(texCoord + outTexelNegY) //  1 -1
    + four_k(texCoord - outTexelY)    //  0 -1
    + four_k(texCoord + outTexelX)    //  1  0
    + four_k(texCoord));              //  0  0

    fragColor += 0.25 * 0.125 * (
      four_k(texCoord + outTexelNegX) // -1  1
    + four_k(texCoord - outTexelX)    // -1  0
    + four_k(texCoord + outTexelY)    //  0  1
    + four_k(texCoord));              //  0  0

    fragColor += 0.25 * 0.125 * (
      four_k(texCoord + outTexel)  //  1  1
    + four_k(texCoord + outTexelX) //  1  0
    + four_k(texCoord + outTexelY) //  0  1
    + four_k(texCoord));           //  0  0

    fragColor += 0.25 * 0.125 * (
      four_k(texCoord + outTexel)     //  1  1
    + four_k(texCoord + outTexelNegX) // -1  1
    + four_k(texCoord + outTexelNegY) //  1 -1
    + four_k(texCoord - outTexel));   // -1 -1
}
