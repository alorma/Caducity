package com.alorma.caducity.ui.screen.add

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.unit.formatAmount

/** Shortcuts of the usual amounts: 250 g · 500 g · 1 kg, or 250 mL · 500 mL · 1 L. */
fun UnitType.amountShortcuts(): List<Int> = listOf(250, 500, 1000)

@Composable
fun AmountChips(
  unitType: UnitType,
  amount: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val locale = LocalConfiguration.current.locales[0]
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()).padding(PaddingValues(vertical = 2.dp)),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    unitType.amountShortcuts().forEach { shortcut ->
      FilterChip(
        selected = amount == shortcut,
        onClick = { onSelect(shortcut) },
        label = { Text(formatAmount(unitType, shortcut, locale), style = CaducityTheme.typography.labelLarge) },
      )
    }
  }
}
