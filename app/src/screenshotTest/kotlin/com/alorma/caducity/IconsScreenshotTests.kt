package com.alorma.caducity

import android.content.res.Configuration
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.tools.screenshot.PreviewTest

/** Les 6 icones adaptatives del llançador (clar i fosc): Rebost, Nevera, Hort / Fleca, Bessa, Camp. */
class IconsScreenshotTests {
  @PreviewTest
  @Preview(name = "Light", widthDp = 360, heightDp = 200)
  @Preview(name = "Dark", widthDp = 360, heightDp = 200, uiMode = Configuration.UI_MODE_NIGHT_YES)
  @Composable
  fun launcherIcons() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(R.drawable.ic_icon_rebost)
        Icon(R.drawable.ic_icon_nevera)
        Icon(R.drawable.ic_icon_hort)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(R.drawable.ic_icon_fleca)
        Icon(R.drawable.ic_icon_bessa)
        Icon(R.drawable.ic_icon_camp)
      }
    }
  }
}

@Composable
private fun Icon(
  @DrawableRes res: Int,
) {
  AndroidView(
    modifier = Modifier.size(96.dp),
    factory = { context -> ImageView(context).apply { setImageResource(res) } },
  )
}
