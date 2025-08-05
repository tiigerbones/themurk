#version 150

uniform sampler2D DiffuseSampler;
uniform float iTime;

in vec2 texCoord;
out vec4 fragColor;

#define SAMPLES 6.0 // Number of samples for smooth blending
#define OFFSET 0.002 // Small offset for subtle fringing
#define CENTER vec2(0.5, 0.5) // Screen center for radial effect

void main() {
    vec2 uv = texCoord;
    vec4 color_sum = vec4(0.0);
    vec4 weight_sum = vec4(0.0);

    // Radial offset direction from pixel to center
    vec2 dir = uv - CENTER;

    // Iterate through samples
    for (float i = 0.0; i <= 1.0; i += 1.0 / SAMPLES) {
        // Radial offset, scaled by i and animated with iTime
        float t = (i - 0.5) * OFFSET * (1.0 + 0.1 * sin(iTime * 0.5));
        vec2 coord = uv + dir * t;
        vec4 color = texture(DiffuseSampler, coord);
        // Weights: R increases, G peaks in middle, B decreases
        vec4 weight = vec4(i, 1.0 - abs(i * 2.0 - 1.0), 1.0 - i, 1.0);
        color_sum += color * weight;
        weight_sum += weight;
    }

    // Compute weighted average
    fragColor = color_sum / weight_sum;
}