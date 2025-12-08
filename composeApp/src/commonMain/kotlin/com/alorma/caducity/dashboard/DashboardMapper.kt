package com.alorma.caducity.dashboard

import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import com.alorma.caducity.data.model.ProductInstance
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

fun List<ProductInstance>.toDashboardSections(now: Instant): List<DashboardSection> {
  val soonThreshold = now + 7.days

  val expired = count { it.expirationDate <= now }
  val soonExpiring = count { it.expirationDate > now && it.expirationDate <= soonThreshold }
  val stillGood = count { it.expirationDate > soonThreshold }

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
