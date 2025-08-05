#version 150

uniform sampler2D DiffuseSampler;
uniform float iTime;

in vec2 texCoord;
out vec4 fragColor;

#define SAMPLES 10.0 // Number of samples for smooth blending
#define OFFSET vec2(0.002, 0.002) // Small offset for subtle fringing

void main() {
    vec2 uv = texCoord;
    vec4 color_sum = vec4(0.0);
    vec4 weight_sum = vec4(0.0);

    // Iterate through samples for smooth blending
    for (float i = 0.0; i <= 1.0; i += 1.0 / SAMPLES) {
        // Linear offset from -0.5 to +0.5, modulated by time for animation
        vec2 coord = uv + (i - 0.5) * OFFSET * (1.0 + 0.1 * sin(iTime));
        vec4 color = texture(DiffuseSampler, coord);
        // Weights: R from 0 to 1, G peaks in middle, B from 1 to 0
        vec4 weight = vec4(i, 1.0 - abs(i * 2.0 - 1.0), 1.0 - i, 1.0);
        color_sum += color * weight;
        weight_sum += weight;
    }

    // Compute weighted average
    fragColor = color_sum / weight_sum;
}