package com.github.numq.noisereduction.silero.model

internal interface SileroModel : AutoCloseable {
    fun process(input: FloatArray): Result<FloatArray>
    fun reset(): Result<Unit>
}