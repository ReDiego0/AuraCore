package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object FlujoVital : Climate{

    override val name: String = "Flujo Vital"
    override val papiTag: String = "&bFlujo Vital"
    override val duration: Int = 120
    override val type: ClimateType = ClimateType.BENEFICIAL

    override fun applyVisuals(world: org.bukkit.World) {
        world.isThundering = false
        world.setStorm(false)
    }

    override fun applyEffects(player: org.bukkit.entity.Player) {
        val regeneration = PotionEffect(
            PotionEffectType.REGENERATION,
            duration,
            1,
            true,
            true,
            true,
        )

        player.world.spawnParticle(
            org.bukkit.Particle.HEART,
            player.location,
            2,
            0.5, 0.5, 0.5,
            0.0, null, true
        )
        player.addPotionEffect(regeneration)

    }

}