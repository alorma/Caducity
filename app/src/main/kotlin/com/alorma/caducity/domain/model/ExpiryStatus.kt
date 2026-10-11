package com.alorma.caducity.domain.model

/** Estat visual d'un producte: determina color i etiqueta. El càlcul pur arriba a la fase 2. */
enum class ExpiryStatus {
  EXPIRED,
  SOON,
  FRESH,
  FROZEN,
}
