package com.alorma.caducity.ui.screen.product.detail

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.Add
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Cooking
import com.alorma.caducity.base.ui.icons.Delete
import com.alorma.caducity.base.ui.icons.ThermometerSnow
import com.alorma.caducity.ui.components.StatusBadge
import com.alorma.caducity.ui.components.StatusBadgeSize
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.calculateShape
import com.alorma.caducity.ui.components.shape.toVerticalShape
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar
import com.alorma.caducity.ui.theme.CaducityTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDetailScreen(
  productId: String,
  onNavigateToAddInstance: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: ProductDetailViewModel = koinViewModel { parametersOf(productId) }
) {
  val state = viewModel.state.collectAsStateWithLifecycle()

  val dialogState = rememberAppDialogState()
  val snackbarState = rememberAppSnackbarHostState()

  SideEffectHandler(viewModel, snackbarState, dialogState)

  when (val currentState = state.value) {
    is ProductDetailState.Loading -> FullscreenLoading()

    is ProductDetailState.Success -> {
      AppScaffold(
        modifier = modifier,
        dialogState = dialogState,
        snackbarState = snackbarState,
        topBar = {
          StyledTopAppBar(
            title = { Text(text = currentState.product.name) },
            navigationIcon = { NavigationIcon() },
          )
        },
        floatingActionButton = {
          FloatingActionButton(
            onClick = onNavigateToAddInstance,
          ) {
            Icon(
              imageVector = AppIcons.Add,
              contentDescription = null,
            )
          }
        },
      ) { paddingValues ->
        LazyColumn(
          modifier = Modifier.padding(paddingValues),
        ) {

          items(
            items = currentState.content,
            key = { datedContent -> "product-${currentState.product.id}-dated-${datedContent.date}" },
            contentType = { "datedContent" },
          ) { datedContent ->
            DatedContent(
              state = datedContent,
              onConsume = viewModel::onConsumeInstance,
              onFreeze = viewModel::onFreezeInstance,
              onDelete = viewModel::onDeleteInstance,
            )
          }
        }
      }
    }

    is ProductDetailState.Error -> DetailError(currentState)
  }
}

@Composable
private fun DatedContent(
  state: DateInstancesUiModel,
  onConsume: (ProductInstanceDetailUiModel) -> Unit,
  onFreeze: (ProductInstanceDetailUiModel) -> Unit,
  onDelete: (ProductInstanceDetailUiModel) -> Unit,
) {
  Box(
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    // Content column
    Row {
      // Spacer for timeline
      Box(modifier = Modifier.width(24.dp))

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(bottom = 16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          StatusBadge(
            status = state.status,
            size = StatusBadgeSize.Large,
          )

          Text(
            text = "·",
            style = CaducityTheme.typography.labelMedium,
          )

          Text(
            text = state.text,
            style = CaducityTheme.typography.labelMedium,
          )
        }

        Column(
          modifier = Modifier.padding(top = 8.dp),
        ) {
          state.instances.forEachIndexed { index, instance ->
            SmallProductInstanceCard(
              modifier = Modifier.fillMaxWidth(),
              instance = instance,
              shapePosition = state.instances.calculateShape(index),
              onConsume = onConsume,
              onFreeze = onFreeze,
              onDelete = onDelete,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DetailError(currentState: ProductDetailState.Error) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = currentState.message,
      style = MaterialTheme.typography.bodyLarge,
      color = CaducityTheme.colorScheme.error,
    )
  }
}

@Composable
private fun SideEffectHandler(
  viewModel: ProductDetailViewModel,
  snackbarState: AppSnackbarHostState,
  dialogState: AppDialogState
) {
  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { effect ->
      Log.i("Alorma", effect.toString())

      when (effect) {
        is ProductDetailSideEffect.ShowMessage -> {
          snackbarState.showSnackbar(
            message = effect.message,
            type = AppFeedbackType.Success,
          )
        }

        is ProductDetailSideEffect.ShowError -> {
          snackbarState.showSnackbar(
            message = effect.message,
            type = AppFeedbackType.Error,
          )
        }

        is ProductDetailSideEffect.FreezeNotAvailable -> {
          snackbarState.showSnackbar(
            message = R.string.error_cannot_freeze_expired,
            type = AppFeedbackType.Status(effect.status),
          )
        }

        is ProductDetailSideEffect.ShowConsumeExpiredWarning -> {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_title
            ),
            text = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.instance.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onConsumeInstanceConfirmed(effect.instance)
          }
        }

        is ProductDetailSideEffect.ShowConsumeExpiredError -> {
          val result = dialogState.showAlertDialog(
            title = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired
            ),
            text = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired_message
            ),
            type = AppFeedbackType.Status(effect.status),
            positiveButton = AppFeedbackResource.AsResource(
              R.string.error_cannot_consume_expired_positive
            ),
            negativeButton = AppFeedbackResource.AsResource(
              R.string.warning_consume_expired_negative
            ),
          )
          if (result == DialogResult.Positive) {
            viewModel.onDeleteInstance(effect.instance)
          }
        }
      }
    }
  }
}

@Composable
private fun SmallProductInstanceCard(
  instance: ProductInstanceDetailUiModel,
  shapePosition: ShapePosition,
  onConsume: (ProductInstanceDetailUiModel) -> Unit,
  onFreeze: (ProductInstanceDetailUiModel) -> Unit,
  onDelete: (ProductInstanceDetailUiModel) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }

  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Surface(
      modifier = Modifier.weight(1f),
      shape = shapePosition.toVerticalShape(),
      color = CaducityTheme.colorScheme.surfaceContainer,
    ) {
      Text(
        modifier = Modifier.padding(12.dp),
        text = instance.text,
        style = MaterialTheme.typography.labelLarge,
        color = CaducityTheme.colorScheme.onSurface,
      )
    }
    Box {
      IconButton(onClick = { showMenu = true }) {
        Text(
          text = "⋮",
          style = MaterialTheme.typography.titleLarge,
          color = CaducityTheme.colorScheme.onSurfaceVariant,
        )
      }
      DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
      ) {
        DropdownMenuItem(
          text = { Text("Consume") },
          leadingIcon = {
            Icon(
              imageVector = AppIcons.Cooking,
              contentDescription = null,
            )
          },
          onClick = {
            onConsume(instance)
            showMenu = false
          }
        )
        DropdownMenuItem(
          text = { Text("Freeze") },
          leadingIcon = {
            Icon(
              imageVector = AppIcons.ThermometerSnow,
              contentDescription = null,
            )
          },
          onClick = {
            onFreeze(instance)
            showMenu = false
          }
        )
        DropdownMenuItem(
          text = { Text("Delete") },
          leadingIcon = {
            Icon(
              imageVector = AppIcons.Delete,
              contentDescription = null,
            )
          },
          onClick = {
            onDelete(instance)
            showMenu = false
          }
        )
      }
    }
  }
}
