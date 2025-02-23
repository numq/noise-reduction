package com.github.numq.noisereduction

import com.github.numq.noisereduction.silero.SileroNoiseReduction
import com.github.numq.noisereduction.silero.model.SileroModel

interface NoiseReduction : AutoCloseable {
    /**
     * Returns the minimum possible input size for the given number of milliseconds.
     *
     * @param sampleRate the sampling rate of the audio data in Hz.
     * @param channels the number of audio channels.
     * @param millis the duration in milliseconds.
     * @return a [Result] containing the minimum chunk size in bytes.
     */
    fun inputSizeForMillis(sampleRate: Int, channels: Int, millis: Long): Result<Int>

    /**
     * Returns the minimum effective chunk size - the size at which there is no need to fill the input data with silence.
     *
     * @param sampleRate the sampling rate of the audio data in Hz.
     * @param channels the number of audio channels.
     * @return a [Result] containing the minimum chunk size in bytes.
     */
    fun minimumInputSize(sampleRate: Int, channels: Int): Result<Int>

    /**
     * Reduces noise in the given PCM audio data.
     *
     * @param pcmBytes the audio data in PCM format.
     * @param sampleRate the sampling rate of the audio data in Hz.
     * @param channels the number of audio channels (e.g., 1 for mono, 2 for stereo).
     * @return a [Result] containing a [ByteArray] of denoised audio data
     */
    suspend fun process(pcmBytes: ByteArray, sampleRate: Int, channels: Int): Result<ByteArray>

    /**
     * Resets the noise reduction internal state.
     *
     * @return a [Result] indicating the success or failure of the operation.
     */
    fun reset(): Result<Unit>

    interface Silero : NoiseReduction {
        companion object {
            /**
             * Creates a new instance of [NoiseReduction].
             *
             * @param modelPath the noise reduction model path.
             * @return a [Result] containing the created instance if successful.
             */
            fun create(modelPath: String): Result<Silero> = runCatching {
                SileroNoiseReduction(
                    model = SileroModel.create(
                        modelPath = modelPath
                    )
                )
            }
        }
    }
}