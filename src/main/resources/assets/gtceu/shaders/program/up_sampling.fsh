#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DownTexture;
uniform vec2 OutSize;

in vec2 texCoord;

out vec4 fragColor;

vec2 outTexel = 1.0 / OutSize;
vec2 outTexelX = vec2(outTexel.x, 0.0);
vec2 outTexelY = vec2(0.0, outTexel.y);
vec2 outTexelNegX = vec2(-outTexel.x, outTexel.y);
vec2 outTexelNegY = vec2(outTexel.x, -outTexel.y);

void main() {
    vec4 out_color = texture(DiffuseSampler, texCoord) * 4.0;         //  0  0

    out_color += texture(DiffuseSampler, texCoord + outTexel);        //  1  1
    out_color += texture(DiffuseSampler, texCoord + outTexelNegX);    // -1  1
    out_color += texture(DiffuseSampler, texCoord + outTexelNegY);    //  1 -1
    out_color += texture(DiffuseSampler, texCoord - outTexel);        // -1 -1

    out_color += texture(DiffuseSampler, texCoord + outTexelX) * 2.0; //  1  0
    out_color += texture(DiffuseSampler, texCoord - outTexelX) * 2.0; // -1  0
    out_color += texture(DiffuseSampler, texCoord + outTexelY) * 2.0; //  0  1
    out_color += texture(DiffuseSampler, texCoord - outTexelY) * 2.0; //  0 -1

    vec4 total = out_color * 0.8 / 16.0 + texture(DownTexture, texCoord) * 0.8;
    total = clamp(total, 0.0, 1.0);
    fragColor = total;
}
