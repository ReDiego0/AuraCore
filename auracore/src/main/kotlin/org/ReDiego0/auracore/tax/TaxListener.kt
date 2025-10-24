package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.event.town.toggle.TownTogglePublicEvent
import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class TaxListener(private val plugin: Auracore) : Listener {

    private val townData = TownData(plugin)
    private val prefix = "${ChatColor.AQUA}[AuraCore] ${ChatColor.GRAY}"

    @EventHandler
    fun onTownToggle(event: TownTogglePublicEvent) {
        val town = event.town
        if (townData.hasAuraCollapsed(town)) {
            event.isCancelled = true
            val player = event.player
            player?.sendMessage("${prefix}${ChatColor.RED}¡El Aura de tu ciudad ha colapsado!")
            player?.sendMessage("${prefix}${ChatColor.RED}No puedes cambiar las protecciones hasta que el impuesto de CC sea pagado.")
        }
    }
}