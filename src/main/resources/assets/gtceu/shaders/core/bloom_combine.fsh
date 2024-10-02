#version 120

varying vec2 texCoord0;


uniform sampler2D buffer_a;
uniform sampler2D buffer_b;
uniform float intensive;
uniform float base;
uniform float threshold_up;
uniform float threshold_down;

// All components are in the range [0…1], including hue.
//vec3 rgb2hsv(vec3 c) {
//    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
//    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
//    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
//
//    float d = q.x - min(q.w, q.y);
//    float e = 1.0e-10;
//    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
//}

void main(void){
    vec3 bloom = texture2D(buffer_b, texCoord0).rgb * intensive;
    vec3 background = texture2D(buffer_a, texCoord0).rgb;
//    gl_FragColor = vec4(background + bloom * ((1 - rgb2hsv(background).z) * (threshold_up - threshold_down) + threshold_down + base), 1.);
    float max = max(background.b, max(background.r, background.g));
    float min = min(background.b, min(background.r, background.g));
    gl_FragColor = vec4(background + bloom * ((1. - (max + min) / 2.) * (threshold_up - threshold_down) + threshold_down + base), 1.);
}
