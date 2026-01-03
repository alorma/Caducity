package com.alorma.caducity.config

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

object ConfigQualifier {
  object DateFormat {
    object HumanReadable: Qualifier {
      override val value: QualifierValue
        get() = "HumanReadable"
    }

    object BackupName: Qualifier {
      override val value: QualifierValue
        get() = "BackupNAme"
    }
  }
}