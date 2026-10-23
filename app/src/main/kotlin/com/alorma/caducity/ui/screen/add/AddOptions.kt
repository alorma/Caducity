package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.AddCircle
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.History
import com.alorma.caducity.base.ui.icons.symbols.Inventory2
import com.alorma.caducity.base.ui.icons.symbols.Kitchen
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.imageVector
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsSwitchCard
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.unit.icon
import com.alorma.caducity.ui.unit.label
import com.alorma.caducity.ui.unit.unitCountText
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toJavaLocalDate

private val OptionRowMinHeight = 68.dp

/** One line under the name: where the category and date come from (history) or what is still to pick (new). */
@Composable
fun AddStatusLine(
  fromHistory: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
      imageVector = if (fromHistory) Symbols.Outlined.History else Symbols.Outlined.AddCircle,
      contentDescription = null,
      tint = CaducityTheme.colorScheme.primary,
      modifier = Modifier.size(18.dp),
    )
    Text(
      text =
        if (fromHistory) {
          stringResource(R.string.add_status_history)
        } else {
          stringResource(R.string.add_status_new)
        },
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}

/**
 * Connected list of the product's options. Each row shows its value and opens a sheet; the two switches are
 * «Es pot congelar» (inherited from the category, overridable) and «Ja és congelat» (only if it can be frozen).
 */
@Composable
fun AddOptionsList(
  state: AddState,
  onCategoryClick: () -> Unit,
  onDateClick: () -> Unit,
  onQuantityClick: () -> Unit,
  onCanFreezeChange: (Boolean) -> Unit,
  onFrozenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val canFreeze = state.canFreeze
  val showFrozen = !state.isEditing && canFreeze == true
  val count = if (showFrozen) 5 else 4
  Column(modifier = modifier.fillMaxWidth()) {
    StyledSettingsGroup {
      CategoryRow(state, onCategoryClick, count)
      DateRow(state, onDateClick, count)
      QuantityRow(state, onQuantityClick, count)
      CanFreezeRow(state, onCanFreezeChange, count)
      if (showFrozen) {
        StyledSettingsSwitchCard(
          icon = { Icon(Symbols.Outlined.AcUnit, contentDescription = null, tint = frozenTint()) },
          title = stringResource(R.string.add_already_frozen),
          subtitle = stringResource(R.string.add_already_frozen_subtitle),
          state = state.frozen,
          onCheckedChange = onFrozenChange,
          shapes = ListItemDefaults.segmentedShapes(index = 4, count = count),
          colors = frozenRowColors(),
          modifier = Modifier.heightIn(min = OptionRowMinHeight),
        )
      }
    }
  }
}

@Composable
private fun frozenTint() = CaducityTheme.colors.frozen.onContainer

/** Row of a unit that is already frozen: the frozen container instead of the plain one. */
@Composable
private fun frozenContainerColors(): ListItemColors {
  val frozen = CaducityTheme.colors.frozen
  val scheme = CaducityTheme.colorScheme
  return ListItemDefaults.colors(
    containerColor = frozen.container,
    contentColor = scheme.onSurface,
    supportingContentColor = scheme.onSurfaceVariant,
    leadingContentColor = frozen.onContainer,
    trailingContentColor = frozen.onContainer,
  )
}

/** «fa 3 setmanes»: how long ago [date] was, in days, weeks or months. */
@Composable
private fun daysAgoText(
  date: LocalDate,
  today: LocalDate,
): String {
  val days = date.daysUntil(today).coerceAtLeast(0)
  return when {
    days == 0 -> stringResource(R.string.add_frozen_today)
    days == 1 -> stringResource(R.string.add_frozen_yesterday)
    days < DAYS_PER_WEEK -> pluralStringResource(R.plurals.add_frozen_days_ago, days, days)
    days < DAYS_PER_MONTH_RANGE ->
      (days / DAYS_PER_WEEK).let {
        pluralStringResource(R.plurals.add_frozen_weeks_ago, it, it)
      }
    else -> (days / DAYS_PER_MONTH).let { pluralStringResource(R.plurals.add_frozen_months_ago, it, it) }
  }
}

private const val DAYS_PER_WEEK = 7
private const val DAYS_PER_MONTH = 30
private const val DAYS_PER_MONTH_RANGE = 60

