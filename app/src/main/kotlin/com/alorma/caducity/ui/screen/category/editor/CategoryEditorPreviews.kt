package com.alorma.caducity.ui.screen.category.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.caducity.ui.components.topbar.NavigationIcon
import com.alorma.caducity.ui.components.topbar.StyledTopAppBar

/** «Nova categoria» buida (sense tema, per a les captures de test). */
@Composable
internal fun CategoryEditorPreviewContent() {
  Column(modifier = Modifier.fillMaxSize()) {
    StyledTopAppBar(
      navigationIcon = { NavigationIcon() },
      title = { Text("Nova categoria") },
    )
    CategoryEditorContent(
      state = CategoryEditorState(loading = false),
      onNameChange = {},
      onDescriptionChange = {},
      onShapeChange = {},
      onIconChange = {},
      onUsualDurationChange = {},
      onDelete = {},
    )
  }
}
