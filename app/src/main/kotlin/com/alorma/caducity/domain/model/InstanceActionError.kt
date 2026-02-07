package com.alorma.caducity.domain.model

sealed class InstanceActionError(message: String) : Exception(message) {
  data object InstanceNotFound : InstanceActionError("Instance not found")
  data object CannotFreezeExpiredInstance : InstanceActionError("Cannot freeze expired instance")
  data class CannotConsumeExpiredInstance(val itemId: String) : InstanceActionError("Cannot consume expired instance: $itemId")
}
