package com.github.numq.noisereduction.silero.model

import ai.djl.Model
import ai.djl.ndarray.NDArray
import ai.djl.ndarray.NDList
import ai.djl.ndarray.NDManager
import ai.djl.translate.Translator
import ai.djl.translate.TranslatorContext
import kotlin.io.path.Path

class PytorchSileroModel(modelPath: String) : SileroModel {
    private companion object {
        const val MODEL_NAME = "silero_denoise"
        const val INPUT_SAMPLE_RATE = 24_000
    }

    private val manager by lazy { NDManager.newBaseManager() }

    private val model by lazy {
        Model.newInstance(MODEL_NAME).apply {
            load(Path(modelPath))
        }
    }

    private val translator by lazy {
        object : Translator<NDArray, NDArray> {
            override fun processInput(context: TranslatorContext, array: NDArray) = NDList(array)

            override fun processOutput(context: TranslatorContext, list: NDList) = list.singletonOrThrow()
        }
    }

    private val predictor by lazy { model.newPredictor(translator) }

    override fun process(input: FloatArray): Result<FloatArray> = runCatching {
        manager.create(input).use { inputArray ->
            predictor.predict(inputArray).toFloatArray()
        }
    }

    override fun reset() = runCatching {
        process(FloatArray(INPUT_SAMPLE_RATE))

        Unit
    }

    override fun close() {
        predictor.close()
        model.close()
        manager.close()
    }
}
