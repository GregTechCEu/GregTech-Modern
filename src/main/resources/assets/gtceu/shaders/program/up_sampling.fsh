#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DownTexture;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

vec4 four_k(vec3 textel, vec2 uv) {
    return (texture(DiffuseSampler, uv + textel.xx) //1 1
    + texture(DiffuseSampler, uv + textel.xy) // 1 -1
    + texture(DiffuseSampler, uv + textel.yx) // -1 1
    + texture(DiffuseSampler, uv + textel.yy)) * 0.25; // -1 -1
}

vec4 up_sampling(vec3 textel, vec2 uv) {
    return vec4(four_k(textel, uv).rgb + texture(DownTexture, uv).rgb, 1.);
}

void main(){
    vec3 textel = vec3(1., -1., 0.) / OutSize.xyx;
    //    out_colour = up_sampling(textel, texCoord);

    vec4 out_colour = texture(DiffuseSampler, texCoord + textel.xx); // 1 1
    out_colour += texture(DiffuseSampler, texCoord + textel.xz) * 2.0; // 1 0
    out_colour += texture(DiffuseSampler, texCoord + textel.xy); // 1 -1
    out_colour += texture(DiffuseSampler, texCoord + textel.yz) * 2.0; // -1 0
    out_colour += texture(DiffuseSampler, texCoord) * 4.0; // 0 0
    out_colour += texture(DiffuseSampler, texCoord + textel.zx) * 2.0; // 0 1
    out_colour += texture(DiffuseSampler, texCoord + textel.yy); // -1 -1
    out_colour += texture(DiffuseSampler, texCoord + textel.zy) * 2.0; // 0 -1
    out_colour += texture(DiffuseSampler, texCoord + textel.yx); // -1 1

    fragColor = vec4(out_colour.rgb * 0.8 / 16. + texture(DownTexture, texCoord).rgb * 0.8, 1.);
}
