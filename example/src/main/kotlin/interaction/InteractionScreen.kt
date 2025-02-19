package interaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import capturing.CapturingService
import com.github.numq.noisereduction.NoiseReduction
import com.github.numq.noisereduction.silero.SileroModelType
import device.Device
import device.DeviceService
import item.SileroModelTypeItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import playback.PlaybackService
import selector.SileroModelTypeItemSelector
import javax.sound.sampled.AudioFormat

@Composable
fun InteractionScreen(
    deviceService: DeviceService,
    silero: NoiseReduction.Silero,
    capturingService: CapturingService,
    playbackService: PlaybackService,
    handleThrowable: (Throwable) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }

    var deviceJob by remember { mutableStateOf<Job?>(null) }

    var capturingJob by remember { mutableStateOf<Job?>(null) }

    val capturingDevices = remember { mutableStateListOf<Device>() }

    var selectedCapturingDevice by remember { mutableStateOf<Device?>(null) }

    var refreshRequested by remember { mutableStateOf(true) }

    var isNoiseReductionEnabled by remember { mutableStateOf(false) }

    var selectedSileroModelTypeItem by remember { mutableStateOf(SileroModelTypeItem.SMALL_FAST) }

    LaunchedEffect(selectedSileroModelTypeItem) {
        val modelType = when (selectedSileroModelTypeItem) {
            SileroModelTypeItem.SMALL_FAST -> SileroModelType.Small.Fast

            SileroModelTypeItem.SMALL_SLOW -> SileroModelType.Small.Slow

            SileroModelTypeItem.LARGE_FAST -> SileroModelType.Large.Fast
        }

        silero.changeModelType(modelType).mapCatching {
            selectedSileroModelTypeItem = when (silero.getModelType().getOrThrow()) {
                is SileroModelType.Small.Fast -> SileroModelTypeItem.SMALL_FAST

                is SileroModelType.Small.Slow -> SileroModelTypeItem.SMALL_SLOW

                is SileroModelType.Large.Fast -> SileroModelTypeItem.LARGE_FAST
            }
        }.onFailure(handleThrowable)
    }

    LaunchedEffect(refreshRequested) {
        deviceJob?.cancelAndJoin()
        deviceJob = null

        if (refreshRequested) {
            deviceJob = coroutineScope.launch {
                deviceService.listCapturingDevices().onSuccess { devices ->
                    if (devices != capturingDevices) {
                        capturingDevices.clear()
                        capturingDevices.addAll(devices)

                        if (selectedCapturingDevice !in capturingDevices) {
                            selectedCapturingDevice = null
                        }
                    }
                }.onFailure(handleThrowable)

                refreshRequested = false
            }
        }
    }

    LaunchedEffect(selectedCapturingDevice, isNoiseReductionEnabled, selectedSileroModelTypeItem) {
        playbackService.stop()

        capturingJob?.cancelAndJoin()
        capturingJob = null

        capturingJob = when (val device = selectedCapturingDevice) {
            null -> return@LaunchedEffect

            else -> coroutineScope.launch {
                val sampleRate = device.sampleRate

                val channels = device.channels

                val chunkSize = silero.minimumInputSize(sampleRate = sampleRate, channels = channels)

                val format = with(device) {
                    AudioFormat(
                        sampleRate.toFloat(), sampleSizeInBits, channels, isSigned, isBigEndian
                    )
                }

                playbackService.start(format).mapCatching {
                    capturingService.capture(
                        device = device, chunkSize = chunkSize.getOrThrow()
                    ).catch {
                        if (it != CancellationException()) {
                            handleThrowable(it)
                        }
                    }.onEach { pcmBytes ->
                        if (isNoiseReductionEnabled) {
                            val denoisedBytes = silero.process(
                                pcmBytes = pcmBytes, sampleRate = sampleRate, channels = channels
                            ).onFailure(handleThrowable).getOrThrow()

                            playbackService.play(pcmBytes = denoisedBytes).getOrThrow()
                        } else {
                            playbackService.play(pcmBytes = pcmBytes).getOrThrow()
                        }
                    }.flowOn(Dispatchers.IO).collect()
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Noise reduction", color = if (isNoiseReductionEnabled) Color.Green else Color.Red)
                Switch(isNoiseReductionEnabled, onCheckedChange = {
                    isNoiseReductionEnabled = it
                })
            }

            AnimatedVisibility(visible = isNoiseReductionEnabled, enter = expandIn(), exit = shrinkOut()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterVertically)
                ) {
                    Text("Silero model type")

                    SileroModelTypeItemSelector(
                        modifier = Modifier.fillMaxWidth(), selectedSileroModelTypeItem = selectedSileroModelTypeItem
                    ) { modelTypeItem ->
                        selectedSileroModelTypeItem = modelTypeItem
                    }
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Capturing devices", modifier = Modifier.padding(8.dp))
                when (refreshRequested) {
                    true -> IconButton(onClick = {
                        refreshRequested = false
                    }) {
                        Icon(Icons.Default.Cancel, null)
                    }

                    false -> IconButton(onClick = {
                        refreshRequested = true
                    }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            }
            when {
                refreshRequested -> Box(
                    modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.Top),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(capturingDevices, key = { it.name }) { device ->
                        Card(modifier = Modifier.fillMaxWidth()
                            .alpha(alpha = if (device == selectedCapturingDevice) .5f else 1f).clickable {
                                selectedCapturingDevice = device.takeIf { it != selectedCapturingDevice }
                            }) {
                            Text(device.name, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }
    }
}