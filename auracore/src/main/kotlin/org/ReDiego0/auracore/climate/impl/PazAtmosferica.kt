package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.World
import org.bukkit.entity.Player

object PazAtmosferica : Climate {

    override val name: String = "Paz Atmosférica"
    override val papiTag: String = "&aPaz Atmosférica"
    override val type: ClimateType = ClimateType.NEUTRAL

    override fun applyVisuals(world: World) {
        world.setStorm(false)
        world.isThundering = false
    }

    override fun applyEffects(player: Player) {
    }
}
