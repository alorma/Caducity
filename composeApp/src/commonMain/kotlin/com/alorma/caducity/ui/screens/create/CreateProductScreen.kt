package com.alorma.caducity.ui.screens.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CreateProductScreen(
    onNavigateBack: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create Product",
            style = MaterialTheme.typography.headlineMedium
        )
        
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = expirationDate,
            onValueChange = { expirationDate = it },
            label = { Text("Expiration Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    // TODO: Save product logic
                    onNavigateBack()
                },
                modifier = Modifier.weight(1f),
                enabled = productName.isNotBlank() && expirationDate.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
