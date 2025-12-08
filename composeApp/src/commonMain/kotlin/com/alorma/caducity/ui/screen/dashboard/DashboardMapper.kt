package com.alorma.caducity.ui.screen.dashboard

import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_section_expired
import caducity.composeapp.generated.resources.dashboard_section_expiring_soon
import caducity.composeapp.generated.resources.dashboard_section_fresh
import com.alorma.caducity.domain.model.DashboardProducts

class DashboardMapper {
  fun mapToDashboardSections(dashboardProducts: DashboardProducts): List<DashboardSection> {
    return listOf(
      DashboardSection(
        title = Res.string.dashboard_section_expired,
        itemCount = dashboardProducts.expired.size
      ),
      DashboardSection(
        title = Res.string.dashboard_section_expiring_soon,
        itemCount = dashboardProducts.expiringSoon.size
      ),
      DashboardSection(
        title = Res.string.dashboard_section_fresh,
        itemCount = dashboardProducts.fresh.size
      )
    )
  }
}
