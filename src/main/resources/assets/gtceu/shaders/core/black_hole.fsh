#version 140

#define AA 2  //change to 1 to increase performance
#define _Speed 3.0  //disk rotation speed
#define _Steps  12. //disk texture layers
#define _Size 0.3 //size of BH

uniform float u_time;
uniform vec2 u_resolution;
uniform vec3 eye;
uniform vec3 target;
uniform sampler2D texture;

float hash(float x){ return fract(sin(x)*152754.742);}
float hash(vec2 x){	return hash(x.x + hash(x.y));}

float value(vec2 p, float f) //value noise
{
    float bl = hash(floor(p*f + vec2(0.,0.)));
    float br = hash(floor(p*f + vec2(1.,0.)));
    float tl = hash(floor(p*f + vec2(0.,1.)));
    float tr = hash(floor(p*f + vec2(1.,1.)));

    vec2 fr = fract(p*f);
    fr = (3. - 2.*fr)*fr*fr;
    float b = mix(bl, br, fr.x);
    float t = mix(tl, tr, fr.x);
    return  mix(b,t, fr.y);
}

vec3 background(vec2 fragCoord, float r)
{
    vec2 uv = fragCoord.xy / u_resolution.xy;
    vec2 lpos = u_resolution.xy / 2. / u_resolution.x;
    vec2 texC2 = fragCoord.xy / u_resolution.x;
    vec2 texC = mix(uv, lpos, (20. * r / (distance((texC2 * 2.0 - lpos * 2.0) * 5. + lpos, lpos) - r))); //Black hole shader
    vec3 getColor = texture2D(texture,texC).rgb;
    return getColor;
}

vec4 raymarchDisk(vec3 ray, vec3 zeroPos)
{
    //return vec4(1.,1.,1.,0.); //no disk

    vec3 position = zeroPos;
    float lengthPos = length(position.xz);
    float dist = min(1., lengthPos*(1./_Size) *0.5) * _Size * 0.4 *(1./_Steps) /( abs(ray.y) );

    position += dist*_Steps*ray*0.5;

    vec2 deltaPos;
    deltaPos.x = -zeroPos.z*0.01 + zeroPos.x;
    deltaPos.y = zeroPos.x*0.01 + zeroPos.z;
    deltaPos = normalize(deltaPos - zeroPos.xz);

    float parallel = dot(ray.xz, deltaPos);
    parallel /= sqrt(lengthPos);
    parallel *= 0.5;
    float redShift = parallel +0.3;
    redShift *= redShift;

    redShift = clamp(redShift, 0., 1.);

    float disMix = clamp((lengthPos - _Size * 2.)*(1./_Size)*0.24, 0., 1.);
    vec3 insideCol =  mix(vec3(1.0,0.8,0.0), vec3(0.5,0.13,0.02)*0.2, disMix);

    insideCol *= mix(vec3(0.4, 0.2, 0.1), vec3(1.6, 2.4, 4.0), redShift);
    insideCol *= 1.25;
    redShift += 0.12;
    redShift *= redShift;

    vec4 o = vec4(0.);

    for(float i = 0. ; i < _Steps; i++)
    {
        position -= dist * ray ;

        float intensity =clamp( 1. - abs((i - 0.8) * (1./_Steps) * 2.), 0., 1.);
        float lengthPos = length(position.xz);
        float distMult = 1.;

        distMult *=  clamp((lengthPos -  _Size * 0.75) * (1./_Size) * 1.5, 0., 1.);
        distMult *= clamp(( _Size * 10. -lengthPos) * (1./_Size) * 0.20, 0., 1.);
        distMult *= distMult;

        float u = lengthPos + u_time* _Size*0.3 + intensity * _Size * 0.2;

        vec2 xy ;
        float rot = mod(u_time*_Speed, 8192.);
        xy.x = -position.z*sin(rot) + position.x*cos(rot);
        xy.y = position.x*sin(rot) + position.z*cos(rot);

        float x = abs( xy.x/(xy.y));
        float angle = 0.02*atan(x);

        const float f = 70.;
        float noise = value( vec2( angle, u * (1./_Size) * 0.05), f);
        noise = noise*0.66 + 0.33*value( vec2( angle, u * (1./_Size) * 0.05), f*2.);

        float extraWidth =  noise * 1. * (1. -  clamp(i * (1./_Steps)*2. - 1., 0., 1.));

        float alpha = clamp(noise*(intensity + extraWidth)*( (1./_Size) * 10.  + 0.01 ) *  dist * distMult , 0., 1.);

        vec3 col = 2.*mix(vec3(0.3,0.2,0.15)*insideCol, insideCol, min(1.,intensity*2.));
        o = clamp(vec4(col*alpha + o.rgb*(1.-alpha), o.a*(1.-alpha) + alpha), vec4(0.), vec4(1.));

        lengthPos *= (1./_Size);

        o.rgb+= redShift*(intensity*1. + 0.5)* (1./_Steps) * 100.*distMult/(lengthPos*lengthPos);
    }

    o.rgb = clamp(o.rgb - 0.005, 0., 1.);
    return o ;
}


mat4 viewMatrix(vec3 eye, vec3 center, vec3 up) {
    // Based on gluLookAt man page
    vec3 f = normalize(center - eye);
    vec3 s = normalize(cross(f, up));
    vec3 u = cross(s, f);
    return mat4(
    vec4(s, 0.0),
    vec4(u, 0.0),
    vec4(-f, 0.0),
    vec4(0.0, 0.0, 0.0, 1)
    );
}

vec3 rayDirection(float fieldOfView, vec2 size, vec2 fragCoord) {
    vec2 xy = fragCoord - size / 2.0;
    float z = size.y / tan(radians(fieldOfView) / 2.0);
    return normalize(vec3(xy, -z));
}

void main()
{
    gl_FragColor = vec4(0.);;

    vec2 fragCoordRot;
    fragCoordRot.x = gl_FragCoord.x*1.0;
    fragCoordRot.y = gl_FragCoord.y*1.0;

    for( int j=0; j<AA; j++ )
    for( int i=0; i<AA; i++ )
    {
        //setting up camera
        vec3 viewDir = rayDirection(45.0, u_resolution.xy, gl_FragCoord.xy);
        vec3 pos = eye;
        float r = distance(pos, target);

        mat4 viewToWorld = viewMatrix(pos, target, vec3(0.0,1.0,0.0));

        vec3 ray = (viewToWorld * vec4(viewDir, 0.0)).xyz;
        vec3 ray2 = vec3(gl_FragCoord.xy / u_resolution.xy, 0.);

        vec4 col = vec4(0.);
        vec4 glow = vec4(0.);
        vec4 outCol =vec4(100.);

        for(int disks = 0; disks< 20; disks++) //steps
        {

            for (int h = 0; h < 6; h++) //reduces tests for exit conditions (to minimise branching)
            {
                float dotpos = dot(pos,pos);
                float invDist = inversesqrt(dotpos); //1/distance to BH
                float centDist = dotpos * invDist; 	//distance to BH
                float stepDist = 0.92 * abs(pos.y /(ray.y));  //conservative distance to disk (y==0)
                float farLimit = centDist * 0.5; //limit step size far from to BH
                float closeLimit = centDist * 0.1 + 0.05 * centDist*centDist*(1./_Size); //limit step size closse to BH
                stepDist = min(stepDist, min(farLimit, closeLimit));
                float invDistSqr = invDist * invDist;
                float bendForce = stepDist * invDistSqr * _Size * 0.625;  //bending force
                ray =  normalize(ray - (bendForce * invDist )*pos);  //bend ray towards BH
                ray2=  normalize(ray2 - (bendForce * invDist )*pos);  //bend ray towards BH
                pos += stepDist * ray;

                glow += vec4(1.2,1.1,1, 1.0) *(0.01*stepDist * invDistSqr * invDistSqr *clamp( centDist*(2.) - 1.2,0.,1.)); //adds fairly cheap glow
            }

            float dist2 = length(pos);

            if(dist2 < _Size * 0.1) //ray sucked in to BH
            {
                outCol =  vec4( col.rgb * col.a + glow.rgb *(1.-col.a ) ,1.) ;
                break;
            }

            else if(dist2 > _Size * 1000.) //ray escaped BH
            {
                vec3 bg = background (gl_FragCoord.xy, 1./r);
                outCol = vec4(col.rgb*col.a + bg.rgb*(1.-col.a)  + glow.rgb *(1.-col.a), 1.);
                break;
            }

            else if (abs(pos.y) <= _Size * 0.002 ) //ray hit accretion disk
            {
                vec4 diskCol = raymarchDisk(ray, pos);   //render disk
                pos.y = 0.;
                pos += abs(_Size * 0.001 /ray.y) * ray;
                col = vec4(diskCol.rgb*(1.-col.a) + col.rgb, col.a + diskCol.a*(1.-col.a));
            }
        }

        //if the ray never escaped or got sucked in
        if(outCol.r == 100.)
        outCol = vec4(col.rgb + glow.rgb *(col.a +  glow.a) , 1.);

        col = outCol;
        col.rgb =  pow( col.rgb, vec3(0.6) );

        gl_FragColor += col/float(AA*AA);
    }
}
