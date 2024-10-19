#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform vec2 InSize;

in vec2 texCoord;
out vec4 fragColor;

vec4 four_k(vec3 textel, vec2 uv) {
    return (texture(DiffuseSampler, uv + textel.xx) //1 1
    + texture(DiffuseSampler, uv + textel.xy) // 1 -1
    + texture(DiffuseSampler, uv + textel.yx) // -1 1
    + texture(DiffuseSampler, uv + textel.yy)) * 0.25; // -1 -1
}

void main(){
    vec3 textel1 = vec3(1., -1., 0.) / InSize.xyx;
    vec3 textel2 = vec3(1., -1., 0.) / OutSize.xyx;

    vec4 out_colour = (four_k(textel1, texCoord + textel2.yy) // -1 -1
    + four_k(textel1, texCoord + textel2.zy) // 0 -1
    + four_k(textel1, texCoord + textel2.yz) // -1 0
    + four_k(textel1, texCoord)) * 0.25 * 0.125; // 0 0

    out_colour += (four_k(textel1, texCoord + textel2.xy) // 1 -1
    + four_k(textel1, texCoord + textel2.zy) // 0 -1
    + four_k(textel1, texCoord + textel2.xz) // 1 0
    + four_k(textel1, texCoord)) * 0.25 * 0.125; // 0 0

    out_colour += (four_k(textel1, texCoord + textel2.yx) //  -1 1
    + four_k(textel1, texCoord + textel2.yz) // -1 0
    + four_k(textel1, texCoord + textel2.zx) // 0 1
    + four_k(textel1, texCoord)) * 0.25 * 0.125; // 0 0

    out_colour += (four_k(textel1, texCoord + textel2.xx) // 1 1
    + four_k(textel1, texCoord + textel2.xz) // 1 0
    + four_k(textel1, texCoord + textel2.zx) // 0 1
    + four_k(textel1, texCoord)) * 0.25 * 0.125; // 0 0

    out_colour += (four_k(textel1, texCoord + textel1.xx) // 1 1
    + four_k(textel1, texCoord + textel1.xy) // 1 -1
    + four_k(textel1, texCoord + textel1.yx) // -1 1
    + four_k(textel1, texCoord + textel1.yy)) * 0.25 * 0.5; // -1 -1

    fragColor = vec4(out_colour.rgb, 1.);
}
