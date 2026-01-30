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
      ItemStatus.Expired -> 0
      ItemStatus.ExpiringSoon -> 1
      ItemStatus.Fresh -> 2
      ItemStatus.Frozen -> 3
      ItemStatus.Consumed -> 4
    }
  }.thenBy { it.expirationDate }

  override fun compare(o1: Item, o2: Item): Int {
    return comparator.compare(o1, o2)
  }
}
