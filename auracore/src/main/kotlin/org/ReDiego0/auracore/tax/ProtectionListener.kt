package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent
import com.palmergames.bukkit.towny.event.actions.TownyBurnEvent
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent
import com.palmergames.bukkit.towny.event.actions.TownySwitchEvent
import com.palmergames.bukkit.towny.event.actions.TownyItemuseEvent
import org.ReDiego0.auracore.Auracore
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ProtectionListener(private val plugin: Auracore) : Listener {

    private val townData = TownData(plugin)

    @EventHandler(priority = EventPriority.LOW)
    fun onTownyBuild(event: TownyBuildEvent) {
        if (!event.isCancelled) return

        val player = event.player
        val townBlock = event.townBlock ?: return

        val town = try {
            townBlock.town
        } catch (e: Exception) {
            null
        } ?: return

        if (townData.hasAuraCollapsed(town)) {
            if (isOutsider(player, town)) {
                event.isCancelled = false
                event.suppressMessage()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onTownyDestroy(event: TownyDestroyEvent) {
        if (!event.isCancelled) return

        val player = event.player
        val townBlock = event.townBlock ?: return

        val town = try {
            townBlock.town
        } catch (e: Exception) {
            null
        } ?: return

        if (townData.hasAuraCollapsed(town)) {
            if (isOutsider(player, town)) {
                event.isCancelled = false
                event.suppressMessage()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onTownySwitch(event: TownySwitchEvent) {
        if (!event.isCancelled) return
        val player = event.player
        val townBlock = event.townBlock ?: return
        val town = try { townBlock.town } catch (e: Exception) { null } ?: return

        if (townData.hasAuraCollapsed(town)) {
            if (isOutsider(player, town)) {
                event.isCancelled = false
                event.suppressMessage()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onTownyItemUse(event: TownyItemuseEvent) {
        if (!event.isCancelled) return
        val player = event.player
        val townBlock = event.townBlock ?: return
        val town = try { townBlock.town } catch (e: Exception) { null } ?: return

        if (townData.hasAuraCollapsed(town)) {
            if (isOutsider(player, town)) {
                event.isCancelled = false
                event.suppressMessage()
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onTownyBurn(event: TownyBurnEvent) {
        if (event.isCancelled) return

        val townBlock = event.townBlock ?: return
        val town = try { townBlock.town } catch (e: Exception) { null } ?: return

        if (townData.hasAuraCollapsed(town)) {
            event.isCancelled = false
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onTownyExplosion(event: TownyExplodingBlocksEvent) {
        val bukkitEvent = event.bukkitExplodeEvent
        val location = when (bukkitEvent) {
            is org.bukkit.event.entity.EntityExplodeEvent -> bukkitEvent.location
            is org.bukkit.event.block.BlockExplodeEvent -> bukkitEvent.block.location
            else -> return
        }

        val townBlock = TownyAPI.getInstance().getTownBlock(location) ?: return
        val town = try { townBlock.town } catch (e: Exception) { null } ?: return
        if (townData.hasAuraCollapsed(town)) {
            event.blockList = event.vanillaBlockList
        }
    }

    private fun isOutsider(player: Player, town: com.palmergames.bukkit.towny.`object`.Town): Boolean {
        val resident = TownyAPI.getInstance().getResident(player.uniqueId)
        return resident == null || !resident.hasTown() || resident.townOrNull != town
    }
}