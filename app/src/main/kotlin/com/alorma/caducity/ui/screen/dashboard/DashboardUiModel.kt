package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Stable
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.domain.model.InstanceStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate

@Stable
sealed interface ProductUiModel {
  val id: String
  val name: String
  val description: String

  @Stable
  data class WithInstances(
    override val id: String,
    override val name: String,
    override val description: String,
    val today: String,
    val instances: ImmutableList<ProductInstanceUiModel>,
  ) : ProductUiModel

  @Stable
  data class Empty(
    override val id: String,
    override val name: String,
    override val description: String,
  ) : ProductUiModel
}

@Stable
data class ProductInstanceUiModel(
  val id: String,
  val identifier: String,
  val status: InstanceStatus,
  val expirationDate: LocalDate,
  val expirationDateText: String,
)

@Stable
data class CalendarData(
  val productsByDate: ImmutableMap<LocalDate, CalendarDateInfo>
)

@Stable
data class CalendarDateInfo(
  val status: InstanceStatus?,
  val shapePosition: ShapePosition,
)