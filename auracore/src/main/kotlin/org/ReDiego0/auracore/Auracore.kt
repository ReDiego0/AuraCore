package org.ReDiego0.auracore

import com.palmergames.bukkit.towny.TownyAPI
import org.ReDiego0.auracore.anomaly.AnomalyManager
import org.ReDiego0.auracore.anomaly.AnomalyCommands
import org.ReDiego0.auracore.climate.ClimateManager
import org.ReDiego0.auracore.economy.CurrencyCommands
import org.ReDiego0.auracore.economy.CurrencyManager
import org.ReDiego0.auracore.extensions.AuraPAPIExpansion
import org.ReDiego0.auracore.economy.PlayerListener
import org.ReDiego0.auracore.tax.ProtectionListener
import org.ReDiego0.auracore.tax.TaxManager
import org.ReDiego0.auracore.tax.TaxListener
import org.bukkit.plugin.java.JavaPlugin

class Auracore : JavaPlugin() {

    companion object {
        lateinit var instance: Auracore
            private set
    }

    lateinit var climateManager: ClimateManager
        private set

    lateinit var currencyManager: CurrencyManager
        private set

    lateinit var anomalyManager: AnomalyManager
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

        currencyManager = CurrencyManager(this, dataFolder.resolve("balances.yml"))
        currencyManager.loadBalances()
        logger.info("CurrencyManager inicializado y balances.yml cargado.")

        taxManager = TaxManager(this)
        logger.info("TaxManager inicializado.")

        climateManager.startClimateTimer(
            changeInterval = 36000L
        )
        climateManager.startEffectApplicator(
            checkInterval = 60L
        )
        logger.info("Temporizadores de clima iniciados.")

        taxManager.startTaxTimer(1728000L)
        logger.info("Temporizador de impuestos de cc iniciado.")

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            AuraPAPIExpansion(this).register()
            logger.info("PlaceholderAPI encontrado y expansión registrada.")
        } else {
            logger.warning("PlaceholderAPI no se encontró. El scoreboard no mostrará el clima.")
        }

        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(climateManager, this)
        server.pluginManager.registerEvents(TaxListener(this), this)
        server.pluginManager.registerEvents(ProtectionListener(this), this)
        logger.info("Listeners registrados.")

        getCommand("auracc")?.setExecutor(CurrencyCommands(this))
        getCommand("auracore")?.setExecutor(AnomalyCommands(this))
        logger.info("Comandos registrados.")


        logger.info("AuraCore se ha habilitado correctamente.")
    }
    override fun onDisable() {
        logger.info("Desactivando AuraCore...")

        server.scheduler.cancelTasks(this)

        if (::climateManager.isInitialized) {
            climateManager.shutdown()
        }
        if (::currencyManager.isInitialized) {
            currencyManager.saveBalances()
            logger.info("Saldos de CC guardados.")
        }

        logger.info("AuraCore se ha deshabilitado.")
    }
}

