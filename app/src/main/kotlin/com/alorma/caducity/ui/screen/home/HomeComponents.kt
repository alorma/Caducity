package com.alorma.caducity.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.Add
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.DeleteHistory
import com.alorma.caducity.base.ui.icons.symbols.Search
import com.alorma.caducity.base.ui.icons.symbols.SoupKitchen
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.ui.components.category.toComposeShape
import com.alorma.caducity.ui.components.topbar.StyledLargeTopAppBar
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
  scrollBehavior: TopAppBarScrollBehavior? = null,
) {
  StyledLargeTopAppBar(
    modifier = modifier,
    scrollBehavior = scrollBehavior,
    title = { Text(text = stringResource(R.string.home_title), modifier = Modifier.semantics { heading() }) },
    subtitle = { Text(text = today.headerText().capitalizeFirst()) },
    actions = {
      if (showActions) {
        IconButton(onClick = onCalendar) {
          Icon(Symbols.Outlined.CalendarMonth, contentDescription = stringResource(R.string.home_calendar))
        }
        IconButton(onClick = onSearch) {
          Icon(Symbols.Outlined.Search, contentDescription = stringResource(R.string.home_search))
        }
      }
    },
  )
}

@Composable
internal fun ExpiredBanner(
  count: Int,
  oldestDaysAgo: Int,
  onReview: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = CaducityTheme.colors.expired
  Card(
    onClick = onReview,
    modifier = modifier.fillMaxWidth(),
    shape = CaducityShapes.group,
    colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.onContainer),
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
  val description =
    stringResource(
      R.string.home_summary_cell_cd,
      label,
      pluralStringResource(R.plurals.home_product_count, count, count),
    )
  // With nothing in it the cell does not navigate (no ripple either).
  Card(
    onClick = onClick,
    enabled = count > 0,
    modifier = modifier.semantics { contentDescription = description },
    shape = shape,
    colors =
      CardDefaults.cardColors(
        containerColor = colors.container,
        contentColor = colors.onContainer,
        disabledContainerColor = colors.container,
        disabledContentColor = colors.onContainer,
      ),
  ) {
    // Three equal-width cells: cap the font scale so labels like «Congelats» never break mid-word.
    val density = LocalDensity.current
    CompositionLocalProvider(
      LocalDensity provides Density(density.density, minOf(density.fontScale, MaxCellFontScale)),
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(count.toString(), style = NumberTextStyle)
        Text(label, style = CaducityTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
      }
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
  val count = pluralStringResource(R.plurals.home_product_count, section.productCount, section.productCount)
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

/** «+N més»: rows of the upcoming days that were left out. */
@Composable
internal fun MoreRows(
  count: Int,
  modifier: Modifier = Modifier,
) {
  Text(
    text = stringResource(R.string.home_more_rows, count),
    style = CaducityTheme.typography.bodyMedium,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
    modifier = modifier.fillMaxWidth().padding(top = 12.dp),
  )
}

/** Link to the pantry, last element of the list when nothing is soon. */
@Composable
internal fun SeePantry(
  onSeePantry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
    TextButton(onClick = onSeePantry) {
      Text(text = stringResource(R.string.home_see_pantry))
      Icon(imageVector = Symbols.Outlined.ChevronRight, contentDescription = null, modifier = Modifier.size(20.dp))
    }
  }
}

/** «Has cuinat?»: opens an empty Transforma. Always the last row of the list when there is something usable. */
@Composable
internal fun CookedCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colors = CaducityTheme.colors.fresh
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = colors.container, contentColor = colors.onContainer),
    modifier = modifier.fillMaxWidth().padding(top = 16.dp),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier = Modifier.size(48.dp).clip(CaducityShapes.chip).background(CaducityTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Symbols.Outlined.SoupKitchen, contentDescription = null, modifier = Modifier.size(28.dp))
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(R.string.home_cooked_title),
          style = CaducityTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = stringResource(R.string.home_cooked_body),
          style = CaducityTheme.typography.bodyMedium,
        )
      }
      Icon(Symbols.Outlined.ChevronRight, contentDescription = null)
    }
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

private const val MaxCellFontScale = 1.3f
