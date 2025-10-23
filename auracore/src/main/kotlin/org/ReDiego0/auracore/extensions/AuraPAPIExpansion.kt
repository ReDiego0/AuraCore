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
            return plugin.climateManager.activeClimate.papiTag
        }

        if (identifier == "cc") {
            if (player == null) {
                return "0.0"
            }

            val balance = currencyManager.getBalance(player.uniqueId)
            return DecimalFormat().format(balance)
        }

        return null
    }
}
