package com.alorma.caducity.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
  val version: Int,
  val exportDate: Long,
  val products: List<BackupProduct>
) {
  companion object {
    const val CURRENT_VERSION = 1
  }
}

@Serializable
data class BackupProduct(
  val id: String,
  val name: String,
  val description: String,
  val instances: List<BackupProductInstance>
)

@Serializable
data class BackupProductInstance(
  val id: String,
  val identifier: String,
  val expirationDate: Long,
  val pausedDate: Long? = null,
  val remainingDays: Int? = null,
  val consumedDate: Long? = null
)
