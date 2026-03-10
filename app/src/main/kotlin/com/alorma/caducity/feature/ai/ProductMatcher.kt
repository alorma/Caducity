package com.alorma.caducity.feature.ai

import kotlin.math.max
import kotlin.math.min

fun jaroWinkler(a: String, b: String): Double {
  if (a.isEmpty() || b.isEmpty()) return 0.0
  if (a == b) return 1.0

  val matchDistance = max(a.length, b.length) / 2 - 1
  val aMatches = BooleanArray(a.length)
  val bMatches = BooleanArray(b.length)

  var matches = 0
  var transpositions = 0

  for (i in a.indices) {
    val start = max(0, i - matchDistance)
    val end = min(i + matchDistance + 1, b.length)
    for (j in start until end) {
      if (bMatches[j] || a[i] != b[j]) continue
      aMatches[i] = true
      bMatches[j] = true
      matches++
      break
    }
  }

  if (matches == 0) return 0.0

  var k = 0
  for (i in a.indices) {
    if (!aMatches[i]) continue
    while (!bMatches[k]) k++
    if (a[i] != b[k]) transpositions++
    k++
  }

  val jaro = (matches.toDouble() / a.length +
    matches.toDouble() / b.length +
    (matches - transpositions / 2.0) / matches) / 3.0

  val prefixLength = (0 until min(4, min(a.length, b.length)))
    .takeWhile { a[it] == b[it] }
    .count()

  return jaro + prefixLength * 0.1 * (1.0 - jaro)
}
