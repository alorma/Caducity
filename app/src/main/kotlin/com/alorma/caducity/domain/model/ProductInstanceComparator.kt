package com.alorma.caducity.domain.model

/**
 * Interface for defining different ProductInstance sorting strategies.
 */
interface ProductInstanceComparator : Comparator<ProductInstance>

/**
 * Default comparator for sorting ProductInstance objects.
 *
 * Sorting priority:
 * 1. Status (Expired -> ExpiringSoon -> Fresh -> Frozen -> Consumed)
 * 2. Expiration date (earliest first)
 */
class StatusProductInstanceComparator : ProductInstanceComparator {

  private val comparator = compareBy<ProductInstance> {
    when (it.status) {
      InstanceStatus.Expired -> 0
      InstanceStatus.ExpiringSoon -> 1
      InstanceStatus.Fresh -> 2
      InstanceStatus.Frozen -> 3
    }
  }.thenBy { it.expirationDate }

  override fun compare(o1: ProductInstance, o2: ProductInstance): Int {
    return comparator.compare(o1, o2)
  }
}
