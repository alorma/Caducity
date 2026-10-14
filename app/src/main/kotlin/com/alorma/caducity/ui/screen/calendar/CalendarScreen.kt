package com.alorma.caducity.ui.screen.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ArrowBack
import com.alorma.caducity.base.ui.icons.symbols.ChevronLeft
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.feature.tracking.CalendarScreen as CalendarScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.product.ProductRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityShapes
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.colors
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CalendarScreen(
  args: CalendarArgs,
  onBack: () -> Unit,
  onOpenItemActions: (itemIds: List<String>) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CalendarViewModel = koinViewModel { parametersOf(args) },
) {
  TrackScreen(screen = CalendarScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        CalendarNavigationSideEffect.Back -> onBack()
        is CalendarNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemIds)
      }
    }
  }

  when (val current = state) {
    CalendarState.Loading -> Unit
    is CalendarState.Ready ->
      CalendarContent(
        state = current,
        onBack = { viewModel.navigate(CalendarNavigation.Back) },
        onMonthShown = viewModel::onMonthShown,
        onDayClick = viewModel::onDayClick,
        onRowClick = { viewModel.navigate(CalendarNavigation.ItemActions(it)) },
        onConsume = viewModel::onConsume,
        onFreeze = viewModel::onFreeze,
        onDiscard = viewModel::onDiscard,
        modifier = modifier,
      )
  }
}

@Composable
internal fun CalendarContent(
  state: CalendarState.Ready,
  onBack: () -> Unit,
  onMonthShown: (LocalDate) -> Unit,
  onDayClick: (LocalDate) -> Unit,
  onRowClick: (List<String>) -> Unit,
  onConsume: (ProductRowModel) -> Unit,
  onFreeze: (ProductRowModel) -> Unit,
  onDiscard: (ProductRowModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Page MonthPages is the month the screen opened on; the pager reaches MonthPages months either way.
  val base = remember { state.month.first }
  val pagerState = rememberPagerState(initialPage = MonthPages, pageCount = { MonthPages * 2 + 1 })
  val scope = rememberCoroutineScope()
  val currentOnMonthShown by rememberUpdatedState(onMonthShown)
  LaunchedEffect(pagerState) {
    snapshotFlow { pagerState.settledPage }.collect { currentOnMonthShown(monthAt(base, it)) }
  }
  AppScaffold(
    modifier = modifier,
    topBar = {
      ResponsiveCenteredContainer(fillHeight = false) {
        CalendarTopBar(
          title = monthAt(base, pagerState.currentPage).monthTitle(),
          categoryName = state.categoryName,
          onBack = onBack,
          onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
          onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
        )
      }
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing),
      ) {
        item(key = "month") {
          MonthCard(
            state = state,
            pagerState = pagerState,
            base = base,
            onDayClick = onDayClick,
            modifier = Modifier.padding(bottom = 12.dp),
          )
        }
        item(key = "day-title") {
          DayTitle(state = state, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
        }
        if (state.rows.isEmpty()) {
          item(key = "empty") {
            Text(
              text = stringResource(R.string.calendar_day_empty),
              style = CaducityTheme.typography.bodyMedium,
              color = CaducityTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            )
          }
        }
        itemsIndexed(state.rows, key = { _, row -> row.key }) { index, row ->
          ProductRow(
            model = row,
            index = index,
            count = state.rows.size,
            onClick = { onRowClick(row.itemIds) },
            onConsume = { onConsume(row) },
            onFreeze = { onFreeze(row) },
            onDiscard = { onDiscard(row) },
          )
        }
      }
    }
  }
}

