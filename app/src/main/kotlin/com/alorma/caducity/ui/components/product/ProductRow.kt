package com.alorma.caducity.ui.components.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.Sunny
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.connectedRowColors
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.ui.unit.unitCountText
import kotlinx.datetime.LocalDate

/**
 * The one row of a product in every list. Tapping the bar opens the product's sheet; the round buttons act
 * on one unit, the one that expires first.
 */
@Composable
fun ProductRow(
  model: ProductRowModel,
  index: Int,
  count: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onConsume: () -> Unit = {},
  onFreeze: () -> Unit = {},
  onUnfreeze: () -> Unit = {},
  onDiscard: () -> Unit = {},
) {
  val statusColors = model.status.colors()
  val detailText = productRowDetailText(model)
  SegmentedListItem(
    onClick = onClick,
    shapes = ListItemDefaults.segmentedShapes(index, count),
    modifier = modifier.heightIn(min = 64.dp),
    colors = connectedRowColors(),
    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
    leadingContent =
      model.category?.let {
        { CategoryShapeIcon(shape = it.shape, icon = CategoryIcons.vectorOf(it.icon), size = 44.dp) }
      },
    supportingContent = {
      if (model.detail is ProductRowDetail.WithDays) {
        Text(
          text = detailText,
          style = CaducityTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
          color = statusColors.onContainer,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      } else {
        Text(
          text = detailText,
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    },
    trailingContent = {
      ProductRowActions(
        model = model,
        onConsume = onConsume,
        onFreeze = onFreeze,
        onUnfreeze = onUnfreeze,
        onDiscard = onDiscard,
      )
    },
  ) {
    Text(
      text = model.name,
      style = CaducityTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun ProductRowActions(
  model: ProductRowModel,
  onConsume: () -> Unit,
  onFreeze: () -> Unit,
  onUnfreeze: () -> Unit,
  onDiscard: () -> Unit,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    when (model.status) {
      ExpiryStatus.EXPIRED ->
        RoundButton(
          icon = Symbols.Outlined.Delete,
          contentDescription = stringResource(R.string.status_action_discard, model.name),
          tint = CaducityTheme.colors.expired.strong,
          onClick = onDiscard,
        )
      ExpiryStatus.SOON, ExpiryStatus.FRESH -> {
        RoundButton(
          icon = Symbols.Outlined.Check,
          contentDescription = stringResource(R.string.home_mark_consumed, model.name),
          tint = CaducityTheme.colors.fresh.strong,
          onClick = onConsume,
        )
        if (model.canFreeze) {
          RoundButton(
            icon = Symbols.Outlined.AcUnit,
            contentDescription = stringResource(R.string.status_action_freeze, model.name),
            tint = CaducityTheme.colors.frozen.strong,
            onClick = onFreeze,
          )
        }
      }
      ExpiryStatus.FROZEN -> {
        RoundButton(
          icon = Symbols.Outlined.Check,
          contentDescription = stringResource(R.string.home_mark_consumed, model.name),
          tint = CaducityTheme.colors.fresh.strong,
          onClick = onConsume,
        )
        RoundButton(
          icon = Symbols.Outlined.Sunny,
          contentDescription = stringResource(R.string.status_action_unfreeze, model.name),
          tint = CaducityTheme.colors.frozen.strong,
          onClick = onUnfreeze,
        )
      }
    }
  }
}

/** «1 unitat · Làctics», «2 unitats · D'aquí 2 dies» or just «3 unitats». */
@Composable
private fun productRowDetailText(model: ProductRowModel): String {
  val quantity = unitCountText(model.unitType, model.unitCount, model.amount)
  return when (val detail = model.detail) {
    is ProductRowDetail.InCategory -> "$quantity · ${detail.categoryName}"
    ProductRowDetail.QuantityOnly -> quantity
    ProductRowDetail.WithDays ->
      "$quantity · ${productRowDaysText(model.status, model.remainingDays, model.frozenSince)}"
  }
}

@Composable
private fun productRowDaysText(
  status: ExpiryStatus,
  days: Int?,
  frozenSince: LocalDate? = null,
): String =
  when {
    status == ExpiryStatus.FROZEN && frozenSince != null ->
      stringResource(R.string.frozen_on, shortDateText(frozenSince))
    status == ExpiryStatus.FROZEN || days == null -> stringResource(R.string.item_pill_frozen)
    days == 0 -> stringResource(R.string.item_pill_today)
    days == 1 -> stringResource(R.string.item_pill_tomorrow)
    days == -1 -> stringResource(R.string.item_pill_yesterday)
    days > 1 -> stringResource(R.string.product_row_in_days, days)
    else -> pluralStringResource(R.plurals.item_pill_days_ago, -days, -days)
  }

/** Round action that morphs its shape while pressed (M3 Expressive). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RoundButton(
  icon: ImageVector,
  contentDescription: String,
  tint: Color,
  onClick: () -> Unit,
) {
  FilledIconButton(
    onClick = onClick,
    modifier = Modifier.size(44.dp),
    shapes = IconButtonDefaults.shapes(),
    colors =
      IconButtonDefaults.filledIconButtonColors(
        containerColor = CaducityTheme.colorScheme.surface,
        contentColor = tint,
      ),
  ) {
    Icon(icon, contentDescription = contentDescription, tint = tint)
  }
}

// region Previews

private fun previewRow(
  name: String,
  status: ExpiryStatus,
  days: Int?,
  units: Int,
  detail: ProductRowDetail,
  category: Category? = null,
  frozenSince: LocalDate? = null,
  canFreeze: Boolean = true,
  unitType: UnitType = UnitType.DEFAULT,
  amount: Int? = null,
) = ProductRowModel(
  key = name,
  name = name,
  itemIds = List(units) { "$name-$it" },
  status = status,
  remainingDays = days,
  category = category,
  detail = detail,
  frozenSince = frozenSince,
  canFreeze = canFreeze,
  unitType = unitType,
  amount = amount,
)

/** A row that can go in the freezer (✓ ❄), one that cannot (only ✓) and a frozen one (✓ ☀). */
@PreviewLightDark
@Composable
internal fun ProductRowFreezePreview() {
  PreviewTheme {
    Column(
      modifier = Modifier.background(CaducityTheme.colorScheme.background).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
    ) {
      val rows =
        listOf(
          previewRow("Pernil dolç", ExpiryStatus.SOON, 2, 1, ProductRowDetail.WithDays),
          previewRow("Fruita fresca", ExpiryStatus.FRESH, 5, 1, ProductRowDetail.WithDays, canFreeze = false),
          previewRow(
            name = "Pa de motlle",
            status = ExpiryStatus.FROZEN,
            days = null,
            units = 1,
            detail = ProductRowDetail.WithDays,
            frozenSince = LocalDate(2026, 10, 4),
            canFreeze = false,
          ),
        )
      rows.forEachIndexed { index, row ->
        ProductRow(model = row, index = index, count = rows.size, onClick = {})
      }
    }
  }
}

@PreviewLightDark
@Composable
internal fun ProductRowPreview() {
  PreviewTheme {
    Column(
      modifier = Modifier.background(CaducityTheme.colorScheme.background).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
    ) {
      val rows =
        listOf(
          previewRow("Iogurt natural", ExpiryStatus.EXPIRED, -2, 1, ProductRowDetail.WithDays),
          previewRow("Pernil dolç", ExpiryStatus.SOON, 2, 3, ProductRowDetail.WithDays),
          previewRow("Enciam", ExpiryStatus.FRESH, 9, 1, ProductRowDetail.InCategory("Fruita i verdura")),
          previewRow(
            name = "Pa de motlle amb un nom molt llarg que no cap",
            status = ExpiryStatus.FROZEN,
            days = null,
            units = 2,
            detail = ProductRowDetail.WithDays,
            frozenSince = LocalDate(2026, 10, 4),
          ),
        )
      rows.forEachIndexed { index, row ->
        ProductRow(model = row, index = index, count = rows.size, onClick = {})
      }
    }
  }
}

// endregion

/** Rows counted in units, packs, grams and millilitres (the type decides what the second line says). */
@PreviewLightDark
@Composable
internal fun ProductRowUnitTypesPreview() {
  PreviewTheme {
    Column(
      modifier = Modifier.background(CaducityTheme.colorScheme.background).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
    ) {
      val rows =
        listOf(
          previewRow("Iogurt", ExpiryStatus.SOON, 2, 4, ProductRowDetail.WithDays),
          previewRow("Arròs", ExpiryStatus.FRESH, 9, 3, ProductRowDetail.WithDays, unitType = UnitType.PACKS),
          previewRow(
            "Espaguetis",
            ExpiryStatus.FRESH,
            20,
            1,
            ProductRowDetail.WithDays,
            unitType = UnitType.GRAMS,
            amount = 1200,
          ),
          previewRow(
            "Llet",
            ExpiryStatus.SOON,
            3,
            1,
            ProductRowDetail.WithDays,
            unitType = UnitType.MILLILITRES,
            amount = 750,
          ),
        )
      rows.forEachIndexed { index, row ->
        ProductRow(model = row, index = index, count = rows.size, onClick = {})
      }
    }
  }
}
