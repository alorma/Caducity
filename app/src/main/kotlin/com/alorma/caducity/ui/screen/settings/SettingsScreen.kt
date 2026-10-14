package com.alorma.caducity.ui.screen.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.symbols.ChevronRight
import com.alorma.caducity.base.ui.icons.symbols.CloudUpload
import com.alorma.caducity.base.ui.icons.symbols.Contrast
import com.alorma.caducity.base.ui.icons.symbols.EventAvailable
import com.alorma.caducity.base.ui.icons.symbols.HourglassBottom
import com.alorma.caducity.base.ui.icons.symbols.Info
import com.alorma.caducity.base.ui.icons.symbols.Language
import com.alorma.caducity.base.ui.icons.symbols.Notifications
import com.alorma.caducity.base.ui.icons.symbols.Settings
import com.alorma.caducity.base.ui.icons.symbols.Shield
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.ViewWeek
import com.alorma.caducity.config.language.LocalizedDateFormatter
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.notification.hasNotificationPermission
import com.alorma.caducity.feature.tracking.SettingsScreen as SettingsScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.threshold.thresholdLabel
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledLargeTopAppBar
import com.alorma.caducity.ui.screen.settings.components.ColorPaletteSelector
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsButtonGroupCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.ColorPalette
import com.alorma.caducity.ui.theme.OverlineTextStyle
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
  onOpenBackup: () -> Unit,
  onOpenCategoryDurations: () -> Unit,
  onOpenPrivacy: () -> Unit,
  onOpenAbout: () -> Unit,
  onOpenDebug: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SettingsViewModel = koinViewModel(),
  debugModeProvider: DebugModeProvider = koinInject(),
  localizedDateFormatter: LocalizedDateFormatter = koinInject(),
) {
  TrackScreen(screen = SettingsScreenEvent())
  val state by viewModel.state.collectAsStateWithLifecycle()
  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarState()
  val context = LocalContext.current

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        SettingsNavigationSideEffect.OpenBackup -> onOpenBackup()
        SettingsNavigationSideEffect.OpenCategoryDurations -> onOpenCategoryDurations()
        SettingsNavigationSideEffect.OpenPrivacy -> onOpenPrivacy()
        SettingsNavigationSideEffect.OpenAbout -> onOpenAbout()
        SettingsNavigationSideEffect.OpenDebug -> onOpenDebug()
      }
    }
  }
  SideEffectHandler(viewModel, dialogState, snackbarState, localizedDateFormatter)

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) viewModel.onReminderToggle(true) else viewModel.onNotificationsDenied()
    }
  // The user can revoke the permission from the system settings: then the reminder is shown (and works) as off.
  val hasPermission = context.hasNotificationPermission()

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  AppScaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    dialogState = dialogState,
    snackbarState = snackbarState,
    topBar = {
      StyledLargeTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = { NavigationIcon() },
        title = {
          Text(stringResource(R.string.settings_screen_title), modifier = Modifier.semantics { heading() })
        },
      )
    },
  ) { padding ->
    ResponsiveSettingsContainer(modifier = Modifier.padding(padding)) {
      SettingsContent(
        state = state,
        reminderOn = state.reminderEnabled && hasPermission,
        isDebug = debugModeProvider.isDebugMode(),
        localizedDateFormatter = localizedDateFormatter,
        onReminderToggle = { enabled ->
          if (!enabled || hasPermission) {
            viewModel.onReminderToggle(enabled)
          } else {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          }
        },
        onReminderTimeClick = viewModel::onReminderTimeClick,
        onSoonThresholdChange = viewModel::onSoonThresholdChange,
        onThemeModeChange = viewModel::onThemeModeChange,
        onColorPaletteChange = viewModel::onColorPaletteChange,
        onWeekStartClick = viewModel::onWeekStartClick,
        onNavigate = viewModel::navigate,
      )
    }
  }
}

