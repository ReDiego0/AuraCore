## AuraCore - Sistema Central de Mecánicas

AuraCore es el plugin central que gestiona las mecánicas únicas del servidor, enfocándose en la geopolítica, la gestión de recursos estratégicos y las consecuencias ambientales.

**Funcionalidades Principales:**

* **Sistema de Economía (Cristales de Carga - CC):**
    * Introduce los Cristales de Carga (CC) como una moneda estratégica fundamental para la supervivencia y expansión territorial.
    * Incluye un sistema de gestión de saldos persistente por jugador (`balances.yml`).
    * Proporciona comandos de administración (`/auracc`) para consultar, añadir, establecer y remover saldos de CC.
    * Permite la transferencia de CC entre jugadores (`/auracc pay`).
    * Integra el saldo de CC con PlaceholderAPI (`%auracore_cc%`) para visualización en scoreboards u otros plugins.

* **Sistema Dinámico de Climas Hostiles:**
    * Implementa un ciclo de climas cambiantes que afectan exclusivamente a los territorios no reclamados (*wilderness*).
    * Incluye diversos climas hostiles (ej. Tormenta Geomagnética, Marea Roja) con efectos negativos aleatorios o constantes (Fatiga, Hambre, Daño a equipo, Ceguera, Lentitud, Náusea).
    * Incluye climas beneficiosos (ej. Resonancia Armónica, Flujo Vital) que otorgan ventajas temporales (Prisa, Velocidad, Regeneración) en la *wilderness*.
    * Modifica el clima visual global de Minecraft (lluvia, tormenta) para reflejar el estado actual del Aura.
    * Muestra el clima activo y su duración restante mediante una BossBar a los jugadores afectados.
    * Integra el nombre del clima activo con PlaceholderAPI (`%auracore_clima%`).

* **Sistema de Impuestos y Colapso del Aura:**
    * Establece un impuesto periódico en CC para las ciudades, calculado en base a su tamaño (número de *chunks*).
    * El impuesto es cobrado automáticamente al líder (Alcalde) de la ciudad desde su saldo personal de CC.
    * Introduce el estado persistente de "Aura Colapsada" para las ciudades cuyo líder no puede pagar el impuesto.
    * **Consecuencias del Colapso:**
        * Fuerza la activación de PvP, aparición de Mobs hostiles, propagación de Fuego y daño por Explosiones dentro de los límites de la ciudad colapsada.
        * Permite que los jugadores externos (*Outsiders*) puedan construir, destruir, interactuar con bloques (Switch) y usar ítems dentro de la ciudad colapsada, eliminando las protecciones territoriales estándar.
    * **Bloqueo de Comandos:** Impide que los alcaldes o asistentes de ciudades colapsadas puedan usar comandos (`/t toggle ...`, `/plot toggle ...`) para desactivar manualmente las consecuencias (PvP, Mobs, Fuego, Explosiones).
    * El estado de colapso y las consecuencias solo se revierten cuando el líder paga el impuesto en el siguiente ciclo de cobro.
