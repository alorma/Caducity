package com.alorma.caducity.data.backup

import kotlinx.serialization.Serializable

/**
 * A backup is importable if its [version] is between [MIN_SUPPORTED_VERSION] and [CURRENT_VERSION]. A migration that
 * adds fields without defaults bumps [MIN_SUPPORTED_VERSION] too; one whose fields have defaults only bumps
 * [CURRENT_VERSION] and older backups still import. Newer backups are rejected.
 */
@Serializable
data class BackupData(
  val version: Int,
  val exportDate: Long,
  val categories: List<BackupCategory>,
  val transformations: List<BackupTransformation>,
) {
  companion object {
    const val CURRENT_VERSION = 5

    /** Version 4 backups lack «Es pot congelar»; its fields have defaults, so they import as they are. */
    const val MIN_SUPPORTED_VERSION = 4
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
  val canFreeze: Boolean = true,
  val products: List<BackupProduct>,
  val standaloneItems: List<BackupProductItem>,
)

@Serializable
data class BackupProduct(
  val id: String,
  val categoryId: String,
  val name: String,
  val createdAt: Long,
  val canFreeze: Boolean? = null,
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
