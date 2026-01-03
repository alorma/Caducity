package com.alorma.caducity.ui.theme.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers

@Preview(name = "Light", group = "Static")
@Preview(name = "Dark", group = "Static", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Dynamic Red", group = "Dynamic", wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE)
@Preview(name = "Dark Dynamic Red", group = "Dynamic", wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Dynamic Green", group = "Dynamic", wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE)
@Preview(name = "Dark Dynamic Green", group = "Dynamic", wallpaper = Wallpapers.GREEN_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Dynamic Blue", group = "Dynamic", wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE)
@Preview(name = "Dark Dynamic Blue", group = "Dynamic", wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Dynamic Yellow", group = "Dynamic", wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE)
@Preview(name = "Dark Dynamic Yellow", group = "Dynamic", wallpaper = Wallpapers.YELLOW_DOMINATED_EXAMPLE, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PreviewDynamicLightDark