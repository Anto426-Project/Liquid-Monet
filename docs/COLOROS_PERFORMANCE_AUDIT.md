# Ottimizzazioni: riscontri nel dump ColorOS e applicazione nell’SDK

Analisi del 26 settembre 2026 sui file locali forniti in
`/home/anto426/coloros17_work`. Il nome/versione del firmware proviene dal dump
dell’utente; l’analisi riguarda i file identificati dagli hash sotto. Simboli ELF,
stringhe e classi selezionate sono stati ispezionati con `nm`, `strings` e JADX.
Il codice decompilato e i binari del produttore restano fuori dal repository.

## Riscontri e decisioni

| Riscontro verificato | Applicazione in Liquid Monet |
| --- | --- |
| `com.oplus.animation.AnimationHandler` riusa una callback Choreographer e richiede altri frame solo quando esistono animazioni attive. | `DampedDragAnimation` possiede un solo ciclo sul frame clock Compose. Gli eventi aggiornano direttamente i target; cinque canali avanzano insieme e il ciclo termina a riposo. Un test invia 10.000 aggiornamenti prima di un frame e verifica un solo job e l’ultimo target. |
| `SpringForce` memorizza coefficienti, stato massa/velocità e usa soluzioni analitiche nei tre regimi di smorzamento. | Kernel C11 originale con buffer riutilizzato e fallback Kotlin equivalente; una chiamata per gruppo di canali. I parametri estetici già condivisi da navbar e indicatori sono mantenuti. Le inversioni preservano posizione e velocità. |
| `OplusSpringAnimationSet` aggiorna gruppi di proprietà durante lo stesso frame. | I canali di posizione, velocità, pressione e scala hanno target indipendenti e condividono lo stesso intervallo temporale. Non vengono accodate coroutine a ogni movimento del dito. |
| `org.vfx.renderkit.RenderEngine` riusa workspace per geometria e una cache LRU limitata a tre `SharedTexture` per ingresso; rilascia risorse obsolete. | I GraphicsLayer e gli shader dell’SDK erano già riutilizzati e rilasciati con il nodo. Questa modifica aggiunge cache dei pennelli, colori e matrici dei campi di sfondo: durante il moto cambiano le trasformazioni, senza ricostruire le liste di colori e i pennelli a ogni frame. |
| Il percorso `BufferBlur_` seleziona dimensioni pari a 0,25 di quelle originali. `librenderkit.so` espone `KawaseBlurFilter`, `FastBlurFilter` e `OneSixteenthKawaseBlurFilter`. | L’SDK mantiene il campionamento ottico ridotto e il limite di area già esistenti; testo e icone restano a piena risoluzione. Il blur continua a usare il backend GPU Compose/Skia. Questi riscontri non equivalgono ad avere implementato lo stesso filtro Kawase o gli stessi coefficienti del firmware. |
| `SharedTexture` espone adozione di AHardwareBuffer e fence EGL; `libgraphics-core.so` importa transazioni ASurfaceControl/ASurfaceTransaction. | Sono percorsi Android di composizione e sincronizzazione specifici. Compose/Skia continua a possedere superfici, risorse e sincronizzazione anche su iOS; non introduciamo un secondo compositor con copie/readback tra backend. |
| `libParticleSystemLib.so` espone gestione di buffer Vulkan, command buffer e pool di memoria. | L’SDK attuale disegna campi di gradiente, non quel motore particellare. Trasferiamo il principio del riuso delle risorse, senza aggiungere un motore Vulkan estraneo ai componenti. |
| `OplusUIFirstManager` chiama entry point vendor per UX thread, uclamp, ioctl e hint del frame. | Queste dipendenze non sono API pubbliche multipiattaforma. Restano esclusi agganci al framework OPlus, pinning della CPU e forzature della frequenza. La calibrazione CPU/GPU persistente, il warmup e i diagnostici termici dell’SDK restano i meccanismi portabili. |

La documentazione pubblica di [OPPO sul rendering e Trinity Engine](https://www.oppo.com/en/newsroom/stories/oppo-coloros-15-launch-smooth/)
e di [OnePlus su OxygenOS 16](https://www.oneplus.com/us/oxygenos16) descrive gli obiettivi
generali. Le decisioni nella tabella si basano anche sui riscontri nei file locali:
non costituiscono una riproduzione completa dei motori proprietari.

## Verifica

Verifiche locali del 26 settembre 2026:

- 254 file Kotlin controllati, zero violazioni strutturali.
- Kernel C11 compilati con warning trattati come errori; nuovo kernel spring
  passato anche con AddressSanitizer e UBSan.
- 95 test host superati: confronto numerico con Compose, continuità alle inversioni,
  arresto del frame loop, ritorno al rilascio, movimento ridotto e input molto denso.
- 33 test strumentali superati sul OnePlus 13 (CPH2653, Android 16). Il nuovo test
  JNI invoca direttamente la libreria nell’APK, senza accettare il fallback Kotlin.
  La suite comprende gesture, indicatori, menu, testo, calendario e sfondi animati.
- Confronto A/B degli otto sfondi (quattro effetti, due temi) a fase fissa sullo
  stesso emulatore Android 15: immagini 660 × 880 identiche pixel per pixel prima
  e dopo l’introduzione della cache. Il test verifica anche che continuino a muoversi.
- La cache copre il moto continuo dei campi. Cambi di dimensioni, configurazione o
  palette la invalidano intenzionalmente: durante il cambio di tema i gradienti
  vengono ricreati con i colori interpolati per conservare la transizione cromatica.
- Build common metadata e Android debug/release R8 superate; release installata e
  avviata sul OnePlus senza crash o errori JNI nei log raccolti.
- Il workflow macOS esegue i test del bridge C su simulatore iOS Arm64 e collega il
  framework per iPhone Arm64. Questi controlli non sostituiscono una prova visiva
  su iPhone fisico.

Le riduzioni strutturali di lavoro non sono una misura di FPS o autonomia. Una
percentuale di miglioramento richiede un confronto controllato sul dispositivo.

## Identità dei file ispezionati

| File | SHA-256 |
| --- | --- |
| `oplus-framework.jar` | `b2561e6024b22cce861b37ec1f87a7dc3b2f6834fa5a5a5210efc708e14ad6c4` |
| `BlurService.apk` | `db8d30aade8605437f8315be96ec9468c1b7ae3dd719eaff02d3e29da7f0ec38` |
| `librenderkit.so` | `cfc4d8737cc49ff4b448a89fe8978d91bb414184c43b43d10f19dc25a61f4109` |
| `libParticleSystemLib.so` | `3411b4ff0a1212fff7dfdfe50ecd1e16fc1d8e9ca2bb27a64350f0b59072e90f` |
| `libgraphics-core.so` | `55d7e9096f3cf425f135eddd9a79d16599960c4da8e68a8c6c2fe0a4da5218d8` |
