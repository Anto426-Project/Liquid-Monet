package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import kotlin.test.*

class LiquidSpringBatchTest {
    @Test
    fun analyticBatchMatchesComposeAcrossDampingRegimes() {
        for (damping in listOf(0.6f, 1f, 1.15f, 3f)) {
            val batch = LiquidSpringBatch(1)
            batch.initialize(0, -2f)
            batch.configure(0, SpringSpec(damping, 380f), 0.000001f)
            batch.setTarget(0, 3f)
            val reference = FloatSpringSpec(damping, 380f, 0.000001f)
            repeat(12) { index ->
                // Compose's reference rounds time to milliseconds; compare at exact ms boundaries.
                batch.step(0.016f)
                val time = (index + 1) * 16_000_000L
                assertEquals(
                    reference.getValueFromNanos(time, -2f, 3f, 0f),
                    batch.value(0),
                    0.00001f,
                )
                assertEquals(
                    reference.getVelocityFromNanos(time, -2f, 3f, 0f),
                    batch.velocity(0),
                    0.0001f,
                )
            }
        }
    }

    @Test
    fun retargetPreservesPositionAndVelocityInsteadOfRestarting() {
        val batch = LiquidSpringBatch(1)
        batch.initialize(0, 0f)
        batch.configure(0, SpringSpec(0.7f, 250f), 0.001f)
        batch.setTarget(0, 1f)
        batch.step(0.1f)
        val position = batch.value(0)
        val velocity = batch.velocity(0)
        batch.setTarget(0, -1f)
        assertEquals(position, batch.value(0))
        assertEquals(velocity, batch.velocity(0))
        repeat(500) { batch.step(1f / 144f) }
        assertEquals(-1f, batch.value(0))
        assertEquals(0f, batch.velocity(0))
        assertFalse(batch.step(0.016f))
    }

    @Test
    fun reducedMotionAndLongSuspensionsEndAtTheExactTarget() {
        val batch = LiquidSpringBatch(2)
        batch.initialize(0, 0f)
        batch.initialize(1, 1f)
        batch.configure(0, SpringSpec(0.6f, 250f), 0.001f)
        batch.configure(1, SnapSpec(), 0.001f)
        batch.setTarget(0, 10f)
        batch.setTarget(1, 2f)
        assertEquals(2f, batch.value(1))
        assertFalse(batch.step(60f))
        assertEquals(10f, batch.value(0))
        assertFailsWith<IllegalArgumentException> { batch.step(Float.NaN) }
        assertFailsWith<IllegalArgumentException> { batch.setTarget(0, Float.POSITIVE_INFINITY) }
    }
}
