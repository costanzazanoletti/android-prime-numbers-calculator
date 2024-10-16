# Esempio di Android Multitasking
## Descrizione del progetto
Questo progetto dimostra come implementare il multitasking in un'app Android attraverso l'uso di `ExecutorService` per eseguire operazioni in background senza bloccare il thrad principale (UI thread). L'app è progettata per calcolare i primi 100 numeri primi in modo asincrono, mostrando l'avanzamento tramite una barra di prograsso e aggiornando la UI con i risultati ottenuti. E' possibile avviare e resettare il calcolo usando i pulsanti **Start** e **Reset**.

## Funzionalità
- **Calcolo asincrono dei numeri primi:** l'app calcola i numeri primi in background utilizzando un thread separato, evitando il blocco della UI.
- **Visualizzazione in tempo reale del progresso:** il progresso del calcolo viene mostrato tramite una barra di avanzamento e aggiornamenti testuali
- **Controllo del flusso di esecuzione:** è possibile avviare il calcolo con il pulsante **Start** e resettarlo con il pulsante **Reset**, che ferma l'esecuzione e ripristina lo stato iniziale
- **Ritardo per simulare operazioni lunghe:** l'app utilizza un ritardo (Thread.sleep()) tra ogni numero primo trovato per simulare operazioni costose in termini di tempo, rendendo evidente il progresso nella UI.

## Requisiti
* Android Studio
* Android SDK 
* Min SDK: 21 (Lollipop)

## Struttura del progetto
- MainActivity.java: contiene tutta la logica dell'applicazione, inclusa l'implementazione dell'esecuzione in background e la gestione dell'interfaccia utente
- activity_main.xml: layout dell'app, con una barra di progresso, due pulsanti e un'area per mostrare lo stato del calcolo

## Scelte progettuali per il multitasking
### 1. ExecutorService
L'app utilizza un `ExecutorService` con un singolo thread (`Executors.newSingleThreadExecutor()`) per gestire il calcolo dei numeri primi in background. Questa scelta consente di eseguire operazioni pesanti senza bloccare il thread principale, permettendo all'interfaccia utente di rimanere reattiva.
```
executorService = Executors.newSingleThreadExecutor();
```
L'uso di `ExecutorService` è preferito rispetto ad altre tecniche di multitasking (come `AsyncTask`, deprecato a partire da Android 11), poichè offre un controllo più esplicito sulla gestione dei thread e sulla loro interruzione.

### 2. Callable e Future per la gestione del task
Il calcolo dei numeri primi viene eseguito in background attraverso un task implementato come `Callable<Void>`. Questo consente di inviare un task all'`ExecutorService` e ottenere un `Future`, che può essere utilizzato per monitorare e controllare il task (ad esempio per annullarlo se necessario).
```
executorService = Executors.newSingleThreadExecutor();
```
Il `Future` associato al task ci consente di annullare il calcolo in corso premendo il pulsante Reset:
```
if (futureTask != null && !futureTask.isCancelled()) {
    futureTask.cancel(true); // Interrompe il task corrente
}
```
### 3. Handler per la comunicazione tra thread
L'aggiornamento della UI in Android può essere eseguito solo dal thread principale. Per permettere al thread di background di comunicare con la UI, è stato utilizzato un `Handler` associato al thread principale (`Looper.getMainLooper()`). Questo Handler riceve messaggi dal task in background e aggiorna la UI in base ai progressi.
```
uiHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message msg) {
        // Aggiornamento UI
    }
};
```
Ogni volta che un nuovo numero promo viene trovato, il task in background invia un messaggio all'`Handler` con i dettagli del progresso:
```
Message message = uiHandler.obtainMessage();
message.arg1 = count; // Progresso
message.arg2 = number; // Numero primo trovato
uiHandler.sendMessage(message);
```
### 4. Gestione dell'interruzione del thread
Per garantire che il task in background possa essere interrotto correttamente, il codice verifica se il thread corrente è stato interrotto (`Thread.currentThread().isInterrupted()`) in ogni iterazione del ciclo di calcolo. Quando il pulsante **Reset** viene premuto, il task in esecuzione viene annullato e il ciclo viene interrotto.

Inoltre, viene gestita l'eccezione `InterruptedException`che può essere lanciata da `Thread.sleep()` nel caso in cui il task venga interrotto durante l'esecuzione:
```
try {
    Thread.sleep(500);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Reimposta lo stato di interruzione
}
```
### 5. UI reattiva e gestione dello stato
* **Start**: Avvia il calcolo dei numeri primi e disabilita temporaneamente il pulsante per evitare che venga premuto più volte
* **Reset**: Interrompe il task in background e ripristina la UI allo stato iniziale, abilitando nuovamente il pulsante **Start**

### 6. Thread.sleep() per simulare un processo lungo
Per rendere visibile l'aggiornamento progressivo della barra di avanzamento e dei risultati, è stato introdotto un ritardo con Thread.sleep(500). Questo simula un'operazione dispendiosa in termini di tempo, come il download di dati o una lunga elaborazione:
```
try {
    Thread.sleep(500);  // Pausa di 500 millisecondi
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // Reimposta lo stato di interruzione
}
```









