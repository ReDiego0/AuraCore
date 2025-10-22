package org.ReDiego0.auracore.extensions

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.ReDiego0.auracore.Auracore
import org.bukkit.entity.Player
class AuraPAPIExpansion(private val plugin: Auracore) : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "auracore"
    }

    override fun getAuthor(): String {
        return "ReDiego0"
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player?, identifier: String): String? {
        if (identifier == "clima") {
            return plugin.climateManager.activeClimate.papiTag
        }

        return null
    }
}
