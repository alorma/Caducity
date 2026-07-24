package com.alorma.caducity.ui.screen.settings.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.feature.ai.AiModelOption
import com.alorma.caducity.feature.ai.AiModelPreferences
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.ui.components.responsive.ResponsiveSettingsContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import org.koin.compose.koinInject

@Composable
fun AiModelSettingsScreen(
  modifier: Modifier = Modifier,
  aiModelPreferences: AiModelPreferences = koinInject(),
  modelManager: ModelManager = koinInject(),
) {
  val selectedModel by aiModelPreferences.selectedModel.collectAsState()
  val downloadState by modelManager.downloadState().collectAsState(initial = ModelDownloadState.Idle)

  AiModelSettingsContent(
    selectedModel = selectedModel,
    downloadState = downloadState,
    onModelSelected = { model ->
      modelManager.switchModel(model)
      if (!modelManager.isModelReady()) {
        modelManager.startDownload()
      }
    },
    modifier = modifier,
  )
}

@Composable
private fun AiModelSettingsContent(
  selectedModel: AiModelOption,
  downloadState: ModelDownloadState,
  onModelSelected: (AiModelOption) -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = { Text(text = stringResource(R.string.settings_ai_model_title)) },
      )
    },
  ) { paddingValues ->
    ResponsiveSettingsContainer(modifier = Modifier.padding(paddingValues)) {
      LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
      ) {
        item {
          StyledSettingsGroup(
            title = { Text(stringResource(R.string.settings_ai_model_select_title)) },
          ) {
            val models = AiModelOption.entries
            models.forEachIndexed { index, model ->
              val isSelected = model == selectedModel
              StyledSettingsCard(
                title = modelLabel(model),
                subtitle = modelDescription(model, isSelected, downloadState),
                onClick = { onModelSelected(model) },
                shapes = ListItemDefaults.segmentedShapes(index = index, count = models.size),
              )
            }
          }
        }

        if (downloadState is ModelDownloadState.Downloading) {
          item {
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = stringResource(R.string.settings_ai_model_downloading, downloadState.progress),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              LinearProgressIndicator(
                progress = { downloadState.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }

        item {
          Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = stringResource(R.string.settings_ai_model_info_title),
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = stringResource(R.string.settings_ai_model_info_description),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun modelLabel(model: AiModelOption): String =
  when (model) {
    AiModelOption.GEMMA_3_270M -> stringResource(R.string.settings_ai_model_light)
    AiModelOption.GEMMA_3_1B -> stringResource(R.string.settings_ai_model_standard)
  }

@Composable
private fun modelDescription(
  model: AiModelOption,
  isSelected: Boolean,
  downloadState: ModelDownloadState,
): String {
  val size = stringResource(R.string.settings_ai_model_size, model.sizeMb)
  val status =
    if (isSelected) {
      when (downloadState) {
        ModelDownloadState.Ready -> stringResource(R.string.settings_ai_model_status_ready)
        ModelDownloadState.Idle -> stringResource(R.string.settings_ai_model_status_not_downloaded)
        is ModelDownloadState.Downloading -> stringResource(R.string.settings_ai_model_status_downloading)
        ModelDownloadState.Failed -> stringResource(R.string.settings_ai_model_status_failed)
      }
    } else {
      stringResource(R.string.settings_ai_model_status_tap_to_select)
    }
  return "$size • $status"
}
