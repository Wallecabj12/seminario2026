# SoundCraft — Estado del Proyecto y Guía de Continuación para Claude Code

> **Documento de Contexto y Transferencia Técnica**  
> **Fecha:** Noviembre 2026  
> **Proyecto:** SoundCraft — Sampler, Secuenciador de Pasos & Sintetizador Virtual (DAW Móvil)  
> **Repositorio:** [https://github.com/Wallecabj12/seminario2026](https://github.com/Wallecabj12/seminario2026) (Rama `main`)  
> **Package:** `com.geovanivargas.planetamusica`

---

## 1. Contexto General y Propósito

**SoundCraft** es una estación de trabajo de audio digital (DAW) móvil para Android, diseñada con una interfaz inspirada en DAWs de escritorio (como FL Studio y BandLab) con identidad propia: estética de hardware vintage/moderno con paleta gris carbón (`#1E2328`), acentos naranja DAW (`#FF8A1F`), perillas rotativas (`DawKnob`), botones LED (`DawButton`), paneles de rack (`DawPanel`), y pads estilo MPC de alto contraste.

La app fue concebida como proyecto universitario y construida de forma incremental a través de 5 iteraciones mayores (Iteraciones 1, 2, 3, 4, 5A, 5B y mejoras de integración).

---

## 2. Stack Tecnológico y Arquitectura

- **Plataforma:** Android (minSdk 24, compileSdk / targetSdk 37).
- **Lenguaje:** Kotlin 2.2.10 (Target JVM 24).
- **UI:** 100% Jetpack Compose con Material 3 (Compose BOM 2026.02.01). **Cero XML de layouts.**
- **Navegación:** Navigation Compose 2.9.0.
- **Arquitectura de UI:** UDF (Unidirectional Data Flow) con `StateFlow`, `UiState` inmutables y `UiEvent` sealed interfaces. Sin lógica de negocio en Composables.
- **Persistencia:** Room 2.7.1 (Base de datos `soundcraft_db` en **Versión 4**, con migraciones `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`) + DataStore Preferences.
- **Audio DSP:**
  - **Sampler:** Android `SoundPool` (baja latencia para 16 streams de one-shots y loops).
  - **Sintetizador:** Motor DSP en **Kotlin puro** (`SynthEngine`), desacoplado de dependencias Android para tests unitarios. Reproducción de vista previa estática vía `AudioTrack` (16-bit PCM Mono 44.1 kHz).
- **Testing:** JUnit 4 + Turbine 1.2.0 + Coroutines Test 1.9.0. 100% de tests unitarios pasando.
- **Inyección de dependencias:** Manual estructurada mediante fábricas (`ViewModelProvider.Factory`).

---

## 3. Estructura de Paquetes

```
app/src/main/java/com/geovanivargas/planetamusica/
├── MainActivity.kt                  # Entry point único, setup de tema y navegación raíz
├── audio/
│   ├── defaults/
│   │   ├── DefaultSoundGenerator.kt # Generación programática de 10 WAVs base (Kick, Snare, Hi-Hat, etc.)
│   │   └── SoundCraftPreferences.kt # DataStore preferences (flag de generación inicial)
│   └── synth/
│       ├── AdsrGenerator.kt         # Generador de envolventes ADSR (amplitud y filtro)
│       ├── AudioExportHelper.kt     # Compartir audio vía FileProvider (Intent.ACTION_SEND) y MediaStore
│       ├── BiquadFilter.kt          # Filtro biquad (LP, HP, BP) con saturación drive y cálculo de curva de frecuencia
│       ├── Effects.kt               # Cadena de efectos: Distorsión tanh, Delay con feedback, Reverb Schroeder
│       ├── FactoryPatches.kt        # 5 patches de fábrica (Bass 808, Lead Synth, Pluck, Ambient Pad, FX Riser)
│       ├── LfoGenerator.kt          # Generador de LFO (Sine, Saw, Square, Triangle, Noise)
│       ├── Oscillators.kt           # Osciladores A y B con anti-aliasing PolyBLEP y Unison (1 a 5 voces)
│       ├── SynthEngine.kt           # Motor de síntesis offline con limitador suave (soft clipper)
│       ├── SynthParams.kt           # Data classes inmutables de parámetros de síntesis
│       ├── SynthParamsJson.kt       # Serializador/deserializador manual JSON para Room
│       └── WavWriter.kt             # Escritor de archivos RIFF WAV PCM 16-bit Mono 44.1 kHz
├── data/
│   ├── FakeAuthRepository.kt        # Simulación de sesión de usuario en memoria
│   ├── presets/
│   │   ├── PresetDao.kt             # DAO Room de presets
│   │   ├── PresetEntity.kt          # Entidad de preset de pads
│   │   ├── PresetPadEntity.kt       # Entidad 1-N de pad individual en preset
│   │   ├── PresetRepositoryImpl.kt  # Implementación de repositorio de presets
│   │   └── PresetWithPads.kt        # Relación @Relation 1 a N
│   ├── sampler/
│   │   ├── FileManager.kt           # Validación de formatos, 10MB/archivo y 50MB total en internal storage
│   │   ├── PadDao.kt                # DAO Room de pads
│   │   ├── PadEntity.kt             # Entidad Pad (id, name, filePath, category, position, volume, isLoop, rate)
│   │   ├── PadMapper.kt             # Mappers entre PadEntity y Pad (domain)
│   │   ├── SamplerRepositoryImpl.kt # Repositorio de pads e inicialización de sonidos por defecto
│   │   ├── SoundCraftDatabase.kt    # RoomDatabase v4 con MIGRATION_1_2, 2_3 y 3_4
│   │   └── SoundPoolAudioPlayer.kt  # Implementación de AudioPlayer con SoundPool (16 streams)
│   └── synth/
│       ├── SynthPatchDao.kt         # DAO Room para patches de sintetizador
│       ├── SynthPatchEntity.kt      # Entidad Room de patch de sintetizador
│       └── SynthPatchRepositoryImpl.kt # Repositorio de patches (fábrica + usuario)
├── domain/
│   ├── AuthRepository.kt / AuthResult.kt / Validators.kt
│   ├── presets/                     # Use Cases: SavePreset, LoadPreset, DeletePreset, RenamePreset, GetPresets
│   ├── sampler/                     # AudioPlayer interface, Pad model, PadCategory enum, SamplerRepository
│   ├── sequencer/                   # SequencerEngine interface, CoroutinesSequencerEngine, StepPattern
│   └── synth/                       # SynthPatch model, SynthPatchRepository interface
└── ui/
    ├── auth/                        # LoginScreen, SignUpScreen, RecoveryScreen, AuthNavGraph
    ├── components/                  # DawKnob, DawButton (LED), DawPanel, LevelMeter
    ├── main/                        # MainScreen (Scaffold DAW, TopBar con Transporte y DawTopTabBar)
    ├── padsettings/                 # PadSettingsSheet (Ajustes de pad + Creador rápido de sintetizador)
    ├── presets/                     # PresetsScreen, PresetsViewModel, PresetsUiState
    ├── sampler/                     # SamplerScreen, PadGrid (16 pads MPC), CategoryChips, PlaybackBar
    ├── sequencer/                   # SequencerScreen (Channel Rack 16 pasos), TransportBar (Play/Stop/Rec/LCD BPM)
    ├── synth/                       # SynthScreen (Rack VST completo), SynthViewModel, SynthUiState
    └── theme/                       # Color.kt (paleta DAW), Theme.kt (DawShapes 4-6dp), Type.kt
```

---

## 4. Qué Está Hecho y Funcionando

### 1. Sistema de Autenticación Opcional (`ui/auth`, `domain/auth`, `data/auth`)
- Pantallas: Login, Sign Up, Recuperar Contraseña.
- Validación de email y contraseñas (mínimo 6 caracteres y coincidencia).
- Botón "Continuar sin cuenta" para que el usuario pueda usar el sampler inmediatamente.
- Sesión en memoria (`FakeAuthRepository`) vinculada a los presets (presets asociados a `userId` o marcados como `"locales"`).

### 2. Sampler / Soundpad 4x4 (`ui/sampler`, `domain/sampler`, `data/sampler`)
- Matriz de 16 pads con diseño tipo MPC (pads de goma con borde luminoso al presionar e index badge `01` a `16`).
- Reproducción instantánea con `SoundPool` al tocar; long-press abre los ajustes avanzados del pad.
- Control de almacenamiento interno: cuota máxima de 50 MB total y 10 MB por archivo de audio. Barra LCD de consumo de memoria en la parte superior.
- Filtro por categorías con `FilterChip` (`Todos`, `Percusión`, `Bajos`, `Synth`, `FX`, `Voces`, `Otros`).
- Generación automática en el primer arranque de 10 sonidos base mediante síntesis algorítmica (`DefaultSoundGenerator`): Kick, Snare, Hi-Hats, Clap, Tom, 808 Bass, Synth Lead, FX Riser, Perc.

### 3. Presets y Ajustes de Pad (`ui/presets`, `ui/padsettings`, `domain/presets`, `data/presets`)
- `PadSettingsSheet` (ModalBottomSheet):
  - Renombrar pad.
  - Selección de categoría.
  - Volumen individual (Slider 0% a 100%).
  - Modo One-shot o Loop continuo.
  - Velocidad/pitch de reproducción (0.5x a 2.0x usando el rate de SoundPool).
  - Reasignación de archivo vía OpenDocument (.wav y .mp3).
  - Eliminación de sonido con reseteo del pad.
- Gestión completa de Presets: guardar estado completo de los 16 pads (incluyendo el patrón del secuenciador), listar, renombrar, cargar con diálogo de confirmación y eliminar.

### 4. Secuenciador de 16 Pasos / Channel Rack (`ui/sequencer`, `domain/sequencer`)
- Interfaz estilo Channel Rack de FL Studio:
  - Filas por cada sonido cargado con nombre, botón LED de Mute, perilla rotativa `DawKnob` de volumen.
  - 16 celdas táctiles agrupadas en bloques de 4 pasos con alternancia de tonalidad carbón.
  - Cursor de reproducción en tiempo real que resalta la columna activa.
- Motor `CoroutinesSequencerEngine`:
  - Disparo de pasos con timing compensado (`SystemClock.elapsedRealtime`) para evitar derivas acumuladas de audio.
  - BPM configurable de 60 a 200 con visualización LCD monoespaciada en color naranja, botones incrementales `+` / `-` y barra de nivel LED (`LevelMeter`).
  - Patrón de ritmo precargado por defecto (Kick en 1/5/9/13, Snare en 5/13, Hi-Hat cada 2 pasos).
  - Serialización del patrón de 16 pasos guardada en Room dentro de cada preset (`patternData`).

### 5. Sintetizador Virtual VST (`audio/synth`, `domain/synth`, `ui/synth`)
- **Motor DSP en Kotlin puro** sin dependencias de NDK:
  - 2 Osciladores independientes con formas Sine, Saw, Square, Triangle y Noise.
  - Algoritmo de anti-aliasing **PolyBLEP** en ondas Saw y Square para eliminar artefactos armónicos digitales.
  - Unison por oscilador: 1 a 5 voces con detune en cents y dispersión estereofónica/fase.
  - Sub-oscilador dedicado (onda senoidal a -1 octava) con nivel ajustable.
  - Filtro biquad resonante (Low-Pass, High-Pass, Band-Pass) con control de corte (20 Hz - 20 kHz), resonancia (Q) y saturación pre-filtro (`drive` tipo tanh).
  - Dos envolventes ADSR completas: Envolvente de Amplitud y Envolvente de Filtro con cantidad de modulación (`filterEnvAmount`).
  - LFO configurable con formas de onda, tasa de 0.1 a 20 Hz, profundidad y ruteo a Cutoff, Pitch o Volumen (trémolo).
  - Cadena de efectos integrada: Distorsión tanh con mix, Delay estéreo con tiempo y feedback, Reverb tipo Schroeder (4 filtros peine + 2 filtros all-pass).
  - Limitador suave (soft-clipper) para evitar distorsión digital o clipping al mezclar voces.
- **Interfaz Visual DAW:**
  - Visor de forma de onda renderizada en tiempo real (~20 ms) en Canvas.
  - Visualización gráfica de la curva de respuesta en frecuencia del filtro y curvas de envolvente ADSR.
  - Teclado táctil interactivo de 1 octava con cambio de octava (`C1` a `C7`) para probar notas.
  - Botón de aleatorización inteligente (`Randomize`) que genera sonidos dentro de rangos musicales y botón de reinicio (`Init`).
  - 5 Patches de fábrica precargados (`Bass 808`, `Lead Synth`, `Pluck`, `Ambient Pad`, `FX Riser`) y almacenamiento ilimitado de patches de usuario en Room (`SynthPatchEntity`).

### 6. Integración Bidireccional Synth ↔ Soundpad y Exportación de Audio
- **Desde el Soundpad (`PadSettingsSheet`):**
  - Botón **"Sintetizar sonido"** que despliega un creador rápido dentro de los ajustes del pad.
  - Permite elegir un instrumento del synth, nota musical (con afinación y notación científica `C2` a `C5`), duración y botón de preescucha.
  - Al presionar **"Asignar a este Pad"**, sintetiza el audio con `SynthEngine`, lo guarda en almacenamiento interno, lo vincula a Room con categoría `SYNTH`, lo carga en SoundPool y lo reproduce inmediatamente como feedback.
  - Enlace directo **"Diseñar en Synth completo ↗"** para abrir el sintetizador con ese pad preseleccionado.
- **Desde el Sintetizador (`SynthScreen`):**
  - Botón **"INTEGRAR CON SOUNDPAD"** con selector que muestra la matriz 4x4 visual completa de los 16 pads (con sus números, nombres actuales o `[Libre]` y color de categoría).
  - Opción de asignar a cualquier pad (reemplazando o rellenando pads libres).
  - Botón **"Asignar y Probar en Soundpad"** que asigna el instrumento y cambia instantáneamente a la pestaña `Pads` para tocarlo.
- **Exportación de Archivos de Audio:**
  - `AudioExportHelper` con `FileProvider` (`res/xml/file_paths.xml`) para compartir el archivo `.wav` mediante `Intent.ACTION_SEND` (`audio/*`) a aplicaciones externas (WhatsApp, Telegram, Google Drive, reproductores de audio o guardado en el dispositivo).

---

## 5. Comandos de Compilación y Test

Todos los comandos se ejecutan desde la raíz del proyecto en PowerShell / Terminal:

```powershell
# 1. Compilar todo el código Kotlin
.\gradlew.bat compileDebugKotlin

# 2. Ejecutar todas las pruebas unitarias
.\gradlew.bat testDebugUnitTest

# 3. Construir el APK de desarrollo
.\gradlew.bat assembleDebug

# 4. Ver estado de git
git status
```

---

## 6. Pruebas Unitarias Implementadas

Ubicación: `app/src/test/java/com/geovanivargas/planetamusica/`

1. **`DefaultSoundGeneratorTest.kt`**: Valida que la generación de audio produzca headers WAV válidos (RIFF, fmt de 16-bit PCM Mono 44.1 kHz, chunk data) y datos no nulos.
2. **`SequencerEngineTest.kt`**: Valida el avance de pasos del secuenciador de 16 pasos, loop continuo y cálculo dinámico de timing según BPM.
3. **`PresetsViewModelTest.kt`**: Valida el guardado y carga de presets con StateFlow usando Turbine.
4. **`AudioSettingsValidationTest.kt`**: Valida los límites y rangos de volumen (0..1) y rate (0.5..2.0) por pad.
5. **`SynthEngineTest.kt`**:
   - `render_returnsCorrectDurationAndValidSampleRangeWithoutClipping`: Valida que el render offline genere la duración exacta de muestras, valores dentro del rango PCM 16-bit (`-32768..32767`) y que el limitador suave impida sobremodulación.
   - `filter_cutoffAttenuatesHighFrequencies`: Valida mediante cálculo de RMS que el filtro biquad Low-Pass a 500 Hz atenúe significativamente una señal de alta frecuencia (4 kHz) en comparación con un corte abierto a 12 kHz.

---

## 7. Qué le Falta por Agregar / Brechas Actuales (Gaps)

Si tomas este proyecto para pulirlo, estas son las áreas funcionales y técnicas donde hay oportunidades claras de mejora:

### Brechas Funcionales
1. **Botón REC de la barra de transporte:** En `TransportBar.kt`, el botón `REC` (con LED rojo) actualmente es decorativo. No graba la sesión en vivo a un archivo maestro de audio.
2. **Exportación de Canción Completa (Mixdown):** Actualmente puedes exportar sonido por sonido del sintetizador o por pad. No existe una función para renderizar la secuencia completa del Channel Rack (los 16 pasos en loop x N repeticiones) a un archivo WAV/MP3 final para compartir la canción completa.
3. **Piano Roll / Secuenciador Melódico para el Synth:** El secuenciador de 16 pasos actual es rítmico (dispara el sonido del pad). El sintetizador tiene teclado para tocar en vivo, pero no hay un piano roll o secuenciador melódico de notas MIDI para crear líneas de bajo o melodías paso a paso dentro del rack.
4. **Backend Real de Autenticación:** `FakeAuthRepository` guarda la sesión en memoria RAM. Cerrar la app desloguea al usuario. No hay persistencia en DataStore de la sesión ni conexión con Firebase Auth o Supabase.
5. **Sincronización en la Nube:** Los presets se guardan exclusivamente en Room local. No hay sincronización en la nube para compartir presets entre dispositivos.

### Brechas Técnicas / Refactorización
1. **Inyección de Dependencias:** Actualmente, los ViewModels instancian dependencias manualmente en sus constructores secundarios (ej: `SoundCraftDatabase.getInstance(application)`, `SoundPoolAudioPlayer`, etc.). Migrar a **Koin** o **Hilt** simplificaría los tests y desacoplaría los repositorios.
2. **Persistencia de Estado Global:** El BPM y el volumen master se resetean al reiniciar la app. Guardar estos ajustes en DataStore Preferences persistiría la configuración entre sesiones.
3. **Manejo del Ciclo de Vida de Audio:** En `MainScreen`, cambiar de pestañas mantiene `SequencerViewModel` vivo, pero asegurar que al minimizar la app (`onPause` / `onStop`) se pause automáticamente la reproducción evita consumo innecesario de batería.

---

## 8. Sugerencias de Pulido Recomendadas (Roadmap de Trabajo)

Priorizadas por impacto para el usuario y calidad del producto:

### Prioridad Alta (Impacto Directo en la Experiencia DAW)
- [ ] **Grabación en Vivo (Live Record / Jam):** Implementar la funcionalidad real del botón `REC`. Al activarlo y dar `PLAY`, un mezclador en memoria o `AudioRecord` graba la mezcla de los pads y el secuenciador hasta presionar `STOP`, guardando un archivo WAV/MP3 en la carpeta de grabaciones.
- [ ] **Exportar Mezcla Completa (Song Export):** Añadir una opción en el menú superior o presets para "Exportar Canción": permite elegir cuántos compases (ej: 4, 8, 16 compases) y genera el archivo WAV de la pista completa para compartirlo externamente.
- [ ] **Feedback Háptico en Pads:** Integrar vibración háptica muy sutil (`VibrationEffect.createPredefined(EFFECT_TICK)`) en `PadGrid` al presionar los pads para simular la respuesta táctil de un pad MPC físico.

### Prioridad Media (Diseño y Potencia Sonora)
- [ ] **Secuenciador Melódico (Piano Roll Simple):** En la pestaña `Rack`, permitir que una pista vinculada a un pad de tipo `SYNTH` se expanda en un mini piano roll para programar melodías (notas C2-C5) en lugar de un solo trigger rítmico.
- [ ] **Codificador MP3 nativo / comprimido:** Actualmente la exportación es PCM WAV 16-bit (compatible y lossless). Integrar un encoder comprimido (AAC `.m4a` con `MediaCodec` nativo de Android o wrapper LAME MP3) para archivos más livianos al compartir por mensajería.
- [ ] **Animación de Perillas y Modulación Visual:** Visualizar el movimiento de las perillas o un indicador de nivel animado en cada canal del rack durante la reproducción.

### Prioridad Baja / Mantenimiento
- [ ] **Migración a Koin:** Configurar módulos de inyección para Room, repositorios y reproductores de audio.
- [ ] **Persistencia en DataStore:** Guardar BPM actual, volumen master y último preset cargado.

---

## 9. Convenciones de Código y Reglas de Estilo del Proyecto

- **Sin XML de layouts:** Todo componente visual debe ser Jetpack Compose.
- **Tema Central:** Usar siempre los tokens de color definidos en `Color.kt`:
  - Fondo: `DawDarkBackground` (`#1E2328`)
  - Paneles: `DawPanelBackground` (`#242A30`)
  - Bordes: `DawPanelBorder` (`#38404A`) con grosor `1.dp`
  - Acento principal: `DawAccentOrange` (`#FF8A1F`)
  - Displays numéricos/BPM: `DawLcdBackground` (`#121518`) con texto monoespaciado naranja
  - Esquinas: `DawShapes` con radios de 4dp a 6dp (rectas estilo rack de hardware).
- **Accesibilidad táctil:** Todo botón, pad, perilla o tab debe respetar un target táctil mínimo de **48dp x 48dp** (`Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`).
- **DSP y Audio:** Mantener las clases matemáticas y de síntesis en `audio/synth/` en **Kotlin puro** (sin `android.*`), asegurando que puedan someterse a pruebas unitarias en la JVM sin emulador ni mocks complejos.
- **Commits en Git:** Utilizar commits atómicos con formato convencional: `feat(modulo): ...`, `fix(modulo): ...`, `refactor(modulo): ...`, `test(modulo): ...`.