@Composable
internal fun SettingsContent(
  state: SettingsState,
  reminderOn: Boolean,
  isDebug: Boolean,
  localizedDateFormatter: LocalizedDateFormatter,
  onReminderToggle: (Boolean) -> Unit,
  onReminderTimeClick: () -> Unit,
  onSoonThresholdChange: (Int) -> Unit,
  onThemeModeChange: (ThemeMode) -> Unit,
  onColorPaletteChange: (ColorPalette) -> Unit,
  onWeekStartClick: () -> Unit,
  onNavigate: (SettingsNavigation) -> Unit,
  modifier: Modifier = Modifier,
) {
  // Four groups (Avisos, Dates, Aparença, Les teves dades) and «Quant a» as a footer (spec 21).
  LazyColumn(
    modifier = modifier.padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    item {
      val oneDay = stringResource(R.string.settings_1d)
      val threeDays = stringResource(R.string.settings_3d)
      val oneWeek = stringResource(R.string.settings_1w)
      StyledSettingsGroup(title = { GroupTitle(R.string.settings_section_alerts) }) {
        StyledSettingsCard(
          icon = { Icon(Symbols.Outlined.Notifications, contentDescription = null) },
          title = stringResource(R.string.settings_notifications),
          subtitle = stringResource(R.string.settings_notifications_time, formatTime(state.reminderTime)),
          shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
          // The row picks the time; the switch turns the reminder on and off.
          onClick = onReminderTimeClick,
          action = {
            Switch(
              checked = reminderOn,
              onCheckedChange = onReminderToggle,
            )
          },
        )
        StyledSettingsButtonGroupCard(
          icon = { Icon(Symbols.Outlined.HourglassBottom, contentDescription = null) },
          title = stringResource(R.string.settings_soon_default),
          subtitle = stringResource(R.string.settings_soon_default_sub),
          selectedItem = state.soonThresholdDays,
          items = SoonThresholdPreferences.ALLOWED_DAYS,
          itemTitleMap = { days ->
            when (days) {
              1 -> oneDay
              7 -> oneWeek
              else -> threeDays
            }
          },
          onItemSelected = onSoonThresholdChange,
          shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
        )
      }
    }

    item {
      val durationsSummary =
        state.categoryDurations
          .mapNotNull { summary -> summary.days?.let { "${summary.name} ${thresholdLabel(it)}" } }
          .joinToString(" · ")
          .ifBlank { null }
      StyledSettingsGroup(title = { GroupTitle(R.string.settings_section_dates) }) {
        StyledSettingsCard(
          icon = { Icon(Symbols.Outlined.EventAvailable, contentDescription = null) },
          title = stringResource(R.string.settings_category_durations),
          subtitle = durationsSummary,
          subtitleMaxLines = 1,
          action = { Icon(Symbols.Outlined.ChevronRight, contentDescription = null) },
          onClick = { onNavigate(SettingsNavigation.CategoryDurations) },
          shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
        )
        StyledSettingsCard(
          icon = { Icon(Symbols.Outlined.ViewWeek, contentDescription = null) },
          title = stringResource(R.string.settings_week_start),
          subtitle = localizedDateFormatter.getDayOfWeekFullName(state.firstDayOfWeek),
          action = { Icon(Symbols.Outlined.ChevronRight, contentDescription = null) },
          onClick = onWeekStartClick,
          shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
        )
      }
    }

    item {
      val palettes = ColorPalette.entries.filter { it != ColorPalette.DYNAMIC || supportsDynamicColors() }
      val themeLight = stringResource(R.string.settings_theme_light)
      val themeDark = stringResource(R.string.settings_theme_dark)
      val themeSystem = stringResource(R.string.settings_theme_system)
      StyledSettingsGroup(title = { GroupTitle(R.string.settings_section_appearance) }) {
        StyledSettingsButtonGroupCard(
          icon = { Icon(Symbols.Outlined.Contrast, contentDescription = null) },
          title = stringResource(R.string.settings_theme_title),
          selectedItem = state.themeMode,
          items = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM),
          itemTitleMap = { mode ->
            when (mode) {
              ThemeMode.LIGHT -> themeLight
              ThemeMode.DARK -> themeDark
              ThemeMode.SYSTEM -> themeSystem
            }
          },
          onItemSelected = onThemeModeChange,
          shapes = ListItemDefaults.segmentedShapes(index = 0, count = 3),
        )
        ColorPaletteSelector(
          selected = state.colorPalette,
          palettes = palettes,
          onSelected = onColorPaletteChange,
          shapes = ListItemDefaults.segmentedShapes(index = 1, count = 3),
        )
        val context = LocalContext.current
        StyledSettingsCard(
          icon = { Icon(Symbols.Outlined.Language, contentDescription = null) },
          title = stringResource(R.string.settings_app_language),
          subtitle = currentLanguageName(context),
          action = { Icon(Symbols.Outlined.ChevronRight, contentDescription = null) },
          onClick = { context.openLanguageSettings() },
          shapes = ListItemDefaults.segmentedShapes(index = 2, count = 3),
        )
      }
    }

    item {
      val rows = if (isDebug) 3 else 2
      StyledSettingsGroup(title = { GroupTitle(R.string.settings_section_data) }) {
        StyledSettingsCard(
          icon = { Icon(Symbols.Outlined.CloudUpload, contentDescription = null) },
          title = stringResource(R.string.settings_backup_title),
          subtitle = stringResource(R.string.settings_backup_sub),
          action = { Icon(Symbols.Outlined.ChevronRight, contentDescription = null) },
          onClick = { onNavigate(SettingsNavigation.Backup) },
          shapes = ListItemDefaults.segmentedShapes(index = 0, count = rows),
        )
        StyledSettingsCard(
          icon = { Icon(Symbols.Outlined.Shield, contentDescription = null) },
          title = stringResource(R.string.settings_privacy_title),
          action = { Icon(Symbols.Outlined.ChevronRight, contentDescription = null) },
          onClick = { onNavigate(SettingsNavigation.Privacy) },
          shapes = ListItemDefaults.segmentedShapes(index = 1, count = rows),
        )
        if (isDebug) {
          StyledSettingsCard(
            icon = { Icon(Symbols.Outlined.Settings, contentDescription = null) },
            title = stringResource(R.string.settings_debug_title),
            subtitle = stringResource(R.string.settings_debug_description),
            action = { Icon(Symbols.Outlined.ChevronRight, contentDescription = null) },
            onClick = { onNavigate(SettingsNavigation.Debug) },
            shapes = ListItemDefaults.segmentedShapes(index = 2, count = rows),
          )
        }
      }
    }

    item {
      AboutFooter(onClick = { onNavigate(SettingsNavigation.About) })
    }
  }
}

