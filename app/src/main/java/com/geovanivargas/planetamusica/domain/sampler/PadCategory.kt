package com.geovanivargas.planetamusica.domain.sampler

enum class PadCategory(val label: String) {
    ALL("Todos"),
    PERCUSSION("Percusión"),
    FX("FX"),
    VOICES("Voces"),
    OTHER("Otros");

    companion object {
        /** Categorías asignables a un pad (sin ALL que es solo para filtrar). */
        val assignable: List<PadCategory> = listOf(PERCUSSION, FX, VOICES, OTHER)
    }
}
