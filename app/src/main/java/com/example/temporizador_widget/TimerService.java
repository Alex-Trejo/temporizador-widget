package com.example.temporizador_widget;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class TimerService extends Service {

    private CountDownTimer countDownTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Obtener el tiempo en segundos desde el Intent
        int timeInSeconds = intent.getIntExtra("timeInSeconds", 0);

        // Iniciar el temporizador
        startCountdown(timeInSeconds);

        return START_NOT_STICKY;
    }

    private void startCountdown(int timeInSeconds) {
        countDownTimer = new CountDownTimer(timeInSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Cada tick puede actualizar la UI del widget o realizar alguna acción
            }

            @Override
            public void onFinish() {
                // Notificación al finalizar el temporizador
                showNotification();
                stopSelf(); // Detener el servicio cuando termine
            }
        };
        countDownTimer.start();
    }

    private void showNotification() {
        String channelId = "timer_channel";
        String channelName = "Timer Notification";

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return; // Evitar errores si el sistema no soporta notificaciones
        }
        if (notificationManager != null) {
            // Crear el canal de notificación (para Android 8+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            // Crear la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Temporizador")
                    .setContentText("El temporizador ha finalizado.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            notificationManager.notify(1, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
