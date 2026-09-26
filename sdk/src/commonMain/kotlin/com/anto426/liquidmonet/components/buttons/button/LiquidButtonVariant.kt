package com.anto426.liquidmonet.components.buttons.button

/** Visual styling variants for Liquid Monet Buttons. */
enum class LiquidButtonVariant {
    /** Primary brand action (Filled with active Monet primary + refraction) */
    Primary,
    /** Secondary brand action (Filled with Monet secondary) */
    Secondary,
    /** Tertiary / Tonal brand action (Filled with Monet tertiary) */
    Tonal,
    /** Pure crystal optical glass (Zero tint, background shines through lens) */
    Glass,
    /** Glass button with subtle luminous outline */
    Outlined,
    /** Minimal ghost button with subtle text/icon highlight */
    Text;

    companion object {
        val Filled: LiquidButtonVariant
            get() = Primary
    }
}

/** Sizing options for Liquid Monet Buttons. */
enum class LiquidButtonSize {
    Small,
    Medium,
    Large,
}
