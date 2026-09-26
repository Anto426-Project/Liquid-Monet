#ifndef LIQUID_SPRING_H
#define LIQUID_SPRING_H

#ifdef __cplusplus
extern "C" {
#endif

/* Channels: position, velocity, target, rate_a, rate_b, threshold, mode.
 * Modes: 0 underdamped, 1 critical, 2 overdamped, 3 snap.
 * Coefficients are prepared only when the motion policy changes.
 * Returns the number of moving channels, or -1 for invalid input.
 */
#define LIQUID_SPRING_STRIDE 7
int liquid_spring_step(float *channels, int channel_count, float seconds);

#ifdef __cplusplus
}
#endif
#endif
