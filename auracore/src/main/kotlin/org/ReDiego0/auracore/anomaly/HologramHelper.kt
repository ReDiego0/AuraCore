package org.ReDiego0.auracore.anomaly // O org.ReDiego0.auracore.helpers

import eu.decentsoftware.holograms.api.DHAPI
import eu.decentsoftware.holograms.api.holograms.Hologram
import org.ReDiego0.auracore.Auracore
import org.bukkit.ChatColor
import org.bukkit.Location

object HologramHelper {
    fun createAnomalyHologram(
        plugin: Auracore,
        hologramName: String,
        location: Location
    ): Hologram? {
        try {
            val adjustedLocation = location.clone().add(0.5, 2.0, 0.5)

            val lines = listOf(
                "${ChatColor.YELLOW}[!] ${ChatColor.GOLD}Anomalía Detectada ${ChatColor.YELLOW}[!]",
                "${ChatColor.AQUA}Clic para intentar canalizar",
                "#ICON: BEACON"
            )
            val hologram = DHAPI.createHologram(hologramName, adjustedLocation, false, lines)

            hologram.enable()
            return hologram
        } catch (e: Exception) {
            plugin.logger.severe("Error al crear holograma '$hologramName': ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    fun deleteAnomalyHologram(hologramName: String) {
        val hologram = DHAPI.getHologram(hologramName)
        hologram?.delete()
    }
}