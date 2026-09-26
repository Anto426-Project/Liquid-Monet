#include "liquid_spring.h"

#include <math.h>

int liquid_spring_step(float *channels, int channel_count, float seconds) {
    if (channels == 0 || channel_count < 1 || channel_count > 64 ||
        !isfinite(seconds) || seconds < 0.0f) {
        return -1;
    }
    /* Validate the complete batch before writing any channel. */
    for (int i = 0; i < channel_count; ++i) {
        const float *c = channels + i * LIQUID_SPRING_STRIDE;
        for (int j = 0; j < LIQUID_SPRING_STRIDE; ++j) {
            if (!isfinite(c[j])) return -1;
        }
        if (c[3] >= 0.0f || c[5] <= 0.0f ||
            (c[6] != 0.0f && c[6] != 1.0f && c[6] != 2.0f && c[6] != 3.0f) ||
            (c[6] == 0.0f && c[4] <= 0.0f) ||
            (c[6] == 2.0f && (c[4] >= c[3] || c[4] >= 0.0f))) {
            return -1;
        }
    }
    int moving = 0;
    const double t = seconds;
    for (int i = 0; i < channel_count; ++i) {
        float *c = channels + i * LIQUID_SPRING_STRIDE;
        const double y = (double)c[0] - c[2];
        const double v = c[1];
        const double a = c[3];
        const double b = c[4];
        double next_y, next_v;
        if (c[6] == 3.0f || (fabs(y) <= c[5] && fabs(v) <= c[5] * 62.5)) {
            c[0] = c[2];
            c[1] = 0.0f;
            continue;
        }
        if (c[6] == 0.0f) {
            const double e = exp(a * t);
            const double s = sin(b * t);
            const double cosine = cos(b * t);
            const double q = (v - a * y) / b;
            next_y = e * (y * cosine + q * s);
            next_v = a * next_y + e * (-y * b * s + q * b * cosine);
        } else if (c[6] == 1.0f) {
            const double e = exp(a * t);
            const double q = v - a * y;
            next_y = (y + q * t) * e;
            next_v = q * e + a * next_y;
        } else {
            const double q = (v - b * y) / (a - b);
            const double first = q * exp(a * t);
            const double second = (y - q) * exp(b * t);
            next_y = first + second;
            next_v = a * first + b * second;
        }
        if (fabs(next_y) <= c[5] && fabs(next_v) <= c[5] * 62.5) {
            c[0] = c[2];
            c[1] = 0.0f;
        } else {
            c[0] = (float)(c[2] + next_y);
            c[1] = (float)next_v;
            ++moving;
        }
    }
    return moving;
}
