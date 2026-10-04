package com.kyant.backdrop

import com.kyant.backdrop.internal.BuiltinRuntimeShader
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import org.jetbrains.skia.RuntimeEffect

/** Four immutable Skia programs; builders/uniforms remain exclusive to individual render nodes. */
@OptIn(ExperimentalAtomicApi::class)
internal object RuntimeShaderPrewarm {
    private val compiled = AtomicReference<Map<String, RuntimeEffect>>(emptyMap())
    private val preparation = CoroutineScope(SupervisorJob() + Dispatchers.Default).async(start = CoroutineStart.LAZY) {
        val programs = buildMap {
            for (shader in BuiltinRuntimeShader.entries) {
                try { put(shader.source, RuntimeEffect.makeForShader(shader.source)) }
                catch (_: Exception) { /* The local render cache handles an unsupported program. */ }
            }
        }
        compiled.store(programs)
    }

    suspend fun prepare() { preparation.await() }

    fun obtain(source: String): RuntimeEffect =
        compiled.load()[source] ?: RuntimeEffect.makeForShader(source)
}
