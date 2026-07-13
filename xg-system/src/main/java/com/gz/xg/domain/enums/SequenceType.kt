package com.gz.xg.domain.enums

enum class SequenceType(
    val code: Int,
    val description: String
) {
    PRODUCTION_ORDER(1, "Production Order"),
    CARTON_LABEL(2, "Carton Label"),
    PALLET(3,"Pallet"),
    STOCK_IN(4,"Stock In"),
    MOVE_STOCK(5,"Move Stock"),
    PREP_MATERIAL(6,"Perp Material"),
    STOCK_OUT(7,"Stock OUT"),

}
