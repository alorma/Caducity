package com.alorma.caducity.domain.model

sealed interface InstanceActionError {
  data object InstanceNotFound : InstanceActionError
  data object CannotFreezeExpiredInstance : InstanceActionError
  data object CannotConsumeExpiredInstance : InstanceActionError
}
