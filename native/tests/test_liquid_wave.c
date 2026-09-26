#include "liquid_wave.h"

#include <assert.h>
#include <math.h>

int main(void) {
    float samples[1025];
    assert(liquid_wave_fill_sine(0, 1024) == 0);
    assert(liquid_wave_fill_sine(samples, 15) == 0);
    assert(liquid_wave_fill_sine(samples, 1024) == 1);
    assert(fabsf(samples[0]) < 0.000001f);
    assert(fabsf(samples[256] - 1.0f) < 0.000001f);
    assert(fabsf(samples[512]) < 0.000001f);
    assert(fabsf(samples[768] + 1.0f) < 0.000001f);
    assert(samples[1024] == samples[0]);
    return 0;
}
