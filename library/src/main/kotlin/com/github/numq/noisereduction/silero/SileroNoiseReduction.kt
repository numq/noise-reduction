package com.github.numq.noisereduction.silero

import com.github.numq.noisereduction.NoiseReduction
import com.github.numq.noisereduction.audio.AudioProcessing.calculateChunkSize
import com.github.numq.noisereduction.audio.AudioProcessing.resample
import com.github.numq.noisereduction.silero.model.SileroModel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class SileroNoiseReduction(
    private val model: SileroModel,
) : NoiseReduction.Silero {
    private companion object {
        const val MINIMUM_CHUNK_MILLIS = 1_000
        const val TARGET_INPUT_SAMPLE_RATE = 24_000
        const val TARGET_OUTPUT_SAMPLE_RATE = 48_000
    }

    private val mutex = Mutex()

    private fun resampleIfNeeded(
        inputData: ByteArray,
        channels: Int,
        inputSampleRate: Int,
        outputSampleRate: Int,
    ) = when (inputSampleRate) {
        outputSampleRate -> inputData

        else -> resample(
            inputData = inputData,
            channels = channels,
            inputSampleRate = inputSampleRate,
            outputSampleRate = outputSampleRate
        )
    }

    override fun inputSizeForMillis(sampleRate: Int, channels: Int, millis: Long) = runCatching {
        val minSize = minimumInputSize(sampleRate, channels).getOrThrow()

        val factor = (millis + MINIMUM_CHUNK_MILLIS - 1) / MINIMUM_CHUNK_MILLIS

        (factor * minSize).toInt()
    }

    override fun minimumInputSize(sampleRate: Int, channels: Int) = runCatching {
        calculateChunkSize(sampleRate = sampleRate, channels = channels, millis = MINIMUM_CHUNK_MILLIS)
    }

    override suspend fun process(pcmBytes: ByteArray, sampleRate: Int, channels: Int) = mutex.withLock {
        runCatching {
            require(sampleRate > 0) { "Sample rate must be greater than 0" }

            require(channels > 0) { "Channel count must be at least 1" }

            if (pcmBytes.isEmpty()) return@runCatching byteArrayOf()

            val resampledSamples = resampleIfNeeded(
                inputData = pcmBytes,
                channels = channels,
                inputSampleRate = sampleRate,
                outputSampleRate = TARGET_INPUT_SAMPLE_RATE
            )

            val floatSamples = FloatArray(resampledSamples.size / 2) { i ->
                ((resampledSamples[i * 2].toInt() and 0xFF) or (resampledSamples[i * 2 + 1].toInt() shl 8)) / 32767f
            }

            val denoisedSamples = model.process(input = floatSamples).getOrThrow()

            resampleIfNeeded(
                inputData = ByteArray(denoisedSamples.size * 2) { index ->
                    val sample = (denoisedSamples[index / 2] * 32767).toInt().coerceIn(-32768..32767)

                    (if (index % 2 == 0) sample and 0xFF else sample shr 8 and 0xFF).toByte()
                },
                channels = channels,
                inputSampleRate = TARGET_OUTPUT_SAMPLE_RATE,
                outputSampleRate = sampleRate
            )
        }
    }

    override fun reset() = model.reset()

    override fun close() = runCatching {
        reset().getOrDefault(Unit)

        model.close()
    }.getOrDefault(Unit)
}