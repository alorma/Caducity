package com.alorma.caducity

sealed interface TopLevelRoute {
  data object Dashboard : TopLevelRoute
}