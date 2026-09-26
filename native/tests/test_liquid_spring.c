#include "liquid_spring.h"
#include <assert.h>
#include <math.h>
#include <string.h>

int main(void) {
    float critical[] = {0, 0, 1, -10, 0, 0.000001f, 1};
    assert(liquid_spring_step(critical, 1, 0.1f) == 1);
    assert(fabs(critical[0] - (1.0 - 2.0 * exp(-1.0))) < 0.000001);
    assert(fabs(critical[1] - 10.0 * exp(-1.0)) < 0.000001);

    /* Exact integration must be independent of display refresh rate in all three regimes. */
    float once[] = {1, 2, -1, -3, 4, 0.000001f, 0,
                    1, 2, -1, -5, 0, 0.000001f, 1,
                    1, 2, -1, -2, -8, 0.000001f, 2};
    float split[21];
    memcpy(split, once, sizeof(once));
    assert(liquid_spring_step(once, 3, 0.1f) == 3);
    for (int frame = 0; frame < 12; ++frame) {
        assert(liquid_spring_step(split, 3, 0.1f / 12.0f) == 3);
    }
    for (int i = 0; i < 3; ++i) {
        assert(fabsf(once[i * 7] - split[i * 7]) < 0.00001f);
        assert(fabsf(once[i * 7 + 1] - split[i * 7 + 1]) < 0.00001f);
    }
    assert(liquid_spring_step(split, 3, 60) == 0);
    assert(split[0] == -1 && split[1] == 0);

    float invalid[21], before[21];
    memcpy(invalid, once, sizeof(once));
    invalid[20] = NAN;
    memcpy(before, invalid, sizeof(invalid));
    assert(liquid_spring_step(invalid, 3, 0.01f) == -1);
    assert(memcmp(before, invalid, sizeof(invalid)) == 0);
    assert(liquid_spring_step(0, 1, 0.01f) == -1);
    assert(liquid_spring_step(critical, 65, 0.01f) == -1);
    assert(liquid_spring_step(critical, 1, -1) == -1);
    assert(liquid_spring_step(critical, 1, INFINITY) == -1);

    float snap[] = {2, -4, 7, -1, 0, 0.001f, 3};
    assert(liquid_spring_step(snap, 1, 0) == 0);
    assert(snap[0] == 7 && snap[1] == 0);
    return 0;
}
