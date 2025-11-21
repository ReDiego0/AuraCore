package org.ReDiego0.auracore

import com.palmergames.bukkit.towny.TownyAPI
import org.ReDiego0.auracore.anomaly.AnomalyManager
import org.ReDiego0.auracore.anomaly.AnomalyCommands
import org.ReDiego0.auracore.anomaly.AnomalyInteractionListener
import org.ReDiego0.auracore.climate.ClimateManager
import org.ReDiego0.auracore.economy.CurrencyCommands
import org.ReDiego0.auracore.economy.CurrencyManager
import org.ReDiego0.auracore.extensions.AuraPAPIExpansion
import org.ReDiego0.auracore.economy.PlayerListener
import org.ReDiego0.auracore.generator.GeneratorManager
import org.ReDiego0.auracore.generator.GeneratorProtectionListener
import org.ReDiego0.auracore.generator.GeneratorCommands
import org.ReDiego0.auracore.tax.ClaimCostListener
import org.ReDiego0.auracore.tax.ProtectionListener
import org.ReDiego0.auracore.tax.TaxManager
import org.ReDiego0.auracore.tax.TaxListener
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

class Auracore : JavaPlugin() {

    companion object {
        lateinit var instance: Auracore
            private set
    }

    lateinit var climateManager: ClimateManager
        private set
    lateinit var currencyManager: CurrencyManager
        private set
    lateinit var taxManager: TaxManager
        private set
    lateinit var anomalyManager: AnomalyManager
        private set
    lateinit var generatorManager: GeneratorManager
        private set
    lateinit var townyAPI: TownyAPI
        private set

    // --- VARIABLES PARA GUARDAR LA CONFIG ---
    var claimNormalCostCC: Double = 10.0
    var claimNormalCostMP: Double = 0.0
    var claimOutpostCostCC: Double = 100.0
    var claimOutpostCostMP: Double = 5000.0
    // Generator Config
    var generatorAmount: Double = 120.0
    var generatorIntervalMillis: Long = TimeUnit.HOURS.toMillis(8)
    // Climate Config
    var climateChangeIntervalTicks: Long = 36000L // 30 min
    var climateEffectCheckIntervalTicks: Long = 60L // 3 seg
    // Tax Config
    var taxIntervalTicks: Long = 1728000L // 24 h
    var taxBaseCostCC: Double = 10.0
    var taxCostPerChunkCC: Double = 2.0
    var nextTaxTimeMillis: Long = 0L
    // Anomaly Config
    var anomalyMinCC: Double = 30.0
    var anomalyMaxCC: Double = 120.0
    var anomalyBonusChance: Double = 0.02
    var anomalyBonusAmountCC: Double = 300.0
    // -----------------------------------------


    override fun onEnable() {
        instance = this
        saveDefaultConfig()
        reloadConfig()
        loadConfigValues()
        logger.info("Configuración cargada desde config.yml.")

        townyAPI = TownyAPI.getInstance()

        climateManager = ClimateManager(this)
        logger.info("ClimateManager inicializado.")

        currencyManager = CurrencyManager(this, dataFolder.resolve("balances.yml"))
        currencyManager.loadBalances()
        logger.info("CurrencyManager inicializado y balances.yml cargado.")

        generatorManager = GeneratorManager(this, generatorAmount, generatorIntervalMillis)
        generatorManager.loadGenerators()
        logger.info("GeneratorManager inicializado.")

        taxManager = TaxManager(this, generatorManager)
        logger.info("TaxManager inicializado.")

        anomalyManager = AnomalyManager(this)
        anomalyManager.loadAnomalies()
        logger.info("AnomalyManager inicializado.")

        climateManager.startClimateTimer(climateChangeIntervalTicks)
        climateManager.startEffectApplicator(climateEffectCheckIntervalTicks)
        logger.info("Temporizadores de clima iniciados.")

        val currentTimeMillis = System.currentTimeMillis()
        val taxIntervalMillis = taxIntervalTicks * 50L

        if (nextTaxTimeMillis <= 0L) {
            nextTaxTimeMillis = currentTimeMillis + taxIntervalMillis
            config.set("internal.next-tax-time-millis", nextTaxTimeMillis)
            saveConfig()
            logger.info("Primera ejecución, programando impuestos para dentro de ${taxIntervalMillis / 1000 / 3600} horas.")
        }
        var delayTicks = (nextTaxTimeMillis - currentTimeMillis) / 50L
        if (delayTicks < 0) {
            logger.info("La hora de impuestos ya pasó. Ejecutando ahora...")
            server.scheduler.runTask(this, Runnable {
                taxManager.runTaxCollectionCycle()
            })
            delayTicks = taxIntervalTicks
        } else {
            logger.info("Próximo ciclo de impuestos programado en ${delayTicks / 20 / 60} minutos.")
        }
        taxManager.scheduleNextTaxCycle(delayTicks)

        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            AuraPAPIExpansion(this).register()
            logger.info("PlaceholderAPI encontrado y expansión registrada.")
        } else {
            logger.warning("PlaceholderAPI no se encontró.")
        }

