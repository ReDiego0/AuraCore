package org.ReDiego0.auracore.tax

import com.palmergames.bukkit.towny.`object`.Town
import org.ReDiego0.auracore.Auracore
import com.palmergames.bukkit.towny.`object`.metadata.BooleanDataField

class TownData(private val plugin: Auracore) {

    companion object {

        const val AURA_COLLAPSED_KEY = "auracore_aura_colapsada"
        private const val AURA_COLLAPSED_LABEL = ""

        val AURA_COLLAPSED_FIELD = BooleanDataField(AURA_COLLAPSED_KEY, false, AURA_COLLAPSED_LABEL)
    }

    fun hasAuraCollapsed(town: Town): Boolean {
        if (!town.hasMeta(AURA_COLLAPSED_KEY)) {
            return false
        }
        val meta = town.getMetadata(AURA_COLLAPSED_KEY)
        return meta is BooleanDataField && meta.value
    }

    fun setAuraCollapsed(town: Town, collapsed: Boolean) {
        if (!town.hasMeta(AURA_COLLAPSED_KEY)) {
            town.addMetaData(AURA_COLLAPSED_FIELD.clone())
        }
        val meta = town.getMetadata(AURA_COLLAPSED_KEY)
        if (meta is BooleanDataField) {
            meta.value = collapsed
        }
        town.save()
    }

    fun setAllProtections(town: Town, enabled: Boolean) {
        val townPerms = town.permissions
        townPerms.pvp = enabled
        townPerms.fire = enabled
        townPerms.explosion = enabled
        townPerms.mobs = enabled

        town.townBlocks.forEach { townBlock ->
            val plotPerms = townBlock.permissions
            plotPerms.pvp = enabled
            plotPerms.fire = enabled
            plotPerms.explosion = enabled
            plotPerms.mobs = enabled

            val permString = plotPerms.toString()
            townBlock.setPermissions(permString)
            townBlock.save()
        }
        town.save()
    }
}
