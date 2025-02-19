import com.github.numq.noisereduction.NoiseReduction
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SileroNoiseReductionTest {
    private val silero by lazy { NoiseReduction.Silero.create().getOrThrow() }

    @Test
    fun `output must be the same as the input for silence`() = runTest {
        val sampleRate = 48_000
        val channels = 1
        val duration = 10.seconds

        val pcmBytes = ByteArray((sampleRate * (duration.inWholeMilliseconds / 1_000.0) * channels * 2).toInt())

        assertContentEquals(pcmBytes, silero.process(pcmBytes, sampleRate, channels).getOrThrow())
    }
}