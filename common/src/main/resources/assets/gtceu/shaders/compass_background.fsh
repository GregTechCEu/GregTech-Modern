#version 150

uniform vec2 iResolution;
uniform float iTime;
uniform vec2 iMouse;

uniform vec4 baseTexture;
uniform vec4 topTexture;
uniform vec4 sideTexture;
uniform vec4 frontTexture;
uniform vec4 backTexture;

uniform sampler2D BLOCK_ATLAS;

in vec2 texCoord;

out vec4 fragColor;

const float pi = 3.14159;

mat3 xrot(float t)
{
    return mat3(1.0, 0.0, 0.0,
    0.0, cos(t), -sin(t),
    0.0, sin(t), cos(t));
}

mat3 yrot(float t)
{
    return mat3(cos(t), 0.0, -sin(t),
    0.0, 1.0, 0.0,
    sin(t), 0.0, cos(t));
}

mat3 zrot(float t)
{
    return mat3(cos(t), -sin(t), 0.0,
    sin(t), cos(t), 0.0,
    0.0, 0.0, 1.0);
}

float sphereDistance(vec3 pos)
{
    return length(pos) - 0.6;
}

float sdBox( vec3 p, vec3 b )
{
    vec3 d = abs(p) - b;
    return min(max(d.x,max(d.y,d.z)),0.0) +
    length(max(d,0.0));
}

float planeDistance(vec3 pos)
{
    vec3 origin = vec3(0.0, -2, 0.0);
    vec3 normal = vec3(0.0, 1.0, 0.0);
    vec3 delta = pos - origin;
    float prod = dot(delta, normal);
    return prod;
}


float cubeSDF(vec3 p) {
    // If d.x < 0, then -1 < p.x < 1, and same logic applies to p.y, p.z
    // So if all components of d are negative, then p is inside the unit cube
    vec3 d = abs(p) - vec3(0.4,0.4,0.4);

    // Assuming p is inside the cube, how far is it from the surface?
    // Result will be negative or zero.
    float insideDistance = min(max(d.x, max(d.y, d.z)), 0.0);

    // Assuming p is outside the cube, how far is it from the surface?
    // Result will be positive or zero.
    float outsideDistance = length(max(d, 0.0));

    return insideDistance + outsideDistance;
}

float map(vec3 pos)
{

    vec3 rpos = (pos - vec3(0.0,0.5,0.0));
    rpos *= yrot(pi*0.25+iTime * .5);
    return min(cubeSDF(rpos), planeDistance(pos));
}

vec3 normal(vec3 p)
{
    vec3 o = vec3(0.01, 0.0, 0.0);
    vec3 n = vec3(0.0);
    n.x = map(p+o) - map(p-o);
    n.y = map(p+o.zxy) - map(p-o.zyx);
    n.z = map(p+o.yzx) - map(p-o.yzx);
    return normalize(n);
}

float trace(vec3 o, vec3 r)
{
    float t = 0.0;
    for (int i = 0; i < 32; ++i) {
        vec3 pos = o + r * t;
        float d = map(pos);
        if (d < 0.001) {
            return t;
        }
        t += d;
    }
    return t;
}

vec2 ltrace(vec3 o, vec3 r)
{
    /* http://iquilezles.org/www/articles/rmshadows/rmshadows.htm */
    float t = 0.0;
    float md = 1000.0;
    float lt = 0.0;
    for (int i = 0; i < 32; ++i) {
        vec3 pos = o + r * t;
        float d = map(pos);
        md = min(md, 16.0*d/t);
        t += min(d, 0.1); /* <-- you need to clamp the distance for it to work :) */
    }
    return vec2(t,clamp(md,0.0,1.0));
}

float light(vec3 world, vec3 sn, vec3 lpos)
{
    vec3 ldel = world + sn * 0.01 - lpos;
    float ldist = length(ldel);
    ldel /= ldist;
    vec2 lt = ltrace(lpos, ldel);
    float lm = 1.0;
    if (lt.x < ldist) {
        lm = lt.y;
    }
    float lp = max(dot(ldel, -sn), 0.0);
    float fl = lp * lm / (1.0 + ldist * ldist * 0.1);
    return fl;
}

