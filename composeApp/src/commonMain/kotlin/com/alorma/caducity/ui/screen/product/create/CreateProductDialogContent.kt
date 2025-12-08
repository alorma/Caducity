package com.alorma.caducity.ui.screen.product.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateProductDialogContent(
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
      TextButton(onClick = {}) {
        Text("Cancel")
      }
      Spacer(modifier = Modifier.padding(8.dp))
      TextButton(onClick = { }) {
        Text("Create")
      }
    }
  }
}
