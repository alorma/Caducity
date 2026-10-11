package com.alorma.caducity.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.Close
import com.alorma.caducity.base.ui.icons.symbols.DeleteHistory
import com.alorma.caducity.base.ui.icons.symbols.Search
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.Widgets
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.ui.components.category.CategoryIcons
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.category.toComposeShape
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.NumberTextStyle
import com.alorma.caducity.ui.theme.colors.StatusColors
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

@Composable
internal fun HomeHeader(
  today: LocalDate,
  showActions: Boolean,
  onCalendar: () -> Unit,
  onSearch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(top = 28.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = today.headerText().uppercase(),
        style = CaducityTheme.typography.labelSmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = stringResource(R.string.home_title),
        style = CaducityTheme.typography.displayLarge,
        modifier = Modifier.semantics { heading() },
      )
    }
    if (showActions) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RoundHeaderButton(Symbols.Outlined.CalendarMonth, stringResource(R.string.home_calendar), onCalendar)
        RoundHeaderButton(Symbols.Outlined.Search, stringResource(R.string.home_search), onSearch)
      }
    }
  }
}

@Composable
private fun RoundHeaderButton(
  icon: ImageVector,
  description: String,
  onClick: () -> Unit,
) {
  IconButton(
    onClick = onClick,
    modifier = Modifier.size(48.dp).clip(CircleShape).background(CaducityTheme.colorScheme.surfaceContainerHigh),
  ) {
    Icon(icon, contentDescription = description)
  }
}

@Composable
internal fun ExpiredBanner(
  count: Int,
  oldestDaysAgo: Int,
  onReview: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = CaducityTheme.colors.expired
  Surface(
    onClick = onReview,
    modifier = modifier.fillMaxWidth(),
    shape = CaducityShapes.group,
    color = colors.container,
    contentColor = colors.onContainer,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier = Modifier.size(48.dp).clip(CategoryShape.BURST.toComposeShape()).background(colors.strong),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Symbols.Filled.DeleteHistory, contentDescription = null, tint = Color.White)
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = pluralStringResource(R.plurals.home_expired_banner_title, count, count),
          style = CaducityTheme.typography.bodyLarge,
        )
        Text(
          text = pluralStringResource(R.plurals.home_expired_banner_subtitle, oldestDaysAgo, oldestDaysAgo),
          style = CaducityTheme.typography.bodySmall,
        )
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.home_expired_banner_action), style = CaducityTheme.typography.labelLarge)
        Icon(Symbols.Outlined.ChevronRight, contentDescription = null)
      }
    }
  }
}

@Composable
internal fun SummaryRow(
  soon: Int,
  fresh: Int,
  frozen: Int,
  onSoon: () -> Unit,
  onFresh: () -> Unit,
  onFrozen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = CaducityTheme.colors
  val outer = 20.dp
  val inner = 8.dp
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
    SummaryCell(
      soon,
      stringResource(R.string.home_summary_soon),
      colors.soon,
      RoundedCornerShape(outer, inner, inner, outer),
      onSoon,
      Modifier.weight(1f),
    )
    SummaryCell(
      fresh,
      stringResource(R.string.home_summary_fresh),
      colors.fresh,
      RoundedCornerShape(inner),
      onFresh,
      Modifier.weight(1f),
    )
    SummaryCell(
      frozen,
      stringResource(R.string.home_summary_frozen),
      colors.frozen,
      RoundedCornerShape(inner, outer, outer, inner),
      onFrozen,
      Modifier.weight(1f),
    )
  }
}

@Composable
private fun SummaryCell(
  count: Int,
  label: String,
  colors: StatusColors,
  shape: Shape,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = shape,
    color = colors.container,
    contentColor = colors.onContainer,
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
      Text(count.toString(), style = NumberTextStyle)
      Text(label, style = CaducityTheme.typography.labelMedium)
    }
  }
}

@Composable
internal fun DaySectionHeader(
  section: HomeDaySection,
  today: LocalDate,
  modifier: Modifier = Modifier,
) {
  val days = today.daysUntil(section.date)
  val count = pluralStringResource(R.plurals.home_product_count, section.unitCount, section.unitCount)
  val title =
    when (days) {
      0 -> stringResource(R.string.home_section_today)
      1 -> stringResource(R.string.home_section_tomorrow)
      else -> section.date.weekdayDayText().capitalizeFirst()
    }
  val subtitle = if (days == 1) "${section.date.weekdayDayText()} · $count" else count
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(title, style = CaducityTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
    Text(
      subtitle,
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 2.dp),
    )
  }
}

