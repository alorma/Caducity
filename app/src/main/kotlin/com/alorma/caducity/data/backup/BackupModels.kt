package com.alorma.caducity.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
  val version: Int,
  val exportDate: Long,
  val categories: List<BackupCategory>,
  // v3; absent in older backups
  val transformations: List<BackupTransformation> = emptyList(),
) {
  companion object {
    const val CURRENT_VERSION = 3
  }
}

@Serializable
data class BackupTransformation(
  val id: String,
  val createdAt: Long,
)

@Serializable
data class BackupCategory(
  val id: String,
  val name: String,
  val description: String,
  // v2; absent in v1 backups (defaults are derived from the name on import)
  val shape: String? = null,
  val icon: String? = null,
  val sortOrder: Int? = null,
  // absent in older v2 files and in v1: follows the global default
  val soonThresholdDays: Int? = null,
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
  // v2; absent in v1 backups
  val discardedDate: Long? = null,
  val createdAt: Long? = null,
  // v3; absent in older backups
  val transformedDate: Long? = null,
  val transformationId: String? = null,
  val originTransformationId: String? = null,
)
