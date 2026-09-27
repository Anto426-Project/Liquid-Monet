package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Immutable

/** Manual CPU-family policy. This ceiling never replaces RAM, clock or measured CPU/GPU limits. */
@Immutable
data class LiquidGlassProcessorFamily(
    val id: String,
    val displayName: String,
    val cpuQualityCeiling: LiquidGlassQualityTier,
    /** Introduction of the underlying generation, not the phone/rebrand release or OS year. */
    val generationIntroducedYear: Int? = null,
) {
    /** Chip policy is explicit; introduction year is metadata, never a second hidden ceiling. */
    val effectiveCpuQualityCeiling: LiquidGlassQualityTier
        get() = cpuQualityCeiling
}

/**
 * Offline, reviewable family rules instead of a list of phone models.
 *
 * Edit a generation once to cover its matching SoCs. Policy revisions migrate saved profiles
 * through the calibration coordinator; interrupted native probes keep their crash guard. Unknown
 * identifiers deliberately return null and retain benchmark-only classification.
 */
object LiquidGlassProcessorFamilies {
    private data class Rule(val family: LiquidGlassProcessorFamily, val identifiers: List<Regex>)

    private fun rule(
        id: String,
        name: String,
        ceiling: LiquidGlassQualityTier,
        vararg patterns: String,
        year: Int? = null,
    ) =
        Rule(
            LiquidGlassProcessorFamily(id, name, ceiling, year),
            patterns.map { Regex("(?:^| )(?:$it)(?: |$)") },
        )

