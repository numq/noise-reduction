package com.github.numq.noisereduction

import com.github.numq.noisereduction.silero.SileroModelType
import com.github.numq.noisereduction.silero.SileroNoiseReduction

interface NoiseReduction : AutoCloseable {
    fun inputSizeForMillis(sampleRate: Int, channels: Int, millis: Long): Result<Int>

    fun minimumInputSize(sampleRate: Int, channels: Int): Result<Int>

    suspend fun process(pcmBytes: ByteArray, sampleRate: Int, channels: Int): Result<ByteArray>

    /**
     * Resets the noise reduction internal state.
     *
     * @return a [Result] indicating the success or failure of the operation.
     */
    fun reset(): Result<Unit>

    interface Silero : NoiseReduction {
        suspend fun getModelType(): Result<SileroModelType>

        suspend fun changeModelType(modelType: SileroModelType): Result<Unit>

        companion object {
            /**
             * Creates a new instance of [NoiseReduction].
             *
             * @param modelType the noise reduction model type.
             * @return a [Result] containing the created instance if successful.
             */
            fun create(modelType: SileroModelType = SileroModelType.Small.Fast): Result<Silero> = runCatching {
                SileroNoiseReduction(modelType = modelType)
            }
        }
    }
}