@Composable
private fun CalendarTopBar(
  title: String,
  categoryName: String?,
  onBack: () -> Unit,
  onPrevious: () -> Unit,
  onNext: () -> Unit,
) {
  StyledTopAppBar(
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(Symbols.Outlined.ArrowBack, contentDescription = stringResource(R.string.calendar_back))
      }
    },
    title = {
      Column {
        Text(
          text = title,
          style = CaducityTheme.typography.headlineMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.semantics { heading() },
        )
        categoryName?.let {
          Text(
            text = it,
            style = CaducityTheme.typography.bodyMedium,
            color = CaducityTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    actions = {
      IconButton(onClick = onPrevious) {
        Icon(Symbols.Outlined.ChevronLeft, contentDescription = stringResource(R.string.calendar_prev_month))
      }
      IconButton(onClick = onNext) {
        Icon(Symbols.Outlined.ChevronRight, contentDescription = stringResource(R.string.calendar_next_month))
      }
    },
  )
}

@Composable
private fun MonthCard(
  state: CalendarState.Ready,
  pagerState: PagerState,
  base: LocalDate,
  onDayClick: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = CaducityShapes.group,
    color = CaducityTheme.colorScheme.surfaceContainer,
  ) {
    Column(modifier = Modifier.padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)) {
      HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
        val month = CalendarMonthModel.of(monthAt(base, page), state.month.firstDayOfWeek)
        Column {
          Row(modifier = Modifier.fillMaxWidth()) {
            month.weekdays.forEach { weekday ->
              Text(
                text = weekday.initial(),
                style = CaducityTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                color = CaducityTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
              )
            }
          }
          month.weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
              week.forEach { date ->
                if (date == null) {
                  Spacer(Modifier.weight(1f).height(CellHeight))
                } else {
                  DayCell(
                    date = date,
                    statuses = state.statusesByDate[date].orEmpty(),
                    isToday = date == state.today,
                    isSelected = date == state.selected,
                    isPast = date < state.today,
                    onClick = { onDayClick(date) },
                    modifier = Modifier.weight(1f),
                  )
                }
              }
            }
          }
          // Every month takes 6 rows so the card does not change height while paging.
          repeat(6 - month.weeks.size) { Spacer(Modifier.height(CellHeight)) }
        }
      }
      Legend(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
    }
  }
}

@Composable
private fun DayCell(
  date: LocalDate,
  statuses: List<ExpiryStatus>,
  isToday: Boolean,
  isSelected: Boolean,
  isPast: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colorScheme = CaducityTheme.colorScheme
  val hasItems = statuses.isNotEmpty()
  val description =
    if (hasItems) {
      pluralStringResource(R.plurals.calendar_cell_desc, statuses.size, date.dayMonthText(), statuses.size)
    } else {
      stringResource(R.string.calendar_cell_desc_none, date.dayMonthText())
    }
  Box(
    modifier =
      modifier
        .height(CellHeight)
        .clip(CaducityShapes.field)
        .semantics {
          role = Role.Button
          this.selected = isSelected
          contentDescription = description
        }.clickable(onClick = onClick),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 2.dp)) {
      // Today and selected are drawn on this circle only, never on the whole cell. Selected wins over today.
      Box(
        modifier =
          Modifier
            .size(DayCircle)
            .clip(CircleShape)
            .then(
              when {
                isSelected -> Modifier.background(colorScheme.primary)
                isToday -> Modifier.border(BorderStroke(2.dp, colorScheme.onSurface), CircleShape)
                else -> Modifier
              },
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = date.day.toString(),
          style = CaducityTheme.typography.bodyMedium.copy(fontSize = 15.sp),
          fontWeight = if (hasItems || isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
          color =
            when {
              isSelected -> colorScheme.onPrimary
              isPast && !hasItems && !isToday -> colorScheme.outline
              else -> colorScheme.onSurface
            },
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp).height(5.dp)) {
        statuses.take(3).forEach { status ->
          Box(Modifier.size(5.dp).clip(CircleShape).background(status.colors().strong))
        }
      }
    }
  }
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
  ) {
    LegendItem(ExpiryStatus.EXPIRED, stringResource(R.string.calendar_legend_expired))
    LegendItem(ExpiryStatus.SOON, stringResource(R.string.calendar_legend_soon))
    LegendItem(ExpiryStatus.FRESH, stringResource(R.string.calendar_legend_fresh))
  }
}

