package com.alorma.caducity.ui.screen.category.create

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Back
import com.alorma.caducity.ui.components.responsive.ResponsiveCenteredContainer
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import org.koin.compose.viewmodel.koinViewModel
import com.alorma.caducity.feature.tracking.CreateCategoryScreen as CreateCategoryScreenEvent
import com.alorma.caducity.feature.tracking.TrackScreen

@Composable
fun CreateCategoryScreen(
  onNavigateBack: () -> Unit,
  onNavigateToCategory: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: CreateCategoryViewModel = koinViewModel(),
) {
  TrackScreen(screen = CreateCategoryScreenEvent())
  val state = viewModel.state.collectAsStateWithLifecycle()

  // Handle navigation side effects
  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        CreateCategoryNavigationSideEffect.NavigateBack -> onNavigateBack()
        is CreateCategoryNavigationSideEffect.NavigateToCategoryDetail ->
          onNavigateToCategory(effect.categoryId)
      }
    }
  }

  CreateCategoryPage(
    state = state.value,
    onNameChange = viewModel::updateName,
    onDescriptionChange = viewModel::updateDescription,
    onCreateClick = viewModel::createCategory,
    onCancelClick = { viewModel.navigate(CreateCategoryNavigation.Cancel) },
    onErrorDismiss = viewModel::clearError,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCategoryPage(
  state: CreateCategoryState,
  onNameChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onCreateClick: () -> Unit,
  onCancelClick: () -> Unit,
  onErrorDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      StyledTopAppBar(
        title = {
          Text(text = stringResource(R.string.create_category_screen_title))
        },
        navigationIcon = {
          IconButton(onClick = onCancelClick) {
            Icon(
              imageVector = AppIcons.Back,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        },
      )
    },
    bottomBar = {
      BottomAppBar {
        ResponsiveCenteredContainer(maxWidth = 600.dp) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            TextButton(
              onClick = onCancelClick,
              enabled = !state.isLoading,
              modifier = Modifier.weight(1f),
            ) {
              Text(stringResource(R.string.create_category_button_cancel))
            }
            Button(
              onClick = onCreateClick,
              enabled = !state.isLoading,
              modifier = Modifier.weight(1f),
            ) {
              if (state.isLoading) {
                CircularProgressIndicator()
              } else {
                Text(stringResource(R.string.create_category_button_create))
              }
            }
          }
        }
      }
    }
  ) { paddingValues ->
    ResponsiveCenteredContainer(
      maxWidth = 600.dp,
      modifier = Modifier.padding(paddingValues),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Product Name Field
        TextField(
          value = state.name,
          onValueChange = onNameChange,
          label = { Text(stringResource(R.string.create_category_name_label)) },
          placeholder = { Text(stringResource(R.string.create_category_name_placeholder)) },
          modifier = Modifier.fillMaxWidth(),
          enabled = !state.isLoading,
          isError = state.error != null,
        )

        // Product Description Field
        TextField(
          value = state.description,
          onValueChange = onDescriptionChange,
          label = { Text(stringResource(R.string.create_category_description_label)) },
          placeholder = { Text(stringResource(R.string.create_category_description_placeholder)) },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          enabled = !state.isLoading,
        )

        // Error Message
        if (state.error != null) {
          Text(
            text = state.error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
          LaunchedEffect(state.error, onErrorDismiss) {
            kotlinx.coroutines.delay(3000)
            onErrorDismiss()
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
