package org.ReDiego0.auracore.anomaly

import org.ReDiego0.auracore.Auracore
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AnomalyData(
    val id: UUID,
    val location: Location,
    val schematicName: String,
    val hologramName: String
)

class AnomalyManager(private val plugin: Auracore) {

    private val anomaliesFile: File = plugin.dataFolder.resolve("anomalies.yml")
    private lateinit var anomaliesConfig: YamlConfiguration
    private val activeAnomalies: ConcurrentHashMap<UUID, AnomalyData> = ConcurrentHashMap()

    fun loadAnomalies() {
        if (!anomaliesFile.exists()) {
            try {
                anomaliesFile.parentFile.mkdirs()
                anomaliesFile.createNewFile()
                plugin.logger.info("Creando nuevo archivo anomalies.yml.")
            } catch (e: IOException) {
                plugin.logger.severe("¡No se pudo crear anomalies.yml! ${e.message}")
                return
            }
        }
        anomaliesConfig = YamlConfiguration.loadConfiguration(anomaliesFile)
        loadActiveAnomaliesFromConfig()
        plugin.logger.info("${activeAnomalies.size} anomalías activas cargadas desde anomalies.yml.")
    }

    private fun loadActiveAnomaliesFromConfig() {
        activeAnomalies.clear()
        anomaliesConfig.getConfigurationSection("anomalies")?.getKeys(false)?.forEach { key ->
            try {
                val id = UUID.fromString(key)
                val path = "anomalies.$key"
                val worldName = anomaliesConfig.getString("$path.world")
                val x = anomaliesConfig.getDouble("$path.x")
                val y = anomaliesConfig.getDouble("$path.y")
                val z = anomaliesConfig.getDouble("$path.z")
                val schematicName = anomaliesConfig.getString("$path.schematic", "default_anomaly") ?: "default_anomaly"
                val hologramName = anomaliesConfig.getString("$path.hologram", id.toString()) ?: id.toString()

                val world = Bukkit.getWorld(worldName ?: "world")
                if (world != null) {
                    val location = Location(world, x, y, z)
                    val data = AnomalyData(id, location, schematicName, hologramName)
                    activeAnomalies[id] = data
                } else {
                    plugin.logger.warning("Mundo '$worldName' no encontrado para la anomalía $id. Omitiendo.")
                }
            } catch (e: Exception) {
                plugin.logger.warning("Error al cargar la anomalía con ID $key: ${e.message}")
            }
        }
    }

    fun saveAnomalies() {
        try {
            anomaliesConfig.set("anomalies", null)
            activeAnomalies.forEach { (id, data) ->
                val path = "anomalies.$id"
                anomaliesConfig.set("$path.world", data.location.world?.name)
                anomaliesConfig.set("$path.x", data.location.x)
                anomaliesConfig.set("$path.y", data.location.y)
                anomaliesConfig.set("$path.z", data.location.z)
                anomaliesConfig.set("$path.schematic", data.schematicName)
                anomaliesConfig.set("$path.hologram", data.hologramName)
            }
            anomaliesConfig.save(anomaliesFile)
        } catch (e: IOException) {
            plugin.logger.severe("¡No se pudo guardar anomalies.yml! ${e.message}")
        }
    }

    fun addAnomaly(location: Location, schematicName: String): AnomalyData? {
        val id = UUID.randomUUID()
        val hologramName = "anomaly_${id.toString().substring(0, 8)}"
        val data = AnomalyData(id, location, schematicName, hologramName)

        activeAnomalies[id] = data
        saveAnomalies()
        plugin.logger.info("Anomalía $id ($schematicName) añadida en $location.")
        return data
    }

    fun removeAnomaly(id: UUID): Boolean {
        val removedData = activeAnomalies.remove(id)
        if (removedData != null) {
            saveAnomalies()
            plugin.logger.info("Anomalía $id removida.")
            return true
        }
        return false
    }

    fun getAnomalyById(id: UUID): AnomalyData? {
        return activeAnomalies[id]
    }

    fun getAnomalyByHologramName(hologramName: String): AnomalyData? {
        return activeAnomalies.values.find { it.hologramName == hologramName }
    }

    fun getAllAnomalies(): Collection<AnomalyData> {
        return activeAnomalies.values
    }
}