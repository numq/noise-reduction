package com.github.numq.noisereduction.silero

sealed class SileroModelType private constructor(open val fullName: String) {
    sealed class Small private constructor(override val fullName: String) : SileroModelType(fullName) {
        data object Fast : Small("silero_denoise_small_fast") {
            override fun toString() = "SileroModelType.Small.Fast"
        }

        data object Slow : Small("silero_denoise_small_slow") {
            override fun toString() = "SileroModelType.Small.Slow"
        }
    }

    sealed class Large private constructor(override val fullName: String) : SileroModelType(fullName) {
        data object Fast : Large("silero_denoise_large_fast") {
            override fun toString() = "SileroModelType.Large.Fast"
        }
    }
}