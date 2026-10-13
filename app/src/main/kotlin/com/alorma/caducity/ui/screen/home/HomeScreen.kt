package com.alorma.caducity.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HomeData
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.domain.model.HomeLaterRow
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.domain.usecase.WeekStripData
import com.alorma.caducity.feature.tracking.HomeScreen as HomeScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.weekstrip.WeekStrip
import com.alorma.caducity.ui.navigation.LocalFloatingBarPadding
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
  onOpenCalendar: (LocalDate?) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenStatusList: (ExpiryStatus) -> Unit,
  onOpenPantry: () -> Unit,
  onOpenItemActions: (itemId: String) -> Unit,
  onOpenAdd: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = koinViewModel(),
) {
  TrackScreen(screen = HomeScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is HomeNavigationSideEffect.OpenCalendar -> onOpenCalendar(effect.date)
        HomeNavigationSideEffect.OpenSearch -> onOpenSearch()
        is HomeNavigationSideEffect.OpenStatusList -> onOpenStatusList(effect.status)
        HomeNavigationSideEffect.OpenPantry -> onOpenPantry()
        is HomeNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemId)
        HomeNavigationSideEffect.OpenAdd -> onOpenAdd()
      }
    }
  }

  ResponsiveCenteredContainer(modifier = modifier) {
    when (val current = state) {
      HomeState.Loading -> Unit
      is HomeState.Ready ->
        HomeContent(
          state = current,
          onNavigate = viewModel::navigate,
          onConsume = viewModel::onConsume,
        )
    }
  }
}

/** One entry of the lazy list; building the list first makes «scroll to the first section» a plain index. */
private sealed interface HomeItem {
  val key: String

  data object Banner : HomeItem {
    override val key = "banner"
  }

  data object Summary : HomeItem {
    override val key = "summary"
  }

  data class SectionHeader(
    val section: HomeDaySection,
  ) : HomeItem {
    override val key = "section-${section.date}"
  }

  data class Row(
    val section: HomeDaySection,
    val row: HomeRow,
    val index: Int,
  ) : HomeItem {
    override val key = "row-${section.date}-${row.key}"
  }

  data object LaterHeader : HomeItem {
    override val key = "later-header"
  }

  data object LaterCard : HomeItem {
    override val key = "later-card"
  }

  data class LaterRow(
    val row: HomeLaterRow,
    val index: Int,
    val count: Int,
  ) : HomeItem {
    override val key = "later-${row.date}-${row.row.key}"
  }

  data object LaterHelp : HomeItem {
    override val key = "later-help"
  }

  data object NothingSoon : HomeItem {
    override val key = "nothing-soon"
  }

  data object Empty : HomeItem {
    override val key = "empty"
  }
}

private fun HomeState.Ready.items(laterExpanded: Boolean): List<HomeItem> =
  buildList {
    if (data.isPantryEmpty) {
      add(HomeItem.Empty)
      return@buildList
    }
    if (data.expiredCount > 0) add(HomeItem.Banner)
    add(HomeItem.Summary)
    if (data.nothingExpiresSoon) add(HomeItem.NothingSoon)
    data.sections.forEach { section ->
      add(HomeItem.SectionHeader(section))
      section.rows.forEachIndexed { index, row -> add(HomeItem.Row(section, row, index)) }
    }
    data.later?.let { later ->
      add(HomeItem.LaterHeader)
      add(HomeItem.LaterCard)
      if (laterExpanded) {
        later.rows.forEachIndexed { index, row -> add(HomeItem.LaterRow(row, index, later.rows.size)) }
      }
      add(HomeItem.LaterHelp)
    }
  }

