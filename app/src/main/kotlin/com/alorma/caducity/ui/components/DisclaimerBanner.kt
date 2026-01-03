package com.alorma.caducity.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Info
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun DisclaimerBanner(
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = CaducityTheme.colorScheme.errorContainer,
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = AppIcons.Info,
        contentDescription = null,
        tint = CaducityTheme.colorScheme.onErrorContainer,
      )
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = stringResource(R.string.disclaimer_brief_title),
          style = CaducityTheme.typography.titleSmall,
          color = CaducityTheme.colorScheme.onErrorContainer,
        )
        Text(
          text = stringResource(R.string.disclaimer_brief_message),
          style = CaducityTheme.typography.bodySmall,
          color = CaducityTheme.colorScheme.onErrorContainer,
        )
      }
    }
  }
}
