package org.ReDiego0.auracore.generator

import org.bukkit.Location
import java.util.UUID

data class GeneratorData(
    val id: UUID,
    val location: Location,
    var nextGenerationTime: Long,
    var hologramName: String? = null
)