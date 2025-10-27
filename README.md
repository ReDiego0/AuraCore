# AuraCore - Sistema Central de Mecánicas

AuraCore es el plugin central que gestiona las mecánicas únicas del servidor, enfocándose en la geopolítica, la gestión de recursos estratégicos y las consecuencias ambientales.

**Funcionalidades Principales:**

* **Sistema de Economía (Cristales de Carga - CC):**
    * Introduce los Cristales de Carga (CC) como una moneda estratégica fundamental para la supervivencia y expansión territorial.
    * Incluye un sistema de gestión de saldos persistente por jugador (`balances.yml`).
    * Proporciona comandos de administración (`/auracc`) para consultar, añadir, establecer y remover saldos de CC.
    * Permite la transferencia de CC entre líderes de ciudades para fomentar el comercio (`/auracc pay`).
    * Integra el saldo de CC con [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (`%auracore_cc%`) para visualización en scoreboards u otros plugins.

* **Sistema Dinámico de Climas Hostiles:**
    * Implementa un ciclo de climas cambiantes que afectan exclusivamente a los territorios no reclamados (*wilderness*).
    * Incluye diversos climas hostiles (ej. Tormenta Geomagnética, Marea Roja) con efectos negativos aleatorios o constantes (Fatiga, Hambre, Daño a equipo, Ceguera, Lentitud, Náusea).
    * Incluye climas beneficiosos (ej. Resonancia Armónica, Flujo Vital) que otorgan ventajas temporales (Prisa, Velocidad, Regeneración) en la *wilderness*.
    * Modifica el clima visual global de Minecraft (lluvia, tormenta) para reflejar el estado actual del Aura.
    * Muestra el clima activo y su duración restante mediante una BossBar a los jugadores afectados.
    * Integra el nombre del clima activo con [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (`%auracore_clima%`).

* **Sistema de Impuestos y Colapso del Aura (Depende de [Towny](https://www.spigotmc.org/resources/towny-advanced.72694/)):**
    * Establece un impuesto periódico en CC para las ciudades, calculado en base a su tamaño (número de *chunks*).
    * El impuesto es cobrado automáticamente al líder (Alcalde) de la ciudad desde su saldo personal de CC.
    * Introduce el estado persistente de "Aura Colapsada" para las ciudades cuyo líder no puede pagar el impuesto.
    * **Consecuencias del Colapso:**
        * Fuerza la activación de PvP, aparición de Mobs hostiles, propagación de Fuego y daño por Explosiones dentro de los límites de la ciudad colapsada.
        * Permite que los jugadores externos (*Outsiders*) puedan construir, destruir, interactuar con bloques (Switch) y usar ítems dentro de la ciudad colapsada, eliminando las protecciones territoriales estándar.
    * **Bloqueo de Comandos:** Impide que los alcaldes o asistentes de ciudades colapsadas puedan usar comandos (`/t toggle ...`, `/plot toggle ...`) para desactivar manualmente las consecuencias (PvP, Mobs, Fuego, Explosiones).
    * El estado de colapso y las consecuencias solo se revierten cuando el líder paga el impuesto en el siguiente ciclo de cobro.

* **Sistema de Anomalías del Campo (Depende de [FAWE](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) y [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-papi-support-no-dependencies.96927/)):**
    * Introduce "Anomalías" como eventos localizados y temporales que sirven como fuente secundaria y riesgosa de Cristales de Carga (CC), incentivando la exploración de los Territorios sin Autoridad (*wilderness*).
    * Permite a los administradores colocar anomalías mediante un comando (`/auracore anomalia place <nombreSchematic>`).
    * Cada anomalía consiste en una estructura física predefinida (cargada desde un archivo `.schem` vía FAWE) y un holograma interactivo (gestionado por DecentHolograms) ubicado en un punto específico dentro de la estructura (definido por un bloque marcador en el schematic).
    * Los jugadores que pertenezcan a una ciudad ([Towny](https://www.spigotmc.org/resources/towny-advanced.72694/)) pueden interactuar (haciendo clic) con el holograma de la anomalía.
    * Al interactuar exitosamente:
        * Se otorga una cantidad aleatoria de CC (rango configurable, con baja probabilidad de obtener una bonificación mayor) directamente al saldo del Alcalde de la ciudad del jugador.
        * Se envían mensajes de notificación al jugador y al Alcalde (si está conectado).
        * La anomalía (holograma y registro en `anomalies.yml`) se elimina permanentemente.
    * Los jugadores sin ciudad no pueden reclamar las anomalías.
    * Incluye comandos de administración para listar (`/auracore anomalia list`) y eliminar (`/auracore anomalia remove <ID>`) anomalías activas.
    * Requiere [FAWE](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) y [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-papi-support-no-dependencies.96927/) como dependencias.

* **Sistema de Generadores de CC (Depende de [Towny](https://www.spigotmc.org/resources/towny-advanced.72694/) y [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-papi-support-no-dependencies.96927/)):**
    * Introduce "Generadores de Aura" como puntos fijos y estratégicos en el mapa, sirviendo como la fuente primaria y estable de Cristales de Carga (CC).
    * Permite a los administradores colocar generadores mediante un comando (`/gcc place`) que sitúa un bloque físico específico (ej. Beacon) en la ubicación deseada.
    * Los bloques generadores son indestructibles por jugadores, explosiones o fuego.
    * Cada generador crea automáticamente un holograma informativo (gestionado por DecentHolograms) que muestra el estado del generador (controlador actual, tiempo restante para la próxima generación).
    * De forma periódica (configurable, ej. cada 8 horas), cada generador comprueba qué ciudad ([Towny](https://www.spigotmc.org/resources/towny-advanced.72694/)) controla el *chunk* en el que se encuentra.
    * Si el *chunk* pertenece a una ciudad con alcalde, se otorga una cantidad fija de CC (configurable, ej. 120 CC) directamente al saldo del Alcalde.
    * Se envían mensajes de notificación al Alcalde y a la ciudad al generarse el CC.
    * El holograma se actualiza periódicamente para reflejar el estado actual.
    * Incluye comandos de administración para listar (`/gcc list`) y eliminar (`/gcc remove`) generadores (elimina tanto el bloque físico como el holograma).
    * Requiere [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-papi-support-no-dependencies.96927/) como dependencia.
* **Archivo de Configuración:**

```yml
# ---------------------------------- #
#    Configuración de AuraCore       #
# ---------------------------------- #

# Sistema de Reclamo de Chunks (Claim)
claim:
  normal:
    # Costo en Cristales de Carga (CC) por reclamar un chunk normal.
    cost-cc: 10.0
    # Costo en Moneda Principal (MP - via Vault) por reclamar un chunk normal.
    cost-mp: 0.0
  outpost:
    # Costo en Cristales de Carga (CC) por reclamar un outpost.
    cost-cc: 100.0
    # Costo en Moneda Principal (MP - via Vault) por reclamar un outpost.
    cost-mp: 5000.0

# Sistema de Generadores de CC
generator:
  production:
    # Cantidad de CC que genera cada ciclo.
    amount: 120.0
    # Intervalo de generación en horas.
    interval-hours: 8

# Sistema de Climas Dinámicos
climate:
  # Intervalo en minutos entre cada cambio de clima.
  change-interval-minutes: 30
  # Intervalo en segundos para la comprobación de efectos y actualización de BossBar.
  effect-check-interval-seconds: 3

# Sistema de Impuestos y Colapso del Aura
tax:
  # Intervalo en horas entre cada ciclo de cobro de impuestos.
  interval-hours: 24
  # Costo base fijo de CC por ciclo de impuestos.
  base-cost-cc: 10.0
  # Costo adicional de CC por cada chunk reclamado (después del primero).
  cost-per-chunk-cc: 2.0

# Sistema de Anomalías del Campo
anomaly:
  reward:
    # Mínimo de CC obtenido al reclamar una anomalía.
    min-cc: 30.0
    # Máximo de CC obtenido al reclamar una anomalía (sin bonificación).
    max-cc: 120.0
    # Probabilidad (entre 0.0 y 1.0) de obtener la recompensa excepcional. 0.02 = 2%
    bonus-chance: 0.02
    # Cantidad de CC de la recompensa excepcional.
    bonus-amount-cc: 300.0

# ---------------------------------- #
#      Estado Interno (NO TOCAR)     #
# ---------------------------------- #
internal:
  next-tax-time-millis: 0

```

