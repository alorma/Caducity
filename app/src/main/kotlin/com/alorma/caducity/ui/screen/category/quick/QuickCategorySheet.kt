package com.alorma.caducity.ui.screen.category.quick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.ui.components.category.CategoryForm
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel

/** «Nova categoria ràpida»: content of the bottom sheet opened from the Add screen. */
@Composable
fun QuickCategorySheet(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: QuickCategoryViewModel = koinViewModel(),
) {
  val state by viewModel.state.collectAsState()

  LaunchedEffect(viewModel) {
    viewModel.navigationSideEffects.collect { effect ->
      when (effect) {
        QuickCategoryNavigationSideEffect.Close -> onClose()
      }
    }
  }

  Column(
    modifier =
      modifier
        .imePadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
        .padding(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Text(stringResource(R.string.quick_category_title), style = CaducityTheme.typography.titleLarge)
    CategoryForm(
      name = state.name,
      onNameChange = viewModel::onNameChange,
      shape = state.shape,
      onShapeChange = viewModel::onShapeChange,
      icon = state.icon,
      onIconChange = viewModel::onIconChange,
      error =
        when (state.error) {
          QuickCategoryError.Duplicate -> stringResource(R.string.quick_category_error_duplicate)
          QuickCategoryError.Failed -> stringResource(R.string.quick_category_error_failed)
          null -> null
        },
    )
    Text(
      text = stringResource(R.string.quick_category_note),
      style = CaducityTheme.typography.bodySmall,
      color = CaducityTheme.colorScheme.onSurfaceVariant,
    )
    Button(
      onClick = viewModel::onCreate,
      enabled = state.canCreate,
      modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
      Text(stringResource(R.string.quick_category_create))
    }
  }
}
