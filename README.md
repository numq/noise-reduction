# Noise reduction

JVM library for noise reduction written in Kotlin based on the ML
model [Silero](https://github.com/snakers4/silero-models)

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
