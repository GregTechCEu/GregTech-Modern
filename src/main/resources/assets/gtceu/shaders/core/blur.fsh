#version 120

varying vec2 texCoord0;

uniform sampler2D DiffuseSampler;
uniform vec2 u_resolution;
uniform vec2 blurDir;

void main(void){
    vec2 pixelSize = blurDir.xy / u_resolution.xy;
    vec4 out_colour = vec4(0.0);

    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * -5.) * 0.0093;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * -4.) * 0.028002;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * -3.) * 0.065984;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * -2.) * 0.121703;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * -1.) * 0.175713;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * 0.) * 0.198596;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * 1.) * 0.175713;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * 2.) * 0.121703;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * 3.) * 0.065984;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * 4.) * 0.028002;
    out_colour += texture2D(DiffuseSampler, texCoord0 + pixelSize * 5.) * 0.0093;
    gl_FragColor = vec4(out_colour.rgb, 1.);
}
