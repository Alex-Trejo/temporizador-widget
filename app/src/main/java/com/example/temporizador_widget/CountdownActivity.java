package com.example.temporizador_widget;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import android.media.MediaPlayer;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CountdownActivity extends AppCompatActivity {

    private TextView countdownTimeText, countdownText;
    private Button pauseButton, cancelButton;
    private CountDownTimer countDownTimer;
    private long remainingTime, totalTimeInMillis;
    private boolean isPaused = false;
    private boolean isFinished = false;
    private int selectedHours, selectedMinutes, selectedSeconds;
    private Vibrator vibrator;
    private NotificationManager notificationManager;
    private MediaPlayer mediaPlayer; // MediaPlayer para el sonido en bucle


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        // Obtener el tiempo enviado desde MainActivity
        selectedHours = getIntent().getIntExtra("selectedHours", 0);
        selectedMinutes = getIntent().getIntExtra("selectedMinutes", 0);
        selectedSeconds = getIntent().getIntExtra("selectedSeconds", 10);

        // Inicializar elementos de la interfaz
        countdownTimeText = findViewById(R.id.countdownTimeText);
        pauseButton = findViewById(R.id.pauseButton);
        cancelButton = findViewById(R.id.cancelButton);

        // Configurar el temporizador
        totalTimeInMillis = (selectedHours * 3600 + selectedMinutes * 60 + selectedSeconds) * 1000;
        remainingTime = totalTimeInMillis;

        updateTimerText();

        // Mostrar el tiempo recibido
        countdownText = findViewById(R.id.countdownTimeText);
        String formattedTime = String.format("%02d:%02d:%02d", selectedHours, selectedMinutes, selectedSeconds);
        countdownText.setText(formattedTime);

        // Inicializar el Vibrator y NotificationManager
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        startCountdownTimer();

        // Configurar el botón de Pausa/Reiniciar
        pauseButton.setOnClickListener(v -> {
            if (isFinished) {
                // Reiniciar el temporizador al tiempo inicial
                remainingTime = totalTimeInMillis;
                isFinished = false;
                pauseButton.setText("Pausar");
                startCountdownTimer();
            }  else if (isPaused) {
                // Reanudar el temporizador desde donde se pausó
                startCountdownTimer();
                pauseButton.setText("Pausar");
                isPaused = false;
            } else {
                // Pausar el temporizador
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                isPaused = true;

                pauseButton.setText("Reanudar");
            }


            // Detener la vibración y la notificación al reiniciar
            if (vibrator != null) {
                vibrator.cancel();
            }
            // Detener la notificación sonora
            stopAlarm(); // Detener el sonido y vibración al reiniciar

        });

        // Configurar el botón de Cancelar
        cancelButton.setOnClickListener(v -> {
            // Cancelar el temporizador y regresar
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            Toast.makeText(CountdownActivity.this, "Temporizador cancelado", Toast.LENGTH_SHORT).show();
            if (vibrator != null) {
                vibrator.cancel();
            }
             // Detener la notificación sonora
            stopAlarm(); // Detener el sonido y vibración al reiniciar

            finish(); // Regresar a la actividad anterior
        });
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                countdownTimeText.setText("00:00:00");
                Toast.makeText(CountdownActivity.this, "Temporizador finalizado", Toast.LENGTH_SHORT).show();
                // Llamar a la función para enviar la notificación y la vibración continua
                sendNotificationAndVibrate();
                // Cambiar el botón de pausa a reiniciar
                pauseButton.setText("Reiniciar");
                isFinished = true;
            }
        }.start();
        isPaused = false;
    }

    private void updateTimerText() {
        int hours = (int) (remainingTime / 3600000);
        int minutes = (int) ((remainingTime % 3600000) / 60000);
        int seconds = (int) ((remainingTime % 60000) / 1000);

        String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        countdownTimeText.setText(formattedTime);
    }

    private void sendNotificationAndVibrate() {
        // Crear un NotificationChannel para versiones de Android Oreo o superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Temporizador Channel";
            String description = "Canal para notificaciones del temporizador";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("TEMPORIZADOR_CHANNEL", name, importance);
            channel.setDescription(description);

            // Registrar el canal
            notificationManager.createNotificationChannel(channel);
        }

        // Crear un PendingIntent para la acción "Cancelar"
        Intent cancelIntent = new Intent(this, CountdownActivity.class);
        cancelIntent.setAction("ACTION_CANCEL_FROM_NOTIFICATION");
        cancelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent cancelPendingIntent = PendingIntent.getActivity(
                this,
                0,
                cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        // Crear la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "TEMPORIZADOR_CHANNEL")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Reemplazar con tu icono
                .setContentTitle("¡Temporizador Terminado!")
                .setContentText("El temporizador ha llegado a cero.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true) // Mantener la notificación activa
                .addAction(R.drawable.ic_cancel, "Cancelar", cancelPendingIntent); // Botón "Cancelar"


        // Mostrar la notificación
        notificationManager.notify(0, notificationBuilder.build());

        // Iniciar vibración
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 1000}; // Vibración: 0ms, 500ms vibrando, 1000ms pausado
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)); // 0 para repetir continuamente
        }

        // Reproducir sonido en bucle
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.setLooping(true); // Activar bucle
        mediaPlayer.start();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if ("ACTION_CANCEL_FROM_NOTIFICATION".equals(intent.getAction())) {
            // Cancelar el temporizador
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            Toast.makeText(this, "Temporizador cancelado", Toast.LENGTH_SHORT).show();

            // Detener la vibración
            if (vibrator != null) {
                vibrator.cancel();
            }

            // Detener el sonido
            stopAlarm();

            // Finalizar la actividad
            finish();
        }
    }


    private void stopAlarm() {
        // Detener vibración
        if (vibrator != null) {
            vibrator.cancel();
        }
        // Detener el sonido
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Cancelar la notificación
        notificationManager.cancel(0);
    }
}
