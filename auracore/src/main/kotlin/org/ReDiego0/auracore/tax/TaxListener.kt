package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.event.town.toggle.TownTogglePVPEvent
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleMobsEvent
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleFireEvent
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleExplosionEvent
import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TaxListener(private val plugin: Auracore) : Listener {

    private val townData = TownData(plugin)
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"
    private val blockMessage : String = "${prefix}${ChatColor.RED}¡El Aura de tu ciudad ha colapsado! No puedes cambiar este permiso.\""


    @EventHandler
    fun onTownTogglePVP(event: TownTogglePVPEvent) {
        val town = event.town
        if (townData.hasAuraCollapsed(town)) {
            event.isCancelled = true
            event.player?.sendMessage(blockMessage)
        }
    }

    @EventHandler
    fun onTownToggleMobs(event: TownToggleMobsEvent) {
        val town = event.town
        if (townData.hasAuraCollapsed(town)) {
            event.isCancelled = true
            event.player?.sendMessage(blockMessage)
        }
    }

    @EventHandler
    fun onTownToggleFire(event: TownToggleFireEvent) {
        val town = event.town
        if (townData.hasAuraCollapsed(town)) {
            event.isCancelled = true
            event.player?.sendMessage(blockMessage)
        }
    }

    @EventHandler
    fun onTownToggleExplosion(event: TownToggleExplosionEvent) {
        val town = event.town
        if (townData.hasAuraCollapsed(town)) {
            event.isCancelled = true
            event.player?.sendMessage(blockMessage)
        }
    }
}