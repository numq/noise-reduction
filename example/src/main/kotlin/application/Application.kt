package application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import capturing.CapturingService
import com.github.numq.noisereduction.NoiseReduction
import device.DeviceService
import interaction.InteractionScreen
import playback.PlaybackService
import java.nio.file.Paths

const val APP_NAME = "Noise reduction"

fun main() {
    val pathToModel = Thread.currentThread().contextClassLoader.getResource("model")?.toURI()

    checkNotNull(pathToModel) { "Model not found" }

    singleWindowApplication(state = WindowState(width = 512.dp, height = 512.dp), title = APP_NAME) {
        val deviceService = remember { DeviceService.create().getOrThrow() }

        val silero = remember {
            NoiseReduction.Silero.create(
                modelPath = Paths.get(
                    Paths.get(pathToModel).toAbsolutePath().toString(),
                    "silero_denoise_small_fast.pt"
                ).toAbsolutePath().toString()
            ).getOrThrow()
        }

        val capturingService = remember { CapturingService.create().getOrThrow() }

        val playbackService = remember { PlaybackService.create().getOrThrow() }

        val (throwable, setThrowable) = remember { mutableStateOf<Throwable?>(null) }

        DisposableEffect(Unit) {
            onDispose {
                playbackService.close()
                silero.close()
            }
        }

        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                InteractionScreen(
                    deviceService = deviceService,
                    silero = silero,
                    capturingService = capturingService,
                    playbackService = playbackService,
                    handleThrowable = setThrowable
                )
                throwable?.let { t ->
                    Snackbar(modifier = Modifier.padding(8.dp), action = {
                        Button(onClick = { setThrowable(null) }) { Text("Dismiss") }
                    }) { Text(t.localizedMessage) }
                }
            }
        }
    }
}