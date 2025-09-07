<h1 align="center">Noise reduction</h1>

<br>

<div align="center" style="display: grid; justify-content: center;">

|                                                                  🌟                                                                   |                  Support this project                   |               
|:-------------------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------:|
|  <img src="https://raw.githubusercontent.com/ErikThiart/cryptocurrency-icons/master/32/bitcoin.png" alt="Bitcoin (BTC)" width="32"/>  | <code>bc1qs6qq0fkqqhp4whwq8u8zc5egprakvqxewr5pmx</code> | 
| <img src="https://raw.githubusercontent.com/ErikThiart/cryptocurrency-icons/master/32/ethereum.png" alt="Ethereum (ETH)" width="32"/> | <code>0x3147bEE3179Df0f6a0852044BFe3C59086072e12</code> |
|  <img src="https://raw.githubusercontent.com/ErikThiart/cryptocurrency-icons/master/32/tether.png" alt="USDT (TRC-20)" width="32"/>   |     <code>TKznmR65yhPt5qmYCML4tNSWFeeUkgYSEV</code>     |

</div>

<br>

<p align="center">JVM library for noise reduction written in Kotlin based on the ML model <a href="https://github.com/snakers4/silero-models">Silero</a></p>

### See also

- [Stretch](https://github.com/numq/stretch) *to change the speed of audio without changing the pitch*


- [Voice Activity Detection](https://github.com/numq/voice-activity-detection) *to extract speech from audio*


- [Speech recognition](https://github.com/numq/speech-recognition) *to transcribe audio to text*


- [Speech generation](https://github.com/numq/speech-generation) *to generate voice audio from text*


- [Text generation](https://github.com/numq/text-generation) *to generate text from prompt*

## When to use

### Silero

Attempts to reduce background noise along with various artefacts such as reverb, clipping, high/lowpass filters etc.,
while trying to preserve and/or enhance speech.

## Features

- Reduces noise in PCM audio data
- Supports any sampling rate and number of channels due to resampling and downmixing

## Installation

- Download latest [release](https://github.com/numq/noise-reduction/releases)

- Add library dependency
   ```kotlin
   dependencies {
        implementation(file("/path/to/jar"))
   }
   ```

### Silero

- Add ONNX dependency
   ```kotlin
   dependencies {
        implementation("ai.djl.pytorch:pytorch-native-cpu:2.5.1:win-x86_64")
   }
   ```

## Usage

> See the [example](example) module for implementation details

### TL;DR

- Call `process` to denoise the input data

### Step-by-step

- Create an instance

  ### Silero

  ```kotlin
  NoiseReduction.Silero.create()
  ```


- Call `inputSizeForMillis` to get the input data size for N milliseconds


- Call `minimumInputSize` to get the audio producer buffer size for real-time reduction


- Call `process` passing the input data, sample rate, and number of channels as arguments


- Call `reset` to reset the internal state - for example when the audio source changes


- Call `close` to release resources

## Requirements

- JVM version 9 or higher

## License

This project is licensed under the [Apache License 2.0](LICENSE)

## Acknowledgments

- [Silero](https://github.com/snakers4/silero-models)
