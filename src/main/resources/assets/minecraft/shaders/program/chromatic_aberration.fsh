#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;

uniform vec3 playerPos;
uniform float effectRadius;

uniform mat4 inverseView;
uniform mat4 inverseProjection;

in vec2 texCoord;
out vec4 fragColor;

float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0; // NDC z
    float near = 0.1;
    float far = 1000.0;
    return (2.0 * near * far) / (far + near - z * (far - near));
}

vec3 reconstructWorldPos(vec2 uv, float depth) {
    float z = depth * 2.0 - 1.0;

    vec4 clipSpacePos = vec4(uv * 2.0 - 1.0, z, 1.0);
    vec4 viewSpacePos = inverseProjection * clipSpacePos;
    viewSpacePos /= viewSpacePos.w;
    vec4 worldPos = inverseView * viewSpacePos;

    return worldPos.xyz;
}

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;

    vec3 worldPos = reconstructWorldPos(texCoord, depth);

    float dist = distance(worldPos, playerPos);

    if (dist < effectRadius) {
        float offset = 0.005;

        vec2 redUV = texCoord + vec2(offset, 0.0);
        vec2 greenUV = texCoord;
        vec2 blueUV = texCoord - vec2(offset, 0.0);

        float r = texture(DiffuseSampler, redUV).r;
        float g = texture(DiffuseSampler, greenUV).g;
        float b = texture(DiffuseSampler, blueUV).b;

        fragColor = vec4(r, g, b, original.a);
    } else {
        fragColor = original;
    }
}