    // These are SDK policy choices, not vendor performance guarantees. Keep code aliases only
    // where their family is known; a generic vendor name such as qcom or mt must not select a tier.
    private val rules =
        listOf(
            // Metal's device name exposes the Apple SoC (e.g. "Apple A18 Pro GPU"). Match
            // actual silicon names, never an iPhone product number or a generic "Apple GPU".
            rule(
                "apple-a8",
                "Apple A8 / A8X",
                LiquidGlassQualityTier.HIGH,
                "APPLE A8X?",
                year = 2014,
            ),
            rule(
                "apple-a9",
                "Apple A9 / A9X",
                LiquidGlassQualityTier.HIGH,
                "APPLE A9X?",
                year = 2015,
            ),
            rule(
                "apple-a10",
                "Apple A10 / A10X",
                LiquidGlassQualityTier.HIGH,
                "APPLE A10X?",
                year = 2016,
            ),
            rule(
                "apple-a11",
                "Apple A11 Bionic",
                LiquidGlassQualityTier.HIGH,
                "APPLE A11",
                year = 2017,
            ),
            rule(
                "apple-a12",
                "Apple A12 / A12X / A12Z",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A12[XZ]?",
                year = 2018,
            ),
            rule(
                "apple-a13",
                "Apple A13 Bionic",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A13",
                year = 2019,
            ),
            rule(
                "apple-a14",
                "Apple A14 Bionic",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A14",
                year = 2020,
            ),
            rule(
                "apple-a15",
                "Apple A15 Bionic",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A15",
                year = 2021,
            ),
            rule(
                "apple-a16",
                "Apple A16 Bionic",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A16",
                year = 2022,
            ),
            rule(
                "apple-a17",
                "Apple A17 Pro",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A17 PRO",
                year = 2023,
            ),
            rule(
                "apple-a18",
                "Apple A18 / A18 Pro",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A18(?: PRO)?",
                year = 2024,
            ),
            rule(
                "apple-a19",
                "Apple A19 / A19 Pro",
                LiquidGlassQualityTier.ULTRA,
                "APPLE A19(?: PRO)?",
                year = 2025,
            ),
            rule(
                "apple-m1",
                "Apple M1 family",
                LiquidGlassQualityTier.ULTRA,
                "APPLE M1(?: PRO| MAX| ULTRA)?",
                year = 2020,
            ),
            rule(
                "apple-m2",
                "Apple M2 family",
                LiquidGlassQualityTier.ULTRA,
                "APPLE M2(?: PRO| MAX| ULTRA)?",
                year = 2022,
            ),
            rule(
                "apple-m3",
                "Apple M3 family",
                LiquidGlassQualityTier.ULTRA,
                "APPLE M3(?: PRO| MAX| ULTRA)?",
                year = 2023,
            ),
            rule(
                "apple-m4",
                "Apple M4 family",
                LiquidGlassQualityTier.ULTRA,
                "APPLE M4(?: PRO| MAX| ULTRA)?",
                year = 2024,
            ),
            rule(
                "apple-m5",
                "Apple M5 family",
                LiquidGlassQualityTier.ULTRA,
                "APPLE M5(?: PRO| MAX| ULTRA)?",
                year = 2025,
            ),
            // Specific generations must precede series fallbacks. Rebrands retain their silicon
            // era.
            // Launch dates and code aliases are documented in docs/PROCESSOR_FAMILIES.md.
            rule(
                "snapdragon-820",
                "Snapdragon 820/821 generation",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON 82[01]",
                "MSM8996(?:PRO)?",
                year = 2015,
            ),
            rule(
                "snapdragon-835",
                "Snapdragon 835 generation",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON 835",
                "MSM8998",
                year = 2016,
            ),
            rule(
                "snapdragon-845",
                "Snapdragon 845 generation",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON 845",
                "SDM845",
                year = 2017,
            ),
            rule(
                "snapdragon-855",
                "Snapdragon 855/860 generation",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON (?:855|860)",
                "SM8150(?:AC)?",
                year = 2018,
            ),
            rule(
                "snapdragon-865",
                "Snapdragon 865/870 generation",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON (?:865|870)",
                "SM8250(?:A[BC])?",
                year = 2019,
            ),
            rule(
                "snapdragon-888",
                "Snapdragon 888 generation",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 888",
                "SM8350(?:AC)?",
                year = 2020,
            ),
            rule(
                "snapdragon-8-gen1",
                "Snapdragon 8 / 8+ Gen 1",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 8 GEN 1",
                "SM(?:8450|8475)",
                year = 2021,
            ),
            rule(
                "snapdragon-8-gen2",
                "Snapdragon 8 Gen 2",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 8 GEN 2",
                "SM8550(?:A[BC])?",
                year = 2022,
            ),
            rule(
                "snapdragon-8-gen3",
                "Snapdragon 8 Gen 3",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 8 GEN 3",
                "SM8650(?:A[ABC])?",
                year = 2023,
            ),
            rule(
                "snapdragon-8-elite",
                "Snapdragon 8 Elite (Oryon)",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 8 ELITE(?! GEN)",
                "SM8750(?:AB)?",
                year = 2024,
            ),
            rule(
                "snapdragon-8s",
                "Snapdragon 8s",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 8S(?: GEN [0-9]+)?",
                "SM(?:8635|8735)",
            ),
            rule(
                "snapdragon-8",
                "Snapdragon 8 (unspecified generation)",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 8(?: GEN [0-9]+| ELITE(?: GEN [0-9]+)?)?",
                "SM8[0-9]{3}",
            ),
            rule(
                "snapdragon-legacy-7",
                "Snapdragon 730/765 generation",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON (?:73[02]|76[58])G?",
                "SM7[12]50(?:A[ABC])?",
                year = 2019,
            ),
            rule(
                "snapdragon-7",
                "Snapdragon 7",
                LiquidGlassQualityTier.ULTRA,
                "SNAPDRAGON 7(?:S)?(?: GEN [0-9]+)?",
                "SM7[0-9]{3}",
            ),
            rule(
                "snapdragon-6",
                "Snapdragon 6",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON 6(?:S)?(?: GEN [0-9]+)?",
                "SM6[0-9]{3}",
            ),
            rule(
                "snapdragon-4",
                "Snapdragon 4",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON 4(?:S)?(?: GEN [0-9]+)?",
                "SM4[0-9]{3}",
            ),
            rule(
                "snapdragon-800",
                "Snapdragon 800 series",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON 8[0-9]{2}",
                "SDM8[0-9]{2}",
                "MSM899[0-9]",
            ),
            rule(
                "snapdragon-legacy-mid",
                "Snapdragon 600/700 series",
                LiquidGlassQualityTier.HIGH,
                "SNAPDRAGON [67][0-9]{2}G?",
                "SDM[67][0-9]{2}",
                "MSM895[0-9]",
            ),
            rule(
                "snapdragon-legacy-entry",
                "Snapdragon 200/400 series",
                LiquidGlassQualityTier.BALANCED,
                "SNAPDRAGON [24][0-9]{2}",
                "SDM4[0-9]{2}",
                "MSM89[023][0-9]",
            ),
            rule(
                "dimensity-9000-gen1",
                "Dimensity 9000 generation",
                LiquidGlassQualityTier.ULTRA,
                "DIMENSITY 9000",
                "MT6983[A-Z]*",
                year = 2021,
            ),
            rule(
                "dimensity-9200",
                "Dimensity 9200 generation",
                LiquidGlassQualityTier.ULTRA,
                "DIMENSITY 9200",
                "MT6985[A-Z]*",
                year = 2022,
            ),
            rule(
                "dimensity-9000",
                "Dimensity 9000 series (unspecified generation)",
                LiquidGlassQualityTier.ULTRA,
                "DIMENSITY 9[0-9]{3}[A-Z]*",
            ),
            rule(
                "dimensity-8000",
                "Dimensity 8000 series",
                LiquidGlassQualityTier.ULTRA,
                "DIMENSITY 8[0-9]{3}[A-Z]*",
                "MT6895[A-Z]*",
            ),
            rule(
                "dimensity-7000",
                "Dimensity 7000 series",
                LiquidGlassQualityTier.HIGH,
                "DIMENSITY 7[0-9]{3}[A-Z]*",
            ),
            rule(
                "dimensity-6000",
                "Dimensity 6000 series",
                LiquidGlassQualityTier.HIGH,
                "DIMENSITY 6[0-9]{3}[A-Z]*",
            ),
            rule(
                "dimensity-legacy",
                "Dimensity 700–1300",
                LiquidGlassQualityTier.HIGH,
                "DIMENSITY (?:[789][0-9]{2}|1[0-3][0-9]{2})[A-Z]*",
                "MT6893[A-Z]*",
            ),
            rule("helio-g", "Helio G", LiquidGlassQualityTier.HIGH, "HELIO G[0-9]{2,3}[A-Z]*"),
            rule("helio-p", "Helio P", LiquidGlassQualityTier.HIGH, "HELIO P[0-9]{2}[A-Z]*"),
            rule("helio-entry", "Helio A", LiquidGlassQualityTier.BALANCED, "HELIO A[0-9]{2}"),
            rule(
                "exynos-modern-flagship",
                "Exynos 2200–2900",
                LiquidGlassQualityTier.ULTRA,
                "EXYNOS ?2[2-9][0-9]{2}",
            ),
            rule(
                "exynos-premium",
                "Exynos 1000/2100/900 series",
                LiquidGlassQualityTier.ULTRA,
                "EXYNOS ?(?:1[0-9]{3}|21[0-9]{2}|9[0-9]{2,3})",
            ),
            rule(
                "exynos-legacy-mid",
                "Exynos 7000/8000 series",
                LiquidGlassQualityTier.HIGH,
                "EXYNOS ?[78][0-9]{3}",
            ),
            rule(
                "tensor",
                "Google Tensor",
                LiquidGlassQualityTier.ULTRA,
                "(?:GOOGLE )?TENSOR(?: G[0-9]+)?",
            ),
            rule(
                "unisoc-4g",
                "UNISOC T3xx/T6xx/T7x00",
                LiquidGlassQualityTier.HIGH,
                "(?:UNISOC |TIGER )?T(?:[36][0-9]{2}|7[0-4][0-9]{2})",
            ),
            rule(
                "unisoc-5g",
                "UNISOC T760–T890 / T8xxx–T9xxx",
                LiquidGlassQualityTier.HIGH,
                "(?:UNISOC |TIGER )?T(?:7[6-9][0-9]|8[0-9]{2}|[89][0-9]{3})",
            ),
            rule(
                "kirin-900",
                "Kirin 900/9000 series",
                LiquidGlassQualityTier.ULTRA,
                "KIRIN 9[0-9]{2,3}[A-Z]*",
            ),
            rule(
                "kirin-mid",
                "Kirin 600/700/800 series",
                LiquidGlassQualityTier.HIGH,
                "KIRIN [678][0-9]{2}[A-Z]*",
            ),
        )

    val families: List<LiquidGlassProcessorFamily> = rules.map { it.family }

    /** Prefer explicit SoC model over the less specific Android hardware/board identifier. */
    fun identify(socModel: String, hardware: String = ""): LiquidGlassProcessorFamily? {
        for (identifier in listOf(socModel, hardware)) {
            if (identifier.length > 256) continue
            val normalized = identifier.uppercase().replace(Separators, " ").trim()
            if (normalized.isEmpty()) continue
            rules
                .firstOrNull { rule -> rule.identifiers.any { it.containsMatchIn(normalized) } }
                ?.let {
                    return it.family
                }
        }
        return null
    }

    private val Separators = Regex("[^A-Z0-9]+")
}
