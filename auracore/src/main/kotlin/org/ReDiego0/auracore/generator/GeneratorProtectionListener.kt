package org.ReDiego0.auracore.generator

import org.ReDiego0.auracore.Auracore
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.entity.EntityExplodeEvent

class GeneratorProtectionListener(private val plugin: Auracore) : Listener {

    private val generatorManager = plugin.generatorManager

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val blockLocation = event.block.location
        val generator = generatorManager.getGeneratorAtLocation(blockLocation)

        if (generator != null) {
            event.isCancelled = true
            event.player.sendMessage("$${org.bukkit.ChatColor.RED}No puedes destruir un Generador de Aura.")
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        val blockList = event.blockList()
        val iterator = blockList.iterator()
        while (iterator.hasNext()) {
            val block = iterator.next()
            if (generatorManager.getGeneratorAtLocation(block.location) != null) {
                iterator.remove()
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val blockList = event.blockList()
        val iterator = blockList.iterator()
        while (iterator.hasNext()) {
            val block = iterator.next()
            if (generatorManager.getGeneratorAtLocation(block.location) != null) {
                iterator.remove()
            }
        }
    }
}