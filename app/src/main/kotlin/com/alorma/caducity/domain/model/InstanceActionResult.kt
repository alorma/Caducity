package com.alorma.caducity.domain.model

sealed interface InstanceActionResult<out T> {
  data class Success<T>(val value: T) : InstanceActionResult<T>
  data class Failure(val error: InstanceActionError) : InstanceActionResult<Nothing>

  fun onFailure(block: (InstanceActionError) -> Unit): InstanceActionResult<T> {
    if (this is Failure) {
      block(error)
    }
    return this
  }

  fun onSuccess(block: (T) -> Unit): InstanceActionResult<T> {
    if (this is Success) {
      block(value)
    }
    return this
  }
}