@Composable
private fun LegendItem(
  status: ExpiryStatus,
  label: String,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(status.colors().strong))
    Spacer(Modifier.width(6.dp))
    Text(
      text = label,
      style = CaducityTheme.typography.bodySmall.copy(fontSize = 12.sp),
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun DayTitle(
  state: CalendarState.Ready,
  modifier: Modifier = Modifier,
) {
  val days = state.today.daysUntil(state.selected)
  val title =
    when (days) {
      0 -> stringResource(R.string.calendar_relative_today)
      1 -> stringResource(R.string.calendar_relative_tomorrow)
      else -> state.selected.dayTitle()
    }
  // «Avui» / «Demà» already say when it is, so their subtitle gives the date instead.
  val whenText =
    when {
      days == 0 || days == 1 -> state.selected.dayTitle().replaceFirstChar { it.lowercase() }
      days > 1 -> pluralStringResource(R.plurals.calendar_relative_in, days, days)
      else -> pluralStringResource(R.plurals.calendar_relative_ago, -days, -days)
    }
  val subtitle =
    if (state.rows.isEmpty()) {
      whenText
    } else {
      val products = state.rows.size
      "$whenText · ${pluralStringResource(R.plurals.home_product_count, products, products)}"
    }
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Text(
      text = title,
      style = CaducityTheme.typography.headlineSmall,
      modifier = Modifier.semantics { heading() },
    )
    Text(
      text = subtitle,
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(bottom = 2.dp),
    )
  }
}

private val CellHeight = 52.dp
private val DayCircle = 34.dp
private const val MonthPages = 1200

private fun monthAt(
  base: LocalDate,
  page: Int,
): LocalDate = base.plus(DatePeriod(months = page - MonthPages))

/** «dl», «dt»… */
private fun DayOfWeek.initial(locale: Locale = Locale.getDefault()): String =
  java.time.DayOfWeek
    .of(isoDayNumber)
    .getDisplayName(TextStyle.SHORT, locale)
    .removeSuffix(".")
    .take(2)
    .lowercase(locale)

/** «Octubre 2026». */
private fun LocalDate.monthTitle(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("LLLL yyyy", locale).format(toJavaLocalDate()).replaceFirstChar { it.titlecase(locale) }

/** «Dimecres 21». */
private fun LocalDate.dayTitle(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("EEEE d", locale).format(toJavaLocalDate()).replaceFirstChar { it.titlecase(locale) }

/** «21 d'octubre». */
private fun LocalDate.dayMonthText(locale: Locale = Locale.getDefault()): String =
  DateTimeFormatter.ofPattern("d MMMM", locale).format(toJavaLocalDate())

// region Previews

private val previewToday = LocalDate(2026, 10, 11)
private val previewCategory = Category("dairy", "Làctics", "", CategoryShape.COOKIE, CategoryIcon("water_full"))

private fun previewRow(
  name: String,
  status: ExpiryStatus,
  days: Int,
  units: Int,
) = ProductRowModel(
  key = name,
  name = name,
  itemIds = List(units) { "$name-$it" },
  status = status,
  remainingDays = days,
  category = previewCategory,
  detail = ProductRowDetail.WithDays,
)

@PreviewLightDark
@Composable
private fun CalendarPreview() {
  PreviewTheme {
    Box(Modifier.background(CaducityTheme.colorScheme.background)) {
      CalendarContent(
        state =
          CalendarState.Ready(
            month = CalendarMonthModel.of(previewToday, DayOfWeek.MONDAY),
            today = previewToday,
            selected = LocalDate(2026, 10, 21),
            categoryName = null,
            statusesByDate =
              mapOf(
                LocalDate(2026, 10, 8) to listOf(ExpiryStatus.EXPIRED),
                LocalDate(2026, 10, 10) to List(3) { ExpiryStatus.EXPIRED },
                previewToday to listOf(ExpiryStatus.SOON, ExpiryStatus.SOON),
                LocalDate(2026, 10, 12) to List(3) { ExpiryStatus.SOON },
                LocalDate(2026, 10, 21) to listOf(ExpiryStatus.FRESH, ExpiryStatus.FRESH),
              ),
            rows =
              listOf(
                previewRow("Formatge cheddar", ExpiryStatus.FRESH, 10, units = 3),
                previewRow("Salsa pesto", ExpiryStatus.FRESH, 10, units = 1),
              ),
          ),
        onBack = {},
        onMonthShown = {},
        onDayClick = {},
        onRowClick = {},
        onConsume = {},
        onFreeze = {},
        onDiscard = {},
      )
    }
  }
}

// endregion
