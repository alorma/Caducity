package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.ui.adaptive.isWidthCompact
import com.alorma.caducity.ui.icons.Add
import com.alorma.caducity.ui.icons.AppIcons
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel = koinViewModel()
) {
  val showDialog = remember { mutableStateOf(false) }

  val dashboardState by viewModel.state.collectAsStateWithLifecycle(
    initialValue = DashboardState.Loading,
  )

  val isCompact = isWidthCompact()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showDialog.value = true },
      ) {
        Icon(
          imageVector = AppIcons.Add,
          contentDescription = "Create Product",
        )
      }
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Dashboard",
        style = MaterialTheme.typography.headlineMedium,
      )

      when (val state = dashboardState) {
        is DashboardState.Loading -> {
          Text(text = "Loading...")
        }

        is DashboardState.Success -> {
          state.sections.forEach { section ->
            DashboardCard(
              title = stringResource(section.title),
              value = section.itemCount.toString(),
            )
          }
        }
      }
    }
  }

  if (showDialog.value) {
    if (isCompact) {
      ModalBottomSheet(
        sheetState = rememberModalBottomSheetState { true },
        onDismissRequest = { showDialog.value = false },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      ) {
        CreateProductDialogContent(
          modifier = Modifier.fillMaxSize(),
          onDismiss = { showDialog.value = false },
        )
      }
    } else {
      BasicAlertDialog(
        onDismissRequest = { showDialog.value = false },
      ) {
        Surface(
          color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
          CreateProductDialogContent(
            onDismiss = { showDialog.value = false },
          )
        }
      }
    }
  }
}

@Composable
private fun DashboardCard(
  title: String,
  value: String,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.displaySmall,
      )
    }
  }
}

@Composable
private fun CreateProductDialogContent(
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(24.dp)
      .then(modifier),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Create Product",
      style = MaterialTheme.typography.headlineMedium,
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Product Name Field
    TextField(
      value = "",
      onValueChange = {},
      label = { Text("Product Name") },
      placeholder = { Text("Enter product name") },
      modifier = Modifier.fillMaxWidth(),
    )

    // Product Description Field
    TextField(
      value = "",
      onValueChange = {},
      label = { Text("Description") },
      placeholder = { Text("Enter description") },
      modifier = Modifier.fillMaxWidth(),
      minLines = 3,
    )

    // Expiration Date Field
    TextField(
      value = "",
      onValueChange = {},
      label = { Text("Expiration Date") },
      placeholder = { Text("DD/MM/YYYY") },
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Action Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.End,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
      Spacer(modifier = Modifier.padding(8.dp))
      TextButton(onClick = onDismiss) {
        Text("Create")
      }
    }
  }
}