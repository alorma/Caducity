package com.alorma.caducity.config

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

object ConfigQualifier {
  object DateFormat {
    object HumanReadable: Qualifier {
      override val value: QualifierValue = "HumanReadable"
    }

    object BackupName: Qualifier {
      override val value: QualifierValue = "BackupName"
    }
  }

  object Palette {
    object Default: Qualifier {
      override val value: QualifierValue = "PaletteDefault"
    }
    object Vibrant: Qualifier {
      override val value: QualifierValue = "PaletteVibrant"
    }
    object Soft: Qualifier {
      override val value: QualifierValue = "PaletteSoft"
    }
  }
}