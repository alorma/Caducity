package com.alorma.caducity.ui.screen.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.base.ui.icons.outlined.Send
import com.alorma.caducity.base.ui.icons.outlined.Sparkle
import com.alorma.caducity.feature.ai.GroceryProposal
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.loading.WavyLoadingIndicator
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
  val modelState by viewModel.modelState.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val listState = rememberLazyListState()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        AiAssistantNavigationSideEffect.NavigateBack -> onNavigateBack()
      }
    }
  }

  // Scroll to bottom whenever messages change
  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
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
    ) {
      when (val state = modelState) {
        ModelDownloadState.Ready -> {
          LazyColumn(
            state = listState,
            modifier =
              Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            items(messages) { message ->
              when (message) {
                is ChatMessage.Outgoing -> OutgoingBubble(message.text)
                is ChatMessage.Proposals -> IncomingProposalsBubble(message.proposals)
                ChatMessage.Thinking -> ThinkingBubble()
                ChatMessage.Error -> IncomingErrorBubble()
              }
            }
          }
        }

        ModelDownloadState.Idle -> {
          Text(
            text = stringResource(R.string.ai_model_preparing),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        is ModelDownloadState.Downloading -> {
          FullscreenLoading(progress = state.progress / 100f)
        }

        ModelDownloadState.Failed -> {
          Text(
            text = stringResource(R.string.ai_model_download_failed),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }

      val isReady = modelState == ModelDownloadState.Ready
      val hasThinking = messages.any { it is ChatMessage.Thinking }

      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
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
          keyboardActions =
            KeyboardActions(
              onSend = {
                if (isReady && !hasThinking && inputText.isNotBlank()) {
                  viewModel.send(inputText)
                  inputText = ""
                }
              },
            ),
          singleLine = true,
          enabled = isReady && !hasThinking,
          shape = MaterialTheme.shapes.extraLarge,
        )
        IconButton(
          onClick = {
            viewModel.send(inputText)
            inputText = ""
          },
          enabled = isReady && !hasThinking && inputText.isNotBlank(),
        ) {
          Icon(
            imageVector = AppIcons.Outlined.Send,
            contentDescription = stringResource(R.string.ai_assistant_send),
            tint =
              if (isReady && !hasThinking && inputText.isNotBlank()) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
          )
        }
      }
    }
  }
}

@Composable
private fun OutgoingBubble(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End,
  ) {
    Text(
      text = text,
      modifier =
        Modifier
          .widthIn(max = 280.dp)
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.primary)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      color = MaterialTheme.colorScheme.onPrimary,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Composable
private fun IncomingProposalsBubble(proposals: List<GroceryProposal>) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
  ) {
    Column(
      modifier =
        Modifier
          .widthIn(max = 280.dp)
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      proposals.forEach { proposal ->
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "${proposal.quantity}× ${proposal.productName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = stringResource(R.string.ai_proposal_expires, proposal.expirationDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
private fun ThinkingBubble() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
  ) {
    Box(
      modifier =
        Modifier
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.surfaceContainerHigh)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      WavyLoadingIndicator(modifier = Modifier.size(24.dp))
    }
  }
}

@Composable
private fun IncomingErrorBubble() {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Start,
  ) {
    Text(
      text = stringResource(R.string.ai_parse_error),
      modifier =
        Modifier
          .widthIn(max = 280.dp)
          .clip(MaterialTheme.shapes.large)
          .background(MaterialTheme.colorScheme.errorContainer)
          .padding(horizontal = 16.dp, vertical = 10.dp),
      color = MaterialTheme.colorScheme.onErrorContainer,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}
