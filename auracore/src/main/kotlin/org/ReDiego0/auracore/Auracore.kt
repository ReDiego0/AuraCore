package org.ReDiego0.auracore

import com.palmergames.bukkit.towny.TownyAPI
import org.ReDiego0.auracore.climate.ClimateManager
import org.ReDiego0.auracore.extensions.AuraPAPIExpansion
import org.ReDiego0.auracore.tax.TaxManager
import org.bukkit.plugin.java.JavaPlugin

class Auracore : JavaPlugin() {

    companion object {
        lateinit var instance: Auracore
            private set
    }

    lateinit var climateManager: ClimateManager
        private set

    lateinit var taxManager: TaxManager
        private set

    lateinit var townyAPI: TownyAPI
        private set

    override fun onEnable() {
        instance = this
        townyAPI = TownyAPI.getInstance()

        climateManager = ClimateManager(this)
        logger.info("ClimateManager inicializado.")

        taxManager = TaxManager(this)
        logger.info("TaxManager inicializado.")

        climateManager.startClimateTimer(
            changeInterval = 36000L
        )
        climateManager.startEffectApplicator(
            checkInterval = 20L
        )
        logger.info("Temporizadores de clima iniciados.")

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            AuraPAPIExpansion(this).register()
            logger.info("PlaceholderAPI encontrado y expansión registrada.")
        } else {
            logger.warning("PlaceholderAPI no se encontró. El scoreboard no mostrará el clima.")
        }

        logger.info("AuraCore se ha habilitado correctamente.")
    }
    override fun onDisable() {
        logger.info("Desactivando AuraCore...")
        server.scheduler.cancelTasks(this)
        if (::climateManager.isInitialized) (
                climateManager.shutdown()
        )

        logger.info("AuraCore se ha deshabilitado.")
    }
}