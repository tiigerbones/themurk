#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 original = texture(DiffuseSampler, texCoord);
    vec3 tintColor = vec3(1.0, 0.0, 1.0); // Pink tint
    fragColor = original * vec4(tintColor, 1.0);
}