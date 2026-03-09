package com.alorma.caducity.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
  val version: Int,
  val exportDate: Long,
  val categories: List<BackupCategory>,
) {
  companion object {
    const val CURRENT_VERSION = 1
  }
}

@Serializable
data class BackupCategory(
  val id: String,
  val name: String,
  val description: String,
  val products: List<BackupProduct>,
  val standaloneItems: List<BackupProductItem>,
)

@Serializable
data class BackupProduct(
  val id: String,
  val categoryId: String,
  val name: String,
  val createdAt: Long,
  val items: List<BackupProductItem>,
)

@Serializable
data class BackupProductItem(
  val id: String,
  val identifier: String,
  val expirationDate: Long,
  val pausedDate: Long? = null,
  val remainingDays: Int? = null,
  val consumedDate: Long? = null,
  val packSize: Int? = null,
)
