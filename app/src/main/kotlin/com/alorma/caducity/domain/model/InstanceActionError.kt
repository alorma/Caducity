package com.alorma.caducity.domain.model

sealed interface InstanceActionError {
  data object InstanceNotFound : InstanceActionError
  data object CannotFreezeExpiredInstance : InstanceActionError
  data class CannotConsumeExpiredInstance(val instanceId: String) : InstanceActionError
}
