#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform vec2 InSize;

in vec2 texCoord;

out vec4 fragColor;

#define OUTPUT_SCALE_FACTOR (0.25 * 0.25 * 0.125)

vec2 inTexel = 1.0 / InSize;
vec2 inTexelInv = vec2(-inTexel.x, inTexel.y);

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

vec4 four_k(vec2 uv) {
    vec4 center = texture(DiffuseSampler, uv);          //  1  1
    vec4 tl = texture(DiffuseSampler, uv + inTexel);    //  1  1
    vec4 tr = texture(DiffuseSampler, uv + inTexelInv); // -1  1
    vec4 bl = texture(DiffuseSampler, uv - inTexel);    // -1 -1
    vec4 br = texture(DiffuseSampler, uv - inTexelInv); //  1 -1

    return average(center, tl, tr, bl, br, false);
}

// I really hope GPU drivers are smart enough to optimize most of the repeated lookups away
// - screret

void main() {
    vec4 center = four_k(texCoord + outTexel)    //  1  1
                + four_k(texCoord + outTexelInv) // -1  1
                + four_k(texCoord - outTexelInv) //  1 -1
                + four_k(texCoord - outTexel);   // -1 -1

    vec4 tl = four_k(texCoord + outTexelInv) // -1  1
            + four_k(texCoord - outTexelX)   // -1  0
            + four_k(texCoord + outTexelY)   //  0  1
            + four_k(texCoord);              //  0  0

    vec4 tr = four_k(texCoord + outTexel)    //  1  1
            + four_k(texCoord + outTexelX)   //  1  0
            + four_k(texCoord + outTexelY)   //  0  1
            + four_k(texCoord);              //  0  0

    vec4 bl = four_k(texCoord - outTexel)    // -1 -1
            + four_k(texCoord - outTexelX)   // -1  0
            + four_k(texCoord - outTexelY)   //  0 -1
            + four_k(texCoord);              //  0  0

    vec4 br = four_k(texCoord - outTexelInv) //  1 -1
            + four_k(texCoord - outTexelY)   //  0 -1
            + four_k(texCoord + outTexelX)   //  1  0
            + four_k(texCoord);              //  0  0

    fragColor = average(center, tl, tr, bl, br, true);

    // Optimization: do multiplication in one step at the end
    // They used to be done separately on each addition to fragColor and in four_k
//   fragColor.rgb *= OUTPUT_SCALE_FACTOR;
}