@Composable
internal fun HomeItemRow(
  row: HomeRow,
  index: Int,
  count: Int,
  onClick: () -> Unit,
  onConsume: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val quantity =
    row.legacyPack?.let { stringResource(R.string.item_pack_of, it) }
      ?: pluralStringResource(R.plurals.home_unit_count, row.unitCount, row.unitCount)
  ConnectedRow(
    index = index,
    count = count,
    onClick = onClick,
    modifier = modifier.height(68.dp),
    contentPadding = PaddingValues(start = 12.dp, end = 12.dp),
  ) {
    CategoryShapeIcon(shape = row.category.shape, icon = CategoryIcons.vectorOf(row.category.icon), size = 44.dp)
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(row.name, style = CaducityTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
      Text(
        "${row.category.name} · $quantity",
        style = CaducityTheme.typography.bodySmall,
        color = CaducityTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    IconButton(
      onClick = onConsume,
      modifier = Modifier.size(44.dp).clip(CircleShape).background(CaducityTheme.colorScheme.surface),
    ) {
      Icon(Symbols.Outlined.Check, contentDescription = stringResource(R.string.home_mark_consumed, row.name))
    }
  }
}

@Composable
internal fun NothingSoon(
  onSeePantry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      stringResource(R.string.home_nothing_soon),
      style = CaducityTheme.typography.titleMedium,
    )
    Text(
      text = stringResource(R.string.home_nothing_soon_link),
      style = CaducityTheme.typography.labelLarge,
      color = CaducityTheme.colorScheme.primary,
      modifier = Modifier.clip(CaducityShapes.chip).clickable(onClick = onSeePantry).padding(8.dp),
    )
  }
}

@Composable
internal fun HomeEmptyState(
  onAdd: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth().padding(top = 56.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    EmptyShapes()
    Spacer(Modifier.height(36.dp))
    Text(
      stringResource(R.string.home_empty_title),
      style = CaducityTheme.typography.headlineMedium,
      modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(12.dp))
    Text(
      stringResource(R.string.home_empty_body),
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(28.dp))
    Button(onClick = onAdd, modifier = Modifier.height(56.dp)) {
      Icon(Symbols.Outlined.Add, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text(stringResource(R.string.home_empty_cta), style = CaducityTheme.typography.labelLarge)
    }
  }
}

/** Three overlapping shapes: Galeta (fresh), Sol (soon) and Flor (surface-shape). */
@Composable
private fun EmptyShapes(modifier: Modifier = Modifier) {
  val colors = CaducityTheme.colors
  Box(modifier = modifier.size(width = 160.dp, height = 120.dp)) {
    Box(
      Modifier
        .align(
          Alignment.CenterStart,
        ).size(84.dp)
        .clip(CategoryShape.COOKIE.toComposeShape())
        .background(colors.fresh.container),
    )
    Box(
      Modifier
        .align(
          Alignment.TopCenter,
        ).padding(start = 28.dp)
        .size(60.dp)
        .clip(CategoryShape.SUNNY.toComposeShape())
        .background(colors.soon.container),
    )
    Box(
      Modifier
        .align(
          Alignment.BottomEnd,
        ).size(64.dp)
        .clip(CategoryShape.CLOVER.toComposeShape())
        .background(colors.surfaceShape),
    )
  }
}

@Composable
internal fun WidgetTipCard(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = CaducityShapes.group,
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Row(
      modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Icon(Symbols.Outlined.Widgets, contentDescription = null, tint = CaducityTheme.colorScheme.primary)
      Text(
        stringResource(R.string.home_widget_tip),
        style = CaducityTheme.typography.bodyMedium,
        modifier = Modifier.weight(1f),
      )
      IconButton(onClick = onDismiss) {
        Icon(Symbols.Outlined.Close, contentDescription = stringResource(R.string.home_widget_tip_dismiss))
      }
    }
  }
}
