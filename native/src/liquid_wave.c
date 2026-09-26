#include "liquid_wave.h"

#include <math.h>

int liquid_wave_fill_sine(float *samples, int period_samples) {
    if (samples == 0 || period_samples < 16 || period_samples > 16384) {
        return 0;
    }
    const float step = 6.2831853071795864769f / (float)period_samples;
    for (int i = 0; i < period_samples; ++i) {
        samples[i] = sinf(step * (float)i);
    }
    samples[period_samples] = samples[0];
    return 1;
}
