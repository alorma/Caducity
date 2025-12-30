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
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.language_catalan
import caducity.composeapp.generated.resources.language_english
import caducity.composeapp.generated.resources.language_spanish
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.theme.LanguageManager
import com.alorma.caducity.base.ui.theme.language.Language
import com.alorma.caducity.ui.screen.settings.components.CardPosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun LanguageSettingsScreen(
  modifier: Modifier = Modifier,
) {
  val languageManager = koinInject<LanguageManager>()

  val languageEnglish = stringResource(Res.string.language_english)
  val languageSpanish = stringResource(Res.string.language_spanish)
  val languageCatalan = stringResource(Res.string.language_catalan)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    StyledSettingsGroup {
      Language.entries.forEachIndexed { index, language ->
        val title = when (language) {
          Language.ENGLISH -> languageEnglish
          Language.SPANISH -> languageSpanish
          Language.CATALAN -> languageCatalan
        }

        val position = when {
          Language.entries.size == 1 -> CardPosition.Single
          index == 0 -> CardPosition.Top
          index == Language.entries.lastIndex -> CardPosition.Bottom
          else -> CardPosition.Middle
        }

        val isSelected = languageManager.selectedLanguage.value == language

        StyledSettingsCard(
          title = title,
          subtitle = "",
          position = position,
          action = if (isSelected) {
            {
              Icon(
                imageVector = AppIcons.Check,
                contentDescription = null,
              )
            }
          } else {
            null
          },
          onClick = { languageManager.setLanguage(language) },
        )
      }
    }
  }
}