@Composable
internal fun HomeContent(
  state: HomeState.Ready,
  onNavigate: (HomeNavigation) -> Unit,
  onConsume: (HomeRow) -> Unit,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
) {
  val data = state.data
  var laterExpanded by rememberSaveable { mutableStateOf(false) }
  val items = state.items(laterExpanded)
  val scope = rememberCoroutineScope()
  var selectedDay by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf<LocalDate?>(null) }

  Column(modifier = modifier.fillMaxSize()) {
    HomeHeader(
      today = data.today,
      showActions = !data.isPantryEmpty,
      onCalendar = { onNavigate(HomeNavigation.Calendar()) },
      onSearch = { onNavigate(HomeNavigation.Search) },
    )
    if (!data.isPantryEmpty) {
      WeekStrip(
        data = state.weekStrip,
        selected = selectedDay,
        onDayClick = { date ->
          val sectionIndex = items.indexOfFirst { it is HomeItem.SectionHeader && it.section.date == date }
          when {
            selectedDay == date -> selectedDay = null
            sectionIndex >= 0 -> {
              selectedDay = date
              scope.launch { listState.animateScrollToItem(sectionIndex) }
            }
            // No «soon» section that day: open the month view on it (P18).
            else -> onNavigate(HomeNavigation.Calendar(date = date, source = "week_strip"))
          }
        },
        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
      )
    }
    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize(),
      contentPadding =
        PaddingValues(
          start = 20.dp,
          end = 20.dp,
          bottom = LocalFloatingBarPadding.current.calculateBottomPadding() + 24.dp,
        ),
      verticalArrangement = Arrangement.spacedBy(ConnectedGap),
    ) {
      itemsIndexed(items, key = { _, item -> item.key }) { _, item ->
        when (item) {
          HomeItem.Banner ->
            ExpiredBanner(
              count = data.expiredCount,
              oldestDaysAgo = data.oldestExpiredDaysAgo ?: 0,
              onReview = { onNavigate(HomeNavigation.StatusList(ExpiryStatus.EXPIRED, "banner")) },
              modifier = Modifier.padding(bottom = 12.dp),
            )
          HomeItem.Summary ->
            SummaryRow(
              soon = data.soonCount,
              fresh = data.freshCount,
              frozen = data.frozenCount,
              onSoon = { onNavigate(HomeNavigation.StatusList(ExpiryStatus.SOON, "summary")) },
              onFresh = { onNavigate(HomeNavigation.StatusList(ExpiryStatus.FRESH, "summary")) },
              onFrozen = { onNavigate(HomeNavigation.StatusList(ExpiryStatus.FROZEN, "summary")) },
              modifier = Modifier.padding(bottom = 12.dp),
            )
          is HomeItem.SectionHeader ->
            DaySectionHeader(
              section = item.section,
              today = data.today,
              modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
            )
          is HomeItem.Row ->
            HomeItemRow(
              row = item.row,
              index = item.index,
              count = item.section.rows.size,
              onClick = { onNavigate(HomeNavigation.ItemActions(item.row.nearestItemId)) },
              onConsume = { onConsume(item.row) },
            )
          HomeItem.LaterHeader -> data.later?.let { LaterHeader(it, Modifier.padding(top = 16.dp, bottom = 6.dp)) }
          HomeItem.LaterCard ->
            data.later?.let { LaterCollapsedCard(it, laterExpanded, onToggle = { laterExpanded = !laterExpanded }) }
          is HomeItem.LaterRow ->
            HomeItemRow(
              row = item.row.row,
              index = item.index,
              count = item.count,
              onClick = { onNavigate(HomeNavigation.ItemActions(item.row.row.nearestItemId)) },
              onConsume = { onConsume(item.row.row) },
              meta = item.row.date.weekdayShortDateText(),
            )
          HomeItem.LaterHelp -> data.later?.let { LaterHelp(it, Modifier.padding(top = 8.dp)) }
          HomeItem.NothingSoon -> NothingSoon(onSeePantry = { onNavigate(HomeNavigation.Pantry("nothing_soon")) })
          HomeItem.Empty -> HomeEmptyState(onAdd = { onNavigate(HomeNavigation.AddFirst) })
        }
      }
    }
  }
}

private val ConnectedGap = 2.dp

private val LocalDateSaver =
  Saver<LocalDate?, String>(save = {
    it?.toString() ?: ""
  }, restore = { if (it.isEmpty()) null else LocalDate.parse(it) })

// region Previews

private val today = LocalDate(2026, 10, 11)
private val dairy = Category("dairy", "Làctics", "", CategoryShape.COOKIE, CategoryIcon("water_full"))
private val produce = Category("produce", "Fruita i verdura", "", CategoryShape.CLOVER8, CategoryIcon("nutrition"))

private fun row(
  name: String,
  category: Category,
  units: Int = 1,
) = HomeRow(name, name, category, units, name, null)

private val sampleData =
  HomeData(
    today = today,
    expiredCount = 7,
    oldestExpiredDaysAgo = 5,
    soonCount = 9,
    freshCount = 42,
    frozenCount = 3,
    sections =
      listOf(
        HomeDaySection(today, listOf(row("Llet sencera", dairy), row("Espinacs", produce))),
        HomeDaySection(LocalDate(2026, 10, 12), listOf(row("Iogurt", dairy, units = 3), row("Maduixes", produce))),
      ),
    later = null,
    isPantryEmpty = false,
  )

@PreviewLightDark
@Composable
private fun HomePreview() {
  PreviewTheme { HomePreviewContent() }
}

/** Inici amb dades d'exemple, sense tema (el posa qui el crida: previews i captures per paleta). */
@Composable
internal fun HomePreviewContent() {
  HomeContent(HomeState.Ready(sampleData, WeekStripData(today, emptyMap())), {}, {})
}

@PreviewLightDark
@Composable
private fun HomeNothingSoonPreview() {
  PreviewTheme {
    HomeContent(
      HomeState.Ready(
        sampleData.copy(expiredCount = 0, sections = emptyList()),
        WeekStripData(today, emptyMap()),
      ),
      {},
      {},
    )
  }
}

@PreviewLightDark
@Composable
private fun HomeEmptyPreview() {
  PreviewTheme {
    HomeContent(
      HomeState.Ready(
        HomeData(today, 0, null, 0, 0, 0, emptyList(), null, isPantryEmpty = true),
        WeekStripData(today, emptyMap()),
      ),
      {},
      {},
    )
  }
}

// endregion
