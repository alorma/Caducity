package com.alorma.caducity.data.backup

import kotlinx.serialization.Serializable

/**
 * A backup is importable only if its [version] is [CURRENT_VERSION], i.e. it validates against the current database.
 * Every database migration bumps [CURRENT_VERSION]; older backups are rejected, never upgraded.
 */
@Serializable
data class BackupData(
  val version: Int,
  val exportDate: Long,
  val categories: List<BackupCategory>,
  val transformations: List<BackupTransformation>,
) {
  companion object {
    const val CURRENT_VERSION = 4
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
  val shape: String,
  val icon: String,
  val sortOrder: Int,
  val usualDurationDays: Int? = null,
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
  val discardedDate: Long? = null,
  val createdAt: Long? = null,
  val transformedDate: Long? = null,
  val transformationId: String? = null,
  val originTransformationId: String? = null,
)
