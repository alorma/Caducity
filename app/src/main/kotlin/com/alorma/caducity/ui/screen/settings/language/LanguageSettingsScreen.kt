package com.alorma.caducity.ui.screen.settings.language

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.config.language.LanguageManager
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.base.ui.icons.Check
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import org.koin.compose.koinInject

@Composable
fun LanguageSettingsScreen(
  modifier: Modifier = Modifier,
  languageManager: LanguageManager = koinInject(),
) {

  val selectedLocale = languageManager.getLocale()
  val availableLocales = languageManager.loadSupportedLocales()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    StyledSettingsGroup {
      availableLocales.forEachIndexed { index, locale ->
        val position = when {
          availableLocales.size == 1 -> ShapePosition.Single
          index == 0 -> ShapePosition.Start
          index == availableLocales.lastIndex -> ShapePosition.End
          else -> ShapePosition.Middle
        }

        StyledSettingsCard(
          title = locale.displayName,
          position = position,
          action = if (selectedLocale.language == locale.language) {
            {
              Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
              )
            }
          } else {
            null
          },
          onClick = { languageManager.applyLocale(locale) },
        )
      }
    }
  }
}
