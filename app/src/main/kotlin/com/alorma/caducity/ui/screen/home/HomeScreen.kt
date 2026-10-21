package com.alorma.caducity.ui.screen.home

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HomeData
import com.alorma.caducity.domain.model.HomeDaySection
import com.alorma.caducity.domain.model.HomeRow
import com.alorma.caducity.domain.model.ProductRowDetail
import com.alorma.caducity.domain.model.ProductRowModel
import com.alorma.caducity.domain.usecase.WeekStripData
import com.alorma.caducity.feature.tracking.HomeScreen as HomeScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.show
import com.alorma.caducity.ui.components.product.ProductRow
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.weekstrip.WeekStrip
import com.alorma.caducity.ui.navigation.LocalFloatingBarPadding
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
  onOpenCalendar: (LocalDate?) -> Unit,
  onOpenSearch: () -> Unit,
  onOpenStatusList: (ExpiryStatus) -> Unit,
  onOpenPantry: () -> Unit,
  onOpenItemActions: (itemIds: List<String>) -> Unit,
  onOpenAdd: () -> Unit,
  onOpenTransform: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = koinViewModel(),
) {
  TrackScreen(screen = HomeScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  ReportDrawnWhen { state is HomeState.Ready }

  val snackbarState = rememberAppSnackbarState()
  val resources = LocalResources.current
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is HomeSideEffect.ShowSnackbar -> launch { snackbarState.show(effect.request, resources) }
      }
    }
  }

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        is HomeNavigationSideEffect.OpenCalendar -> onOpenCalendar(effect.date)
        HomeNavigationSideEffect.OpenSearch -> onOpenSearch()
        is HomeNavigationSideEffect.OpenStatusList -> onOpenStatusList(effect.status)
        HomeNavigationSideEffect.OpenPantry -> onOpenPantry()
        is HomeNavigationSideEffect.OpenItemActions -> onOpenItemActions(effect.itemIds)
        HomeNavigationSideEffect.OpenAdd -> onOpenAdd()
        HomeNavigationSideEffect.OpenTransform -> onOpenTransform()
      }
    }
  }

  when (val current = state) {
    HomeState.Loading -> HomeLoading(today = koinInject<TodayProvider>().today(), modifier = modifier)
    is HomeState.Ready ->
      HomeContent(
        state = current,
        onNavigate = viewModel::navigate,
        onConsume = viewModel::onConsume,
        onFreeze = viewModel::onFreeze,
        modifier = modifier,
        snackbarState = snackbarState,
      )
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

  data class More(
    val count: Int,
  ) : HomeItem {
    override val key = "more"
  }

  data object SeePantry : HomeItem {
    override val key = "see-pantry"
  }

  data object Cooked : HomeItem {
    override val key = "cooked"
  }

  data object Empty : HomeItem {
    override val key = "empty"
  }
}

private fun HomeState.Ready.items(): List<HomeItem> =
  buildList {
    if (data.isPantryEmpty) {
      add(HomeItem.Empty)
      return@buildList
    }
    if (data.expiredCount > 0) add(HomeItem.Banner)
    add(HomeItem.Summary)
    (data.sections + data.upcoming).forEach { section ->
      add(HomeItem.SectionHeader(section))
      section.rows.forEachIndexed { index, row -> add(HomeItem.Row(section, row, index)) }
    }
    if (data.upcomingMoreCount > 0) add(HomeItem.More(data.upcomingMoreCount))
    if (data.hasUsableProducts) add(HomeItem.Cooked)
    if (data.nothingExpiresSoon) add(HomeItem.SeePantry)
  }

@Composable
internal fun HomeContent(
  state: HomeState.Ready,
  onNavigate: (HomeNavigation) -> Unit,
  onConsume: (HomeRow) -> Unit,
  onFreeze: (HomeRow) -> Unit,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
  snackbarState: AppSnackbarState = rememberAppSnackbarState(),
) {
  val data = state.data
  val items = state.items()
  val scope = rememberCoroutineScope()
  var selectedDay by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf<LocalDate?>(null) }

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  // The floating bar overlays the content, so the scaffold reserves no space at the bottom.
  AppScaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    snackbarState = snackbarState,
    contentWindowInsets = WindowInsets(0.dp),
    topBar = {
      ResponsiveCenteredContainer(fillHeight = false) {
        HomeHeader(
          today = data.today,
          showActions = !data.isPantryEmpty,
          onCalendar = { onNavigate(HomeNavigation.Calendar()) },
          onSearch = { onNavigate(HomeNavigation.Search) },
          scrollBehavior = scrollBehavior,
        )
      }
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      Column(modifier = Modifier.fillMaxSize()) {
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
          items(items, key = { it.key }, contentType = { it::class }) { item ->
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
                ProductRow(
                  model = item.row.toProductRow(item.section, data.today),
                  index = item.index,
                  count = item.section.rows.size,
                  onClick = { onNavigate(HomeNavigation.ItemActions(item.row.itemIds)) },
                  onConsume = { onConsume(item.row) },
                  onFreeze = { onFreeze(item.row) },
                )
              is HomeItem.More -> MoreRows(count = item.count)
              HomeItem.SeePantry -> SeePantry(onSeePantry = { onNavigate(HomeNavigation.Pantry("see_pantry")) })
              HomeItem.Cooked -> CookedCard(onClick = { onNavigate(HomeNavigation.Transform("cooked_card")) })
              HomeItem.Empty -> HomeEmptyState(onAdd = { onNavigate(HomeNavigation.AddFirst) })
            }
          }
        }
      }
    }
  }
}

