# Motion, optical rendering and resource reuse

## Budget delle card e dei controlli

Le card grandi usano sempre un budget locale leggero: campionamento al massimo 0,5 per asse e
materiale al massimo BALANCED, anche quando il tema richiede ULTRA. La superficie conserva il
fondale campionato, la tinta Monet, il bordo luminoso e la profondità esterna; non attiva la lente
estesa, il blur, la dispersione cromatica o l'ombra interna. Lo stesso trattamento copre LiquidCard,
gli accordion e i contenitori multimediali.

Il limite si applica solo allo sfondo. Testi e icone restano alla risoluzione del layout; i controlli
interni, la navbar e le altre superfici mantengono il profilo del tema. Il campionamento a 0,5 usa
circa un quarto dei pixel di una texture a scala 1, prima dei margini: è una riduzione del lavoro
di campionamento, non una promessa equivalente sul consumo totale o sui tempi dei frame.

Liquid Monet uses original portable C11 kernels and the Compose/Skia rendering backends.
The SDK does not bundle firmware code, proprietary binaries or vendor scheduling APIs.

## Implementation

- `DampedDragAnimation` owns one frame loop. Pointer events update targets directly; five
  position/velocity/press/scale channels advance together and the loop stops when idle.
- The C11 spring kernel caches coefficients and solves all damping regimes analytically.
  A reused batch buffer carries the channels through one native call per frame. Kotlin provides
  an equivalent fallback, and target reversals preserve position and velocity.
- Gradient backgrounds retain brushes, colors and transform matrices while geometry and palette
  stay unchanged. Motion updates transforms in drawing; palette transitions rebuild their colors.
- Graphics layers, shaders and masks have bounded or node-scoped lifetimes. Filtered backdrop
  textures may use reduced resolution while text, icons and foreground controls stay sharp.
- Compose/Skia owns surfaces and synchronization on Android and iOS. The GPU backend supplies blur;
  no separate compositor, custom Vulkan particle engine or vendor Kawase implementation is added.
- Persistent CPU/GPU calibration, shader preparation and thermal diagnostics use portable or
  public platform APIs. No CPU pinning or forced clock frequency is required.

## Rifrazione più marcata

I preset comuni aumentano l’ampiezza della lente senza aumentare il raggio del blur,
il numero di campioni cromatici o la risoluzione dei buffer:

| Preset | Altezza della fascia prima → dopo | Rifrazione prima → dopo |
| --- | --- | --- |
| Subtle | 10 → 10 dp | 16 → 20 dp |
| Standard | 18 → 20 dp | 32 → 44 dp |
| Navigation | 24 → 24 dp | 24 → 32 dp |
| Interactive | 10 → 10 dp | 14 → 18 dp |
| Immersive | 24 → 26 dp | 48 → 64 dp |

Le scale ottiche MINIMAL/BALANCED/HIGH/ULTRA passano da 0,20/0,55/0,85/1,00 a
0,40/0,70/0,95/1,00. MINIMAL abilita la lente soltanto per controlli e navigazione
interattivi, senza aggiungere blur, dispersione cromatica o lenti ai grandi pannelli.
BALANCED la abilita sulle superfici compatte; HIGH e ULTRA conservano le lenti estese.
Intensità zero e piattaforme senza runtime shader continuano a disattivarla.

La fascia hardware regola separatamente il campionamento (0,50/0,67/0,85/1,00):
un dispositivo lento conserva quindi la geometria richiesta con meno pixel intermedi.
Il test GPU confronta entrambe le estremità dell’intervallo a piena e mezza risoluzione,
controllando che il contenuto in primo piano rimanga nitido. La verifica a risoluzione
ridotta sul dispositivo Android di test non equivale a un benchmark su un telefono di fascia bassa.

## Verifica

Verifiche locali del 26 settembre 2026:

- 254 file Kotlin controllati, zero violazioni strutturali.
- Kernel C11 compilati con warning trattati come errori; nuovo kernel spring
  passato anche con AddressSanitizer e UBSan.
- 96 test host superati: confronto numerico con Compose, continuità alle inversioni,
  arresto del frame loop, ritorno al rilascio, movimento ridotto e input molto denso.
- 34 test strumentali superati sul dispositivo CPH2653 con Android 16. Il nuovo test
  JNI invoca direttamente la libreria nell’APK, senza accettare il fallback Kotlin.
  La suite comprende gesture, indicatori, menu, testo, calendario e sfondi animati.
- Confronto A/B degli otto sfondi (quattro effetti, due temi) a fase fissa sullo
  stesso emulatore Android 15: immagini 660 × 880 identiche pixel per pixel prima
  e dopo l’introduzione della cache. Il test verifica anche che continuino a muoversi.
- La cache copre il moto continuo dei campi. Cambi di dimensioni, configurazione o
  palette la invalidano intenzionalmente: durante il cambio di tema i gradienti
  vengono ricreati con i colori interpolati per conservare la transizione cromatica.
- Build common metadata e Android debug/release R8 superate; release installata e
  avviata sul dispositivo Android di test senza crash o errori JNI nei log raccolti.
- Il workflow macOS esegue i test del bridge C su simulatore iOS Arm64 e collega il
  framework per iPhone Arm64. Questi controlli non sostituiscono una prova visiva
  su iPhone fisico.

Le riduzioni strutturali di lavoro non sono una misura di FPS o autonomia. Una
percentuale di miglioramento richiede un confronto controllato sul dispositivo.
