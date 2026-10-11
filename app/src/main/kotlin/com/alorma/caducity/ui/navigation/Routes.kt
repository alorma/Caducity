package com.alorma.caducity.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** Every destination of the app's single back stack. */
sealed interface AppRoute : NavKey

/** Destinations reachable from the floating navigation bar (one tab each). */
sealed interface TopLevelRoute : AppRoute

@Serializable
data object Home : TopLevelRoute

@Serializable
data object Pantry : TopLevelRoute

/** Secondary screen (no navigation bar), opened from an icon, not a tab. */
@Serializable
data object Settings : AppRoute

@Serializable
enum class CategoryFilter {
  Fresh,
  Soon,
  Expired,
  Frozen,
}

fun CategoryFilter.toStatus(): ExpiryStatus =
  when (this) {
    CategoryFilter.Fresh -> ExpiryStatus.FRESH
    CategoryFilter.Soon -> ExpiryStatus.SOON
    CategoryFilter.Expired -> ExpiryStatus.EXPIRED
    CategoryFilter.Frozen -> ExpiryStatus.FROZEN
  }

@Serializable
data class Category(
  val id: String,
  val filter: CategoryFilter? = null,
  /** Product to highlight: notifications scheduled before the redesign open a product. */
  val initialProductId: String? = null,
) : AppRoute

@Serializable
data object Expired : AppRoute

// TEMP(fase 7): old "items by status" screen, replaced by Expired.
@Serializable
data class FilteredItems(
  val status: ItemStatus,
) : AppRoute

@Serializable
data class Calendar(
  val categoryId: String? = null,
  val date: LocalDate? = null,
) : AppRoute

@Serializable
data class Add(
  val categoryId: String? = null,
  val source: String? = null,
  val itemId: String? = null,
) : AppRoute

@Serializable
data class CategoryEditor(
  val id: String? = null,
) : AppRoute

/** Shown as a bottom sheet over the current screen. */
@Serializable
data class ItemActions(
  val itemId: String,
) : AppRoute

/** Shown as a bottom sheet over the current screen. */
@Serializable
data object QuickCategory : AppRoute

@Serializable
data object Onboarding : AppRoute

@Serializable
data object SettingsAppearance : AppRoute

@Serializable
data object SettingsNotifications : AppRoute

@Serializable
data object SettingsBackup : AppRoute

@Serializable
data object SettingsPrivacy : AppRoute

@Serializable
data object SettingsAbout : AppRoute

@Serializable
data object SettingsDebug : AppRoute

@Serializable
data object SettingsDebugRemoteConfigs : AppRoute