/** While the first data arrives: the fixed parts of the screen (header, week strip, status blocks) without data. */
@Composable
internal fun HomeLoading(
  today: LocalDate,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets(0.dp),
    topBar = {
      ResponsiveCenteredContainer(fillHeight = false) {
        HomeHeader(today = today, showActions = true, onCalendar = {}, onSearch = {})
      }
    },
  ) { padding ->
    ResponsiveCenteredContainer(modifier = Modifier.padding(padding)) {
      Column(modifier = Modifier.fillMaxSize()) {
        WeekStrip(
          data = WeekStripData(today, emptyMap()),
          selected = null,
          onDayClick = {},
          modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
          contentPadding = PaddingValues(horizontal = 20.dp),
        )
        SummaryRow(
          soon = 0,
          fresh = 0,
          frozen = 0,
          onSoon = {},
          onFresh = {},
          onFrozen = {},
          modifier = Modifier.padding(horizontal = 20.dp),
        )
      }
    }
  }
}

/** Every row has the status of its section: the days left are the distance from today to that day. */
private fun HomeRow.toProductRow(
  section: HomeDaySection,
  today: LocalDate,
): ProductRowModel =
  ProductRowModel(
    key = key,
    name = name,
    itemIds = itemIds,
    status = section.status,
    remainingDays = today.daysUntil(section.date),
    category = category,
    detail = ProductRowDetail.InCategory(category.name),
    canFreeze = canFreeze,
  )

private val ConnectedGap = 2.dp

private val LocalDateSaver =
  Saver<LocalDate?, String>(save = {
    it?.toString() ?: ""
  }, restore = { if (it.isEmpty()) null else LocalDate.parse(it) })

// region Previews

private val today = LocalDate(2026, 10, 11)
private val dairy = Category("dairy", "Làctics", "", CategoryShape.COOKIE, CategoryIcon("water_full"))
private val produce = Category("produce", "Fruita i verdura", "", CategoryShape.COOKIE7, CategoryIcon("nutrition"))

private fun row(
  name: String,
  category: Category,
  units: Int = 1,
) = HomeRow(name, name, category, List(units) { "$name-$it" })

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
  HomeContent(HomeState.Ready(sampleData, WeekStripData(today, emptyMap())), {}, {}, {})
}

@PreviewLightDark
@Composable
private fun HomeNothingSoonPreview() {
  PreviewTheme { HomeNothingSoonPreviewContent() }
}

/** Inici amb «0 Aviat»: properes caducitats, «+N més» i enllaç al Rebost (sense tema, per a les captures). */
@Composable
internal fun HomeNothingSoonPreviewContent() {
  HomeContent(
    HomeState.Ready(
      sampleData.copy(
        expiredCount = 0,
        soonCount = 0,
        sections = emptyList(),
        upcoming =
          listOf(
            HomeDaySection(LocalDate(2026, 10, 15), listOf(row("Iogurt", dairy, units = 4)), ExpiryStatus.FRESH),
            HomeDaySection(
              LocalDate(2026, 10, 17),
              listOf(row("Hummus", produce), row("Pa de motlle", dairy)),
              ExpiryStatus.FRESH,
            ),
          ),
        upcomingMoreCount = 3,
      ),
      WeekStripData(today, emptyMap()),
    ),
    {},
    {},
    {},
  )
}

@PreviewLightDark
@Composable
private fun HomeEmptyPreview() {
  PreviewTheme { HomeEmptyPreviewContent() }
}

/** Inici amb el rebost buit (sense tema, per a les captures). */
@Composable
internal fun HomeEmptyPreviewContent() {
  HomeContent(
    HomeState.Ready(
      HomeData(today, 0, null, 0, 0, 0, emptyList(), isPantryEmpty = true),
      WeekStripData(today, emptyMap()),
    ),
    {},
    {},
    {},
  )
}

// endregion
