package com.geovanivargas.planetamusica.data.sampler

import com.geovanivargas.planetamusica.domain.sampler.Pad
import com.geovanivargas.planetamusica.domain.sampler.PadCategory

fun PadEntity.toDomain() = Pad(
    id = id,
    name = name,
    filePath = filePath,
    category = runCatching { PadCategory.valueOf(category) }.getOrDefault(PadCategory.OTHER),
    position = position,
    volume = volume,
    isLoop = isLoop,
    rate = rate,
)

fun Pad.toEntity() = PadEntity(
    id = id,
    name = name,
    filePath = filePath,
    category = category.name,
    position = position,
    volume = volume,
    isLoop = isLoop,
    rate = rate,
)