/** Under the options of an edit: what the actions of the list are for, and how to thaw a frozen unit. */
@Composable
fun AddEditNote(
  frozen: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (frozen) {
      Icon(
        imageVector = Symbols.Outlined.AcUnit,
        contentDescription = null,
        tint = frozenTint(),
        modifier = Modifier.size(18.dp),
      )
    }
    Text(
      text = stringResource(if (frozen) R.string.add_note_frozen else R.string.add_note_edit),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}

/** «Ja és congelat» switched on takes the frozen status colors instead of the green of a selected row. */
@Composable
private fun frozenRowColors(): ListItemColors {
  val frozen = CaducityTheme.colors.frozen
  val scheme = CaducityTheme.colorScheme
  return ListItemDefaults.colors(
    containerColor = scheme.surfaceContainer,
    contentColor = scheme.onSurface,
    supportingContentColor = scheme.onSurfaceVariant,
    leadingContentColor = frozen.onContainer,
    trailingContentColor = scheme.onSurfaceVariant,
    selectedContainerColor = frozen.container,
    selectedContentColor = frozen.onContainer,
    selectedSupportingContentColor = frozen.onContainer,
    selectedLeadingContentColor = frozen.onContainer,
    selectedTrailingContentColor = frozen.onContainer,
  )
}

@Composable
private fun CategoryRow(
  state: AddState,
  onClick: () -> Unit,
  count: Int,
) {
  val category = state.category
  StyledSettingsCard(
    icon = {
      if (category != null) {
        CategoryShapeIcon(shape = category.shape, icon = category.icon.imageVector, size = 40.dp)
      } else {
        Icon(Symbols.Outlined.Inventory2, contentDescription = null)
      }
    },
    title = stringResource(R.string.add_category_label),
    subtitle = if (category == null) stringResource(R.string.add_required) else null,
    action = {
      OptionValue(
        value = category?.name ?: stringResource(R.string.add_choose),
        highlight = category == null,
      )
    },
    onClick = onClick,
    shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
    modifier = Modifier.heightIn(min = OptionRowMinHeight),
  )
}

@Composable
private fun DateRow(
  state: AddState,
  onClick: () -> Unit,
  count: Int,
) {
  val date = state.date
  val frozenOn = state.frozenOn
  if (state.isEditing && state.frozen && frozenOn != null) {
    // A frozen unit shows when it went into the freezer; its remaining time is not edited here.
    StyledSettingsCard(
      icon = { Icon(Symbols.Outlined.AcUnit, contentDescription = null, tint = frozenTint()) },
      title = stringResource(R.string.add_frozen_on),
      subtitle = daysAgoText(frozenOn, state.today),
      action = {
        Text(
          text = shortDate(frozenOn),
          style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
          color = CaducityTheme.colors.frozen.onContainer,
        )
      },
      onClick = {},
      shapes = ListItemDefaults.segmentedShapes(index = 1, count = count),
      colors = frozenContainerColors(),
      modifier = Modifier.heightIn(min = OptionRowMinHeight),
    )
    return
  }
  StyledSettingsCard(
    icon = { Icon(Symbols.Outlined.CalendarMonth, contentDescription = null) },
    title = stringResource(R.string.add_date_label),
    subtitle = if (date == null) stringResource(R.string.add_required) else dateSubtitle(date, state.today),
    action = {
      OptionValue(
        value = date?.let { shortDate(it) } ?: stringResource(R.string.add_choose),
        highlight = date == null,
      )
    },
    onClick = onClick,
    shapes = ListItemDefaults.segmentedShapes(index = 1, count = count),
    modifier = Modifier.heightIn(min = OptionRowMinHeight),
  )
}

@Composable
private fun QuantityRow(
  state: AddState,
  onClick: () -> Unit,
  count: Int,
) {
  StyledSettingsCard(
    icon = { Icon(state.unitType.icon, contentDescription = null) },
    title = stringResource(R.string.add_quantity),
    action = {
      OptionValue(
        value =
          if (state.isEditing) {
            state.unitType.label()
          } else {
            unitCountText(
              unitType = state.unitType,
              count = state.quantity,
              amount = state.amount.takeIf { state.isMeasured },
              level = state.level.takeIf { state.isHandful },
            )
          },
      )
    },
    onClick = onClick,
    shapes = ListItemDefaults.segmentedShapes(index = 2, count = count),
    modifier = Modifier.heightIn(min = OptionRowMinHeight),
  )
}

@Composable
private fun CanFreezeRow(
  state: AddState,
  onCanFreezeChange: (Boolean) -> Unit,
  count: Int,
) {
  val category = state.category
  val overridden = state.canFreezeOverride != null || state.productCanFreeze != null
  StyledSettingsSwitchCard(
    icon = { Icon(Symbols.Outlined.Kitchen, contentDescription = null, tint = frozenTint()) },
    title = stringResource(R.string.add_can_freeze),
    subtitle =
      when {
        category == null -> stringResource(R.string.add_can_freeze_pending)
        overridden -> stringResource(R.string.add_can_freeze_only_this)
        else ->
          stringResource(
            R.string.add_can_freeze_subtitle,
            category.name,
            stringResource(if (category.canFreeze) R.string.add_can_freeze_yes else R.string.add_can_freeze_no),
          )
      },
    state = state.canFreeze ?: false,
    onCheckedChange = { if (category != null) onCanFreezeChange(it) },
    shapes = ListItemDefaults.segmentedShapes(index = 3, count = count),
    modifier = Modifier.heightIn(min = OptionRowMinHeight),
  )
}

@Composable
private fun OptionValue(
  value: String,
  highlight: Boolean = false,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = value,
      style = CaducityTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
      color = if (highlight) CaducityTheme.colorScheme.primary else CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Icon(Symbols.Outlined.ChevronRight, contentDescription = null)
  }
}

/** «diumenge · d'aquí 7 dies»; an already expired date says so. */
@Composable
private fun dateSubtitle(
  date: LocalDate,
  today: LocalDate,
): String {
  val weekday = date.dayOfWeek.localizedName()
  val days = today.daysUntil(date)
  return when {
    days < 0 -> stringResource(R.string.add_date_help_expired)
    days == 0 -> stringResource(R.string.add_date_row_today, weekday)
    days == 1 -> stringResource(R.string.add_date_row_tomorrow, weekday)
    else -> pluralStringResource(R.plurals.add_date_row_days, days, weekday, days)
  }
}

private fun shortDate(date: LocalDate): String =
  date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))

private fun DayOfWeek.localizedName(): String =
  java.time.DayOfWeek
    .of(ordinal + 1)
    .getDisplayName(TextStyle.FULL, Locale.getDefault())
