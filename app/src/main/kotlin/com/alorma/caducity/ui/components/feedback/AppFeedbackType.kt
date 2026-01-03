package com.alorma.caducity.ui.components.feedback

import com.alorma.caducity.domain.model.InstanceStatus

sealed class AppFeedbackType {
  data class Status(val status: InstanceStatus) : AppFeedbackType()
  data object Success : AppFeedbackType()
  data object Info : AppFeedbackType()
  data object Error : AppFeedbackType()
}