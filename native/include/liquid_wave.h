#ifndef LIQUID_WAVE_H
#define LIQUID_WAVE_H

#ifdef __cplusplus
extern "C" {
#endif

/* Builds one period plus its wrap sample. Returns zero for invalid inputs. */
int liquid_wave_fill_sine(float *samples, int period_samples);

#ifdef __cplusplus
}
#endif

#endif