        server.pluginManager.registerEvents(PlayerListener(this), this)
        server.pluginManager.registerEvents(climateManager, this)
        server.pluginManager.registerEvents(TaxListener(this), this)
        server.pluginManager.registerEvents(ProtectionListener(this), this)
        server.pluginManager.registerEvents(AnomalyInteractionListener(this, anomalyMinCC, anomalyMaxCC, anomalyBonusChance, anomalyBonusAmountCC), this)
        server.pluginManager.registerEvents(GeneratorProtectionListener(this), this)
        server.pluginManager.registerEvents(ClaimCostListener(this), this) // Usará config
        logger.info("Listeners registrados.")

        getCommand("auracc")?.setExecutor(CurrencyCommands(this))
        getCommand("auracore")?.setExecutor(AnomalyCommands(this))
        getCommand("gcc")?.setExecutor(GeneratorCommands(this))
        logger.info("Comandos registrados.")

        logger.info("AuraCore se ha habilitado correctamente.")
    }

    private fun loadConfigValues() {
        claimNormalCostCC = config.getDouble("claim.normal.cost-cc", 10.0)
        claimNormalCostMP = config.getDouble("claim.normal.cost-mp", 0.0)
        claimOutpostCostCC = config.getDouble("claim.outpost.cost-cc", 100.0)
        claimOutpostCostMP = config.getDouble("claim.outpost.cost-mp", 5000.0)

        generatorAmount = config.getDouble("generator.production.amount", 120.0)
        generatorIntervalMillis = TimeUnit.HOURS.toMillis(config.getLong("generator.production.interval-hours", 8))

        climateChangeIntervalTicks = TimeUnit.MINUTES.toSeconds(config.getLong("climate.change-interval-minutes", 30)) * 20L
        climateEffectCheckIntervalTicks = config.getLong("climate.effect-check-interval-seconds", 3) * 20L

        taxIntervalTicks = TimeUnit.HOURS.toSeconds(config.getLong("tax.interval-hours", 24)) * 20L
        taxBaseCostCC = config.getDouble("tax.base-cost-cc", 10.0)
        taxCostPerChunkCC = config.getDouble("tax.cost-per-chunk-cc", 2.0)

        anomalyMinCC = config.getDouble("anomaly.reward.min-cc", 30.0)
        anomalyMaxCC = config.getDouble("anomaly.reward.max-cc", 120.0)
        anomalyBonusChance = config.getDouble("anomaly.reward.bonus-chance", 0.02)
        anomalyBonusAmountCC = config.getDouble("anomaly.reward.bonus-amount-cc", 300.0)

        nextTaxTimeMillis = config.getLong("nex-tax-time-millis", 0L)
    }

    override fun onDisable() {
        logger.info("Desactivando AuraCore...")
        server.scheduler.cancelTasks(this)

        if (::taxManager.isInitialized) {
            taxManager.cancelScheduledTask()
        }

        if (::climateManager.isInitialized) {
            climateManager.shutdown()
        }
        if (::currencyManager.isInitialized) {
            currencyManager.saveBalances()
            logger.info("Saldos de CC guardados.")
        }
        if (::generatorManager.isInitialized) {
            generatorManager.saveGenerators()
            logger.info("Generadores guardados.")
        }
        logger.info("AuraCore se ha deshabilitado.")
    }
}