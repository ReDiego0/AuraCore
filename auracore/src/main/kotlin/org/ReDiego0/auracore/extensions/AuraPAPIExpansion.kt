package org.ReDiego0.auracore.extensions

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.ReDiego0.auracore.Auracore
import org.bukkit.entity.Player
import java.text.DecimalFormat

class AuraPAPIExpansion(private val plugin: Auracore) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "auracore"
    override fun getAuthor(): String = "ReDiego0"
    override fun getVersion(): String = plugin.description.version
    override fun persist(): Boolean = true

    private val currencyManager = plugin.currencyManager

    override fun canRegister(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (identifier == "clima") {
            if (player == null) return "&7Desconocido"
            val currentClimate = plugin.climateManager.getClimateForWorld(player.world.name)
            return currentClimate?.papiTag ?: "&7Estable"
        }

        if (identifier == "cc") {
            if (player == null) {
                return "0.0"
            }
            val df = DecimalFormat("#.##")
            val balance = currencyManager.getBalance(player.uniqueId)
            return df.format(balance)
        }

        return null
    }
}