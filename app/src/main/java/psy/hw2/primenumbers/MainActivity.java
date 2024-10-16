package psy.hw2.primenumbers;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textView;
    private Button startButton, resetButton;
    private Handler uiHandler;

    private ExecutorService executorService;
    private Future<?> futureTask; // Per gestire e interrompere il task

    private boolean isReset = false;  // Flag per indicare se il calcolo è stato resettato
    private final int NUM_PRIMES = 100; // Numero di numeri primi da calcolare

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        startButton = findViewById(R.id.startButton);
        resetButton = findViewById(R.id.resetButton);

        // Imposta la barra di progresso con il numero totale di numeri primi da calcolare
        progressBar.setMax(NUM_PRIMES);

        // Crea un ExecutorService con un singolo thread
        executorService = Executors.newSingleThreadExecutor();

        // Handler per aggiornare la UI
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Aggiorna la UI con i dati del messaggio ricevuto
                int progress = msg.arg1;
                int primeNumber = msg.arg2;

                if (!isReset) {
                    progressBar.setProgress(progress);
                    textView.setText("Numero primo trovato: " + primeNumber + " (Totale: " + progress + "/" + NUM_PRIMES + ")");
                }

                // Quando il calcolo è completato
                if (progress == NUM_PRIMES && !isReset) {
                    textView.setText("Calcolo completato! Ultimo numero primo: " + primeNumber);
                    startButton.setEnabled(true); // Riattiva il pulsante Start
                }
            }
        };

        // Listener per il pulsante Start
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPrimeCalculation();
                startButton.setEnabled(false); // Disabilita Start durante il calcolo
            }
        });

        // Listener per il pulsante Reset
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isReset = true; // Imposta il reset
                if (futureTask != null && !futureTask.isCancelled()) {
                    futureTask.cancel(true); // Interrompe il task corrente
                }
                resetCalculation(); // Resetta UI e calcoli
            }
        });
    }

    private void startPrimeCalculation() {
        isReset = false; // Cancella lo stato di reset

        // Invia il task all'ExecutorService e ottiene un Future per poterlo interrompere
        futureTask = executorService.submit(new Callable<Void>() {
            @Override
            public Void call() {
                int count = 0;
                int number = 2; // Inizia da 2, il primo numero primo

                while (count < NUM_PRIMES && !Thread.currentThread().isInterrupted()) {
                    if (isPrime(number)) {
                        count++;

                        // Invia un messaggio all'handler per aggiornare la UI
                        Message message = uiHandler.obtainMessage();
                        message.arg1 = count; // Progresso
                        message.arg2 = number; // Numero primo trovato
                        uiHandler.sendMessage(message);

                        // Aggiunge un ritardo di 500 millisecondi tra i numeri primi trovati
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Reimposta lo stato di interruzione
                        }
                    }
                    number++;
                }

                return null; // Task completato
            }
        });
    }

    // Metodo per resettare la UI e ricominciare il calcolo
    private void resetCalculation() {
        textView.setText("Premi Start per iniziare");
        progressBar.setProgress(0);

        // Riabilita il pulsante Start
        startButton.setEnabled(true);
    }

    // Metodo per determinare se un numero è primo
    private boolean isPrime(int num) {
        if (num <= 1) return false;
        for (int i = 2; i <= Math.sqrt(num); i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Chiude l'ExecutorService quando l'attività viene distrutta
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
