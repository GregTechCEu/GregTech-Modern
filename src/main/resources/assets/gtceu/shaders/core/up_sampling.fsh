#version 150

out vec2 texCoord0;

uniform sampler2D upTexture;
uniform sampler2D downTexture;
uniform vec2 u_resolution;
uniform vec2 u_resolution2;

vec4 four_k(vec3 textel, vec2 uv) {
    return (texture2D(upTexture, uv + textel.xx) //1 1
    + texture2D(upTexture, uv + textel.xy) // 1 -1
    + texture2D(upTexture, uv + textel.yx) // -1 1
    + texture2D(upTexture, uv + textel.yy)) * 0.25; // -1 -1
}

vec4 up_sampling(vec3 textel, vec2 uv) {
    return vec4(four_k(textel, uv).rgb + texture2D(downTexture, uv).rgb, 1.);
}

out vec4 fragColor;

void main() {
    vec3 textel = vec3(1., -1., 0.) / u_resolution.xyx;
//    out_colour = up_sampling(textel, texCoord0);

    vec4 out_colour = texture2D(upTexture, texCoord0 + textel.xx);
    out_colour += texture2D(upTexture, texCoord0 + textel.xz) * 2.0;
    out_colour += texture2D(upTexture, texCoord0 + textel.xy);
    out_colour += texture2D(upTexture, texCoord0 + textel.yz) * 2.0;
    out_colour += texture2D(upTexture, texCoord0) * 4.0;
    out_colour += texture2D(upTexture, texCoord0 + textel.zx) * 2.0;
    out_colour += texture2D(upTexture, texCoord0 + textel.yy);
    out_colour += texture2D(upTexture, texCoord0 + textel.zy) * 2.0;
    out_colour += texture2D(upTexture, texCoord0 + textel.yx);

    fragColor = vec4(out_colour.rgb * 0.8 / 16. + texture2D(downTexture, texCoord0).rgb * 0.8, 1.);
}
