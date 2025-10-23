package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

object PulsoEstatico : Climate{
    override val name: String = "Pulso Estatico"
    override val papiTag: String = "&bPulso Estatico"
    override val type: ClimateType = ClimateType.HOSTILE
    override val duration: Int = 1000

    override fun applyVisuals(world: World) {
        world.setStorm(true)
    }

    override fun applyEffects(player: Player) {
        val slowness = PotionEffect(
            PotionEffectType.SLOWNESS,
            duration,
            1,
            true,
            true,
            true,
        )

        val blindnessDuration = Random.nextInt(20, 60)
        val blindness = PotionEffect(
            PotionEffectType.BLINDNESS,
            blindnessDuration,
            0,
            true,
            true,
            true,
        )

        player.world.spawnParticle(
            org.bukkit.Particle.ELECTRIC_SPARK,
            player.location,
            2,
            0.5, 0.5, 0.5,
            0.0, null, true)

        player.addPotionEffect(slowness)

        if (Random.nextDouble() < 0.15) {
            player.addPotionEffect(blindness)
        }
    }
}