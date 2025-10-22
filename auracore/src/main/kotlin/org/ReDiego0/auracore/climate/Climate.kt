package org.ReDiego0.auracore.climate

import org.bukkit.World
import org.bukkit.entity.Player

interface Climate {
    val name: String
    val papiTag: String
    val type: ClimateType
    fun applyVisuals(world: World)
    fun applyEffects(player: Player)
}
enum class ClimateType {
    HOSTILE,
    NEUTRAL,
    ENEFICIAL
}
