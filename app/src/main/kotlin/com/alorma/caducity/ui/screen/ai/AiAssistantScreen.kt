package com.alorma.caducity.ui.screen.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.icons.outlined.Send
import com.alorma.caducity.base.ui.icons.outlined.Sparkle
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AiAssistantScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AiAssistantViewModel = koinViewModel(),
) {
  var inputText by rememberSaveable { mutableStateOf("") }

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        AiAssistantNavigationSideEffect.NavigateBack -> onNavigateBack()
      }
    }
  }

  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledTopAppBar(
        title = { Text(text = stringResource(R.string.ai_assistant_title)) },
        navigationIcon = {
          IconButton(onClick = { viewModel.navigate(AiAssistantNavigation.Cancel) }) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        },
        actions = {
          Icon(
            imageVector = AppIcons.Outlined.Sparkle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp),
          )
        },
      )
    },
  ) { paddingValues ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
      contentAlignment = Alignment.BottomCenter,
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = inputText,
          onValueChange = { inputText = it },
          placeholder = { Text(text = stringResource(R.string.ai_assistant_input_placeholder)) },
          modifier = Modifier.weight(1f),
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
          singleLine = true,
          shape = MaterialTheme.shapes.extraLarge,
        )
        IconButton(
          onClick = { /* TODO: send */ },
          enabled = inputText.isNotBlank(),
        ) {
          Icon(
            imageVector = AppIcons.Outlined.Send,
            contentDescription = stringResource(R.string.ai_assistant_send),
            tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