vec2 uvMapping(vec4 uvs, vec2 uv) {
    // uvs(u0, v0, u1, v1)
    return vec2(uvs.x + (uvs.z - uvs.x) * uv.x, uvs.y + (uvs.w - uvs.y) * uv.y);
}

void mainImage( out vec4 fragColor, in vec2 fragCoord ) {
    vec2 uv = fragCoord.xy / iResolution.xy * 2.0 - 1.0;
    uv.x *= iResolution.x / iResolution.y;

    vec3 ray = normalize(vec3(uv, 1.3));
    ray *= xrot(pi*0.27);

    mat3 rotr = mat3(1.0);//yrot(iTime);
    vec2 mp = iMouse.xy / iResolution.xy;
    // mp.y += 0.5;
    rotr = xrot(-mp.y) * yrot(-mp.x*3.0);

    ray *= rotr;
    vec3 origin = vec3(0.0, 0.0, -1.4) * rotr;
    origin.y += 2.0;

    float t = trace(origin, ray);
    vec3 world = origin + ray * t;

    vec3 rpos = (world - vec3(0.0,0.5,0.0));
    rpos *= yrot(pi*0.25+iTime * .5);
    float cubeD = cubeSDF(rpos);

    vec3 bg = vec3(0.1);

    if(cubeD < 0.1) {
        float dx = min(abs(rpos.x - 0.4), abs(rpos.x + 0.4));
        float dy = min(abs(rpos.y - 0.4), abs(rpos.y + 0.4));
        float dz = min(abs(rpos.z - 0.4), abs(rpos.z + 0.4));

        if (rpos.y >= 0.4) { // top
            vec2 _uv = vec2((rpos.x + 0.4) / 0.8, (rpos.z + 0.4) / 0.8);
            bg = texture(BLOCK_ATLAS, uvMapping(baseTexture, _uv)).rgb;
            vec4 overlay = texture(BLOCK_ATLAS, uvMapping(topTexture, _uv));
            bg = mix(bg, overlay.rgb, overlay.a);
        } else if (abs(rpos.x) >= 0.4) { // side
            vec2 _uv = vec2((rpos.z + 0.4) / 0.8, (rpos.y + 0.4) / 0.8);
            bg = texture(BLOCK_ATLAS, uvMapping(baseTexture, _uv)).rgb;
            vec4 overlay = texture(BLOCK_ATLAS, uvMapping(sideTexture, _uv));
            bg = mix(bg, overlay.rgb, overlay.a);
        } else if (rpos.z >= 0.4) { // back
            vec2 _uv = vec2((rpos.x + 0.4) / 0.8, (rpos.y + 0.4) / 0.8);
            bg = texture(BLOCK_ATLAS, uvMapping(baseTexture, _uv)).rgb;
            vec4 overlay = texture(BLOCK_ATLAS, uvMapping(backTexture, _uv));
            bg = mix(bg, overlay.rgb, overlay.a);
        } else { // front
            vec2 _uv = vec2(1. - (rpos.x + 0.4) / 0.8, (rpos.y + 0.4) / 0.8);
            bg = texture(BLOCK_ATLAS, uvMapping(baseTexture, _uv)).rgb;
            vec4 overlay = texture(BLOCK_ATLAS, uvMapping(frontTexture, _uv));
            bg = mix(bg, overlay.rgb, overlay.a);
        }
    }

    vec3 sn = normal(world);
    float fd = map(world);

    float la = light(world, sn, vec3(-2.0, 1.5, 0.0));
    float lb = light(world, sn, vec3(2.0, 1.5, 0.0));


    float fog = 1.0 / (1.0 + t * t * 0.01 + fd * 5.0);

    vec3 diff = bg;
    float dp = max(dot(ray,-sn),0.0);

    vec3 rc = diff * dp;
    rc += la * abs(sin(vec3(1.0, 0.5, 0.3) + iTime * 0.2) - 0.2);
    rc += lb * abs(sin(vec3(0.0, 0.5, 0.7) + iTime * 0.2) + 0.2);

    rc *= fog;
    fragColor = vec4(rc, 1.0);
}

void main() {
    mainImage(fragColor, vec2(texCoord.x * iResolution.x, texCoord.y * iResolution.y));
}