/** Group label: uppercase, 12 sp with 1.2 sp spacing, secondary color. */
@Composable
private fun GroupTitle(
  @StringRes textRes: Int,
) {
  Text(
    text = stringResource(textRes).uppercase(),
    style = OverlineTextStyle,
    color = CaducityTheme.colorScheme.onSurfaceVariant,
  )
}

/** «Quant a Caducity · versió i crèdits»: a quiet footer instead of a row. */
@Composable
private fun AboutFooter(onClick: () -> Unit) {
  Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
    TextButton(
      onClick = onClick,
      colors = ButtonDefaults.textButtonColors(contentColor = CaducityTheme.colorScheme.onSurfaceVariant),
    ) {
      Icon(Symbols.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(Modifier.width(8.dp))
      Text(text = stringResource(R.string.settings_about_footer), style = CaducityTheme.typography.bodyMedium)
    }
  }
}

private fun formatTime(time: LocalTime): String = "%d:%02d".format(time.hour, time.minute)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SideEffectHandler(
  viewModel: SettingsViewModel,
  dialogState: AppDialogState,
  snackbarState: AppSnackbarState,
  localizedDateFormatter: LocalizedDateFormatter,
) {
  val scope = rememberCoroutineScope()
  LaunchedEffect(viewModel) {
    viewModel.sideEffects.collect { effect ->
      when (effect) {
        is SettingsSideEffect.ShowTimePicker ->
          scope.launch {
            val pickerState = TimePickerState(effect.current.hour, effect.current.minute, is24Hour = true)
            val result =
              dialogState.showAlertDialog(
                title = { Text(stringResource(R.string.settings_time_dialog_title)) },
                content = { TimePicker(state = pickerState) },
                positiveButton = { Text(stringResource(R.string.settings_dialog_save)) },
                negativeButton = { Text(stringResource(R.string.settings_dialog_cancel)) },
                type = AppFeedbackType.Info,
              )
            if (result == DialogResult.Positive) {
              viewModel.onReminderTimeChange(LocalTime(pickerState.hour, pickerState.minute))
            }
          }
        is SettingsSideEffect.ShowWeekStartPicker ->
          scope.launch {
            var selected by mutableStateOf(effect.current)
            val result =
              dialogState.showAlertDialog(
                title = { Text(stringResource(R.string.settings_week_start)) },
                content = {
                  Column(Modifier.selectableGroup()) {
                    WEEK_START_OPTIONS.forEach { day ->
                      Row(
                        modifier =
                          Modifier
                            .selectable(
                              selected = selected == day,
                              role = Role.RadioButton,
                              onClick = { selected = day },
                            ).padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        RadioButton(selected = selected == day, onClick = null)
                        Text(
                          text = localizedDateFormatter.getDayOfWeekFullName(day),
                          modifier = Modifier.padding(start = 12.dp),
                        )
                      }
                    }
                  }
                },
                positiveButton = { Text(stringResource(R.string.settings_dialog_save)) },
                negativeButton = { Text(stringResource(R.string.settings_dialog_cancel)) },
                type = AppFeedbackType.Info,
              )
            if (result == DialogResult.Positive) viewModel.onFirstDayOfWeekChange(selected)
          }
        SettingsSideEffect.NotificationsDenied ->
          scope.launch {
            snackbarState.showSnackbar(
              message = R.string.settings_notifications_denied,
              type = AppFeedbackType.Error,
            )
          }
      }
    }
  }
}

