package com.alorma.caducity.domain.model

/**
 * Interface for defining different Item sorting strategies.
 */
interface ItemComparator : Comparator<Item>

/**
 * Default comparator for sorting Item objects.
 *
 * Sorting priority:
 * 1. Status (Expired -> ExpiringSoon -> Fresh -> Frozen -> Consumed)
 * 2. Expiration date (earliest first)
 */
class StatusItemComparator : ItemComparator {

  private val comparator = compareBy<Item> {
    when (it.status) {
      InstanceStatus.Expired -> 0
      InstanceStatus.ExpiringSoon -> 1
      InstanceStatus.Fresh -> 2
      InstanceStatus.Frozen -> 3
    }
  }.thenBy { it.expirationDate }

  override fun compare(o1: Item, o2: Item): Int {
    return comparator.compare(o1, o2)
  }
}
