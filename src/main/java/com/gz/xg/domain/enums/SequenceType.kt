package com.gz.xg.domain.enums

enum class SequenceType(
    val code: Int,
    val description: String
) {
    PRODUCTION_ORDER(1, "Production Order"),
    CARTON_LABEL(2, "Carton Label"),
    PALLET(3,"Pallet"),
}