private val WEEK_START_OPTIONS = listOf(DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

private val SupportedLanguages = setOf("ca", "es", "en")

/** Language the app is showing; a system language the app does not have falls back to English. */
private fun currentLanguageName(context: Context): String {
  val configured = context.resources.configuration.locales[0]
  val locale = if (configured.language in SupportedLanguages) configured else Locale.ENGLISH
  return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
}

private fun Context.openLanguageSettings() {
  // Per-app language settings only exist from Android 13; before that the app follows the system language.
  val intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      Intent(Settings.ACTION_APP_LOCALE_SETTINGS).setData(Uri.fromParts("package", packageName, null))
    } else {
      Intent(Settings.ACTION_LOCALE_SETTINGS)
    }
  startActivity(intent)
}

@PreviewLightDark
@Composable
private fun SettingsPreview() {
  PreviewTheme { SettingsPreviewContent() }
}

/** Ajustos amb dades d'exemple, sense tema (el posa qui el crida). */
@Composable
internal fun SettingsPreviewContent(selectedPalette: ColorPalette = ColorPalette.REBOST) {
  SettingsContent(
    state =
      SettingsState(
        reminderEnabled = true,
        colorPalette = selectedPalette,
        categoryDurations =
          listOf(
            CategoryDurationSummary("Fruita i verdura", 7),
            CategoryDurationSummary("Làctics", 3),
          ),
      ),
    reminderOn = true,
    isDebug = false,
    localizedDateFormatter = LocalizedDateFormatter(),
    onReminderToggle = {},
    onReminderTimeClick = {},
    onSoonThresholdChange = {},
    onThemeModeChange = {},
    onColorPaletteChange = {},
    onWeekStartClick = {},
    onNavigate = {},
  )
}
