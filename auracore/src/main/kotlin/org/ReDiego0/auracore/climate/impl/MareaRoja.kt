package org.ReDiego0.auracore.climate.impl

import org.ReDiego0.auracore.climate.Climate
import org.ReDiego0.auracore.climate.ClimateType
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.Damageable
import kotlin.random.Random

object MareaRoja : Climate {
    override val name: String = "Marea Roja"
    override val papiTag: String = "&cMarea Roja"
    override val duration: Int = 120
    override val type: ClimateType = ClimateType.HOSTILE

    override fun applyVisuals(world: World) {
        world.setStorm(false)
        world.isThundering = false
    }

    override fun applyEffects(player: Player) {
        if (Random.nextInt(3) != 0) return
        val armorContents = player.inventory.armorContents
        var armorChanged = false

        for (i in armorContents.indices) {
            val item = armorContents[i]

            if (item != null && item.type != Material.AIR && item.type.maxDurability > 0) {
                val meta = item.itemMeta as? Damageable ?: continue

                if (meta.damage < item.type.maxDurability - 1) {
                    meta.damage = meta.damage + 1
                    item.itemMeta = meta
                    armorContents[i] = item
                    armorChanged = true
                }
            }
        }

        if (armorChanged) {
            player.inventory.armorContents = armorContents
        }
        val mainHand = player.inventory.itemInMainHand

        if (mainHand.type != Material.AIR && mainHand.type.maxDurability > 0) {
            val meta = mainHand.itemMeta as? Damageable
            if (meta != null) {
                if (meta.damage < mainHand.type.maxDurability - 1) {
                    meta.damage = meta.damage + 1
                    mainHand.itemMeta = meta
                    player.inventory.setItemInMainHand(mainHand)
                    player.playSound(player.location, Sound.ITEM_SHIELD_BREAK, 0.5f, 2.0f)
                }
            }
        }

        player.world.spawnParticle(
            Particle.CRIMSON_SPORE,
            player.location.add(0.0, 1.0, 0.0),
            20,
            3.0, 2.0, 3.0,
            0.0, null, true
        )
    }
}