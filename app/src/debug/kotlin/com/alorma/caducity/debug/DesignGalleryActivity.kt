package com.alorma.caducity.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.symbols.AcUnit
import com.alorma.caducity.base.ui.icons.symbols.BakeryDining
import com.alorma.caducity.base.ui.icons.symbols.CalendarMonth
import com.alorma.caducity.base.ui.icons.symbols.Check
import com.alorma.caducity.base.ui.icons.symbols.Delete
import com.alorma.caducity.base.ui.icons.symbols.EventRepeat
import com.alorma.caducity.base.ui.icons.symbols.Grocery
import com.alorma.caducity.base.ui.icons.symbols.HourglassBottom
import com.alorma.caducity.base.ui.icons.symbols.Kitchen
import com.alorma.caducity.base.ui.icons.symbols.LocalDrink
import com.alorma.caducity.base.ui.icons.symbols.Nutrition
import com.alorma.caducity.base.ui.icons.symbols.SetMeal
import com.alorma.caducity.base.ui.icons.symbols.Settings
import com.alorma.caducity.base.ui.icons.symbols.Symbols
import com.alorma.caducity.base.ui.icons.symbols.WaterFull
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.components.category.CategoryShapeIcon
import com.alorma.caducity.ui.components.chips.FilterChipOption
import com.alorma.caducity.ui.components.chips.FilterChips
import com.alorma.caducity.ui.components.chips.QuickDateChips
import com.alorma.caducity.ui.components.chips.QuickDateOption
import com.alorma.caducity.ui.components.connected.ConnectedListSpacing
import com.alorma.caducity.ui.components.connected.ConnectedRow
import com.alorma.caducity.ui.components.status.StatusPill
import com.alorma.caducity.ui.components.switch.CaducitySwitch
import com.alorma.caducity.ui.theme.AppTheme
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.NumberTextStyle
import com.alorma.caducity.ui.theme.OverlineTextStyle
import com.alorma.caducity.ui.theme.ThemeMode
import com.alorma.caducity.ui.theme.ThemePreferences
import org.koin.android.ext.android.inject

/** Galeria del sistema de disseny (només debug): `adb shell am start -n <pkg>/com.alorma.caducity.debug.DesignGalleryActivity`. */
class DesignGalleryActivity : ComponentActivity() {
  private val themePreferences: ThemePreferences by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      AppTheme(themePreferences = themePreferences) {
        Surface(modifier = Modifier.fillMaxSize()) {
          Gallery(
            onToggleDark = {
              themePreferences.themeMode.value =
                if (themePreferences.themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
            },
          )
        }
      }
    }
  }
}

@Composable
private fun Gallery(onToggleDark: () -> Unit) {
  val type = CaducityTheme.typography
  Column(
    modifier =
      Modifier
        .safeDrawingPadding()
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
      Button(onClick = onToggleDark, shape = ButtonGroupDefaults.connectedLeadingButtonShape) { Text("Clar") }
      Button(onClick = onToggleDark, shape = ButtonGroupDefaults.connectedTrailingButtonShape) { Text("Fosc") }
    }
    Section("Tipografia")
    Text("Què caduca", style = type.displaySmall)
    Text("Rebost", style = type.headlineLarge)
    Text("Afegeix producte", style = type.headlineSmall)
    Text("Avui", style = type.titleLarge)
    Text("Làctics", style = type.titleMedium)
    Text("Llet sencera", style = type.titleSmall)
    Text("Text destacat", style = type.bodyLarge)
    Text("Text general de cos", style = type.bodyMedium)
    Text("Metadades 13", style = type.bodySmall)
    Text("Botó", style = type.labelLarge)
    Text("ETIQUETA", style = OverlineTextStyle)
    Text("12", style = NumberTextStyle)

    Section("Estats")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      StatusPill(ExpiryStatus.EXPIRED, "Caducat")
      StatusPill(ExpiryStatus.SOON, "Aviat")
      StatusPill(ExpiryStatus.FRESH, "Fresc")
      StatusPill(ExpiryStatus.FROZEN, "Congelat")
    }

    Section("Formes de categoria")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
      val icons =
        listOf(
          Symbols.Outlined.WaterFull,
          Symbols.Outlined.Nutrition,
          Symbols.Outlined.SetMeal,
          Symbols.Outlined.Grocery,
          Symbols.Outlined.BakeryDining,
          Symbols.Outlined.LocalDrink,
        )
      CategoryShape.entries.forEachIndexed { i, shape ->
        CategoryShapeIcon(shape = shape, icon = icons[i], size = 48.dp, selected = i == 2)
      }
    }

    Section("Llista connectada")
    Column(verticalArrangement = Arrangement.spacedBy(ConnectedListSpacing)) {
      repeat(3) { i ->
        ConnectedRow(index = i, count = 3, onClick = {}) {
          Text("Fila ${i + 1}", style = type.titleSmall)
        }
      }
    }

    Section("Grup de botons")
    var group by remember { mutableIntStateOf(0) }
    val groupOptions = listOf("Avui", "Demà", "Setmana")
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
      groupOptions.forEachIndexed { index, label ->
        ToggleButton(
          checked = group == index,
          onCheckedChange = { group = index },
          modifier = Modifier.weight(1f),
          shapes =
            when (index) {
              0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
              groupOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
              else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            },
        ) { Text(label) }
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
      Button(onClick = {}, modifier = Modifier.weight(1f), shape = ButtonGroupDefaults.connectedLeadingButtonShape) {
        Icon(Symbols.Outlined.Check, contentDescription = null)
        Text("Consumit")
      }
      Button(
        onClick = {},
        modifier = Modifier.weight(1f),
        shape = ButtonGroupDefaults.connectedTrailingButtonShape,
        colors =
          ButtonDefaults.buttonColors(
            containerColor = CaducityTheme.colors.frozen.container,
            contentColor = CaducityTheme.colors.frozen.onContainer,
          ),
      ) {
        Icon(Symbols.Outlined.AcUnit, contentDescription = null)
        Text("Congela")
      }
    }

    Section("Xips")
    var chip by remember { mutableIntStateOf(0) }
    FilterChips(
      options =
        listOf(
          FilterChipOption("Tots", 12),
          FilterChipOption("Caducat", 2, ExpiryStatus.EXPIRED),
          FilterChipOption("Aviat", 4, ExpiryStatus.SOON),
          FilterChipOption("Fresc", 6, ExpiryStatus.FRESH),
        ),
      selectedIndex = chip,
      onSelect = { chip = it },
    )
    QuickDateChips(
      options =
        listOf(
          QuickDateOption("+1 dia"),
          QuickDateOption("+3 dies"),
          QuickDateOption("+1 setmana"),
          QuickDateOption("Tria", Symbols.Outlined.CalendarMonth),
        ),
      onClick = {},
    )

    Section("Interruptor i icones")
    var on by remember { mutableStateOf(true) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      CaducitySwitch(checked = on, onCheckedChange = { on = it })
      CaducitySwitch(checked = !on, onCheckedChange = { on = !it })
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      androidx.compose.material3.Icon(Symbols.Outlined.HourglassBottom, null)
      androidx.compose.material3.Icon(Symbols.Filled.HourglassBottom, null)
      androidx.compose.material3.Icon(Symbols.Outlined.Kitchen, null)
      androidx.compose.material3.Icon(Symbols.Filled.Kitchen, null)
      androidx.compose.material3.Icon(Symbols.Outlined.Settings, null)
      androidx.compose.material3.Icon(Symbols.Outlined.EventRepeat, null)
      androidx.compose.material3.Icon(Symbols.Outlined.Delete, null)
    }
  }
}

@Composable
private fun Section(title: String) {
  Text(title.uppercase(), style = OverlineTextStyle, color = CaducityTheme.colorScheme.onSurfaceVariant)
}
