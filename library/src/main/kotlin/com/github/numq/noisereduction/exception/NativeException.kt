package com.github.numq.noisereduction.exception

data class NativeException(override val cause: Throwable) : Exception(cause)