<div align="center">

# SWelcome

### Bienvenidas y despedidas que se ven increíbles.

Mensajes de entrada y salida con gradientes, hex y rangos — la primera impresión de tu servidor.

[![build](https://github.com/JhonTova/SWelcome/actions/workflows/build.yml/badge.svg)](https://github.com/JhonTova/SWelcome/actions/workflows/build.yml) [![release](https://img.shields.io/github/v/release/JhonTova/SWelcome?label=release)](https://github.com/JhonTova/SWelcome/releases/latest) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62B47A) ![Java](https://img.shields.io/badge/Java-21-E76F00)

</div>

---

## Características

- **Mensajes de entrada y salida** totalmente personalizables.
- **Gradientes y color hex** (`<gradient:#a:#b>`, `&#ff8800`) gracias a SiriusCore.
- **Mensaje especial de primera vez** para jugadores nuevos.
- **Número de jugador** (`¡Eres el jugador #1234 en unirse!`).
- **Por rango** — mensajes distintos según el grupo de **LuckPerms**.
- Cada jugador puede **activar/desactivar** sus propios mensajes globales.

## Comandos

| Comando | Acción |
|---|---|
| `/swelcome toggle` | Activa o desactiva tus mensajes globales. |
| `/swelcome reload` | Recarga la configuración. |
| `/swelcome help` | Muestra la ayuda. |

## Requisitos

Requiere **[SiriusCore](https://github.com/JhonTova/SiriusCore)**. LuckPerms es opcional (para mensajes por rango).

## Instalación

`SiriusCore.jar` + `SWelcome.jar` en `plugins/`. Spigot / Paper / Purpur 1.21.11+.

## Descarga

El `.jar` de cada versión está en [Releases](https://github.com/JhonTova/SWelcome/releases/latest).

## Compilar desde el código

Requiere Java 21 y Maven. SiriusCore no está en un repositorio Maven público, así que
primero se instala en local la versión que declara el `pom.xml`:

```bash
git clone --branch v1.1.1 https://github.com/JhonTova/SiriusCore.git
mvn -f SiriusCore/pom.xml install -DskipTests
mvn package        # en este repositorio; el .jar queda en target/
```

Cada push y pull request se compila en GitHub Actions. Al subir una tag `v*`, el `.jar`
se adjunta solo a la release.

## Licencia

[MIT](LICENSE): puedes usarlo, modificarlo y redistribuirlo manteniendo el aviso de copyright.
Requiere SiriusCore, que tiene su propia licencia.
