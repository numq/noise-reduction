package com.github.numq.noisereduction.silero.model

internal interface SileroModel : AutoCloseable {
    fun process(input: FloatArray): Result<FloatArray>
    fun reset(): Result<Unit>

    companion object {
        fun create(modelPath: String): SileroModel = PytorchSileroModel(modelPath = modelPath)
    }
}