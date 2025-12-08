package com.alorma.caducity.dashboard

import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import com.alorma.caducity.data.model.ProductInstance
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

object DashboardThresholds {
  val soonExpiringDuration: Duration = 7.days
}

class DashboardMapper {
  fun mapToDashboardSections(
    instances: List<ProductInstance>,
    now: Instant
  ): List<DashboardSection> {
    val soonThreshold = now + DashboardThresholds.soonExpiringDuration

    val expired = instances.count { it.expirationDate <= now }
    val soonExpiring = instances.count { it.expirationDate > now && it.expirationDate <= soonThreshold }
    val stillGood = instances.count { it.expirationDate > soonThreshold }

    return listOf(
      DashboardSection(
        title = Res.string.dashboard_section_expired,
        itemCount = expired
      ),
      DashboardSection(
        title = Res.string.dashboard_section_expiring_soon,
        itemCount = soonExpiring
      ),
      DashboardSection(
        title = Res.string.dashboard_section_fresh,
        itemCount = stillGood
      )
    )
  }
}
