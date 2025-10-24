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
        town.permissions.pvp = enabled
        town.permissions.fire = enabled
        town.permissions.explosion = enabled
        town.permissions.mobs = enabled
        town.save()
        town.townBlocks.forEach { townBlock ->
            townBlock.save()
        }

    }
}
