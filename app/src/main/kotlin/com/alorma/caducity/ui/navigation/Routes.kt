package com.alorma.caducity.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.domain.model.ExpiryStatus
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

/** Caducats, Aviat, Frescos or Congelats: one screen, four variants. */
@Serializable
data class StatusList(
  val status: ExpiryStatus,
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

/** Full screen: turns [itemIds] (and any ingredient added later) into new products. */
@Serializable
data class Transform(
  val itemIds: List<String> = emptyList(),
) : AppRoute

/** Full screen: picks the products of the pantry that Transform uses as ingredients. */
@Serializable
data class IngredientPicker(
  val selectedItemIds: List<String> = emptyList(),
) : AppRoute

@Serializable
data class CategoryEditor(
  val id: String? = null,
) : AppRoute

/** Shown as a bottom sheet over the current screen. */
@Serializable
data class ItemActions(
  val itemIds: List<String>,
) : AppRoute

/** Shown as a bottom sheet over the current screen. */
@Serializable
data object QuickCategory : AppRoute

/** Shown as a bottom sheet over Home, once, to whoever updates the app: «Novetat Transforma». */
@Serializable
data object TransformIntro : AppRoute

@Serializable
data object Onboarding : AppRoute

@Serializable
data object SettingsBackup : AppRoute

@Serializable
data object SettingsCategoryMargins : AppRoute

@Serializable
data object SettingsPrivacy : AppRoute

@Serializable
data object SettingsAbout : AppRoute

@Serializable
data object SettingsDebug : AppRoute

@Serializable
data object SettingsDebugRemoteConfigs : AppRoute
