package com.alorma.caducity.domain.model

import java.text.Normalizer

private val ACCENTS = Regex("\\p{Mn}+")
private val SPACES = Regex("\\s+")

/** Lowercase, accent-free, trimmed and single-spaced text, used to compare and search names. */
fun String.normalizeForSearch(): String =
  Normalizer
    .normalize(lowercase(), Normalizer.Form.NFD)
    .replace(ACCENTS, "")
    .trim()
    .replace(SPACES, " ")
