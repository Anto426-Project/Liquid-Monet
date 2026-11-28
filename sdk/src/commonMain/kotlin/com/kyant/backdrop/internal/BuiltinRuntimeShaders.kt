package com.kyant.backdrop.internal

/** The shader sources used by the SDK; warm-up and rendering use the same programs. */
internal enum class BuiltinRuntimeShader(val source: String, val hasBackdropInput: Boolean) {
    Refraction(RoundedRectRefractionShaderString, true),
    Dispersion(RoundedRectRefractionWithDispersionShaderString, true),
    Highlight(DefaultHighlightShaderString, false),
    Ambient(AmbientHighlightShaderString, false),
    AdaptiveLegibility(AdaptiveLegibilityShaderString, true)
}
