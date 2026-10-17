package com.geovanivargas.planetamusica.domain.sequencer

data class StepPattern(
    val padId: Int,
    val steps: BooleanArray = BooleanArray(16),
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StepPattern) return false
        return padId == other.padId &&
            steps.contentEquals(other.steps) &&
            isMuted == other.isMuted &&
            volume == other.volume
    }

    override fun hashCode(): Int {
        var result = padId
        result = 31 * result + steps.contentHashCode()
        result = 31 * result + isMuted.hashCode()
        result = 31 * result + volume.hashCode()
        return result
    }

    companion object {
        fun createDefaultPatterns(availablePadIds: List<Int>): List<StepPattern> {
            return availablePadIds.map { padId ->
                val steps = BooleanArray(16)
                when (padId) {
                    1 -> { // Kick: 1, 5, 9, 13 (índices 0, 4, 8, 12)
                        steps[0] = true
                        steps[4] = true
                        steps[8] = true
                        steps[12] = true
                    }
                    2 -> { // Snare: 5, 13 (índices 4, 12)
                        steps[4] = true
                        steps[12] = true
                    }
                    3 -> { // Hi-Hat cerrado: cada 2 pasos (0, 2, 4, 6, 8, 10, 12, 14)
                        for (i in 0 until 16 step 2) {
                            steps[i] = true
                        }
                    }
                }
                StepPattern(padId = padId, steps = steps)
            }
        }

        fun encodeToString(patterns: List<StepPattern>): String {
            return patterns.joinToString(";") { p ->
                val bits = p.steps.joinToString("") { if (it) "1" else "0" }
                "${p.padId}:$bits:${if (p.isMuted) 1 else 0}:${p.volume}"
            }
        }

        fun decodeFromString(encoded: String): List<StepPattern> {
            if (encoded.isBlank()) return emptyList()
            return encoded.split(";").mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size >= 4) {
                    val padId = parts[0].toIntOrNull() ?: return@mapNotNull null
                    val bits = parts[1]
                    val steps = BooleanArray(16) { i -> bits.getOrNull(i) == '1' }
                    val isMuted = parts[2] == "1"
                    val volume = parts[3].toFloatOrNull() ?: 1.0f
                    StepPattern(padId = padId, steps = steps, isMuted = isMuted, volume = volume)
                } else null
            }
        }
    }
}
