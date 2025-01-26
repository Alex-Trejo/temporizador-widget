package com.example.temporizador_widget;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.os.Build;
import android.content.pm.PackageManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class MainActivity extends Activity {

    private NumberPicker hoursPicker, minutesPicker, secondsPicker;
    private TextView timeText;
    private Button startTimerButton;
    private CountDownTimer countDownTimer;
    private int selectedHours = 0, selectedMinutes = 0, selectedSeconds = 10;  // Default time 00:00:10
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Inicializar NumberPickers
        hoursPicker = findViewById(R.id.hoursPicker);
        minutesPicker = findViewById(R.id.minutesPicker);
        secondsPicker = findViewById(R.id.secondsPicker);

        // Inicializar TextView
        timeText = findViewById(R.id.timeText);

        // Configurar NumberPickers
        hoursPicker.setMinValue(0);
        hoursPicker.setMaxValue(99);
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setMaxValue(59);


        // Configurar el valor predeterminado
        hoursPicker.setValue(0);
        minutesPicker.setValue(0);
        secondsPicker.setValue(0);

        // Configuración del botón de inicio
        startTimerButton = findViewById(R.id.startTimerButton);
        startTimerButton.setOnClickListener(v -> {
            // Verificar si los valores de los NumberPickers son cero (no modificados)
            if (hoursPicker.getValue() == 0 && minutesPicker.getValue() == 0 && secondsPicker.getValue() == 0) {
                // Usar el tiempo predeterminado del TextView
                String[] timeParts = timeText.getText().toString().split(":");
                selectedHours = Integer.parseInt(timeParts[0]);
                selectedMinutes = Integer.parseInt(timeParts[1]);
                selectedSeconds = Integer.parseInt(timeParts[2]);
            } else {
                // Obtener los valores seleccionados de los NumberPickers
                selectedHours = hoursPicker.getValue();
                selectedMinutes = minutesPicker.getValue();
                selectedSeconds = secondsPicker.getValue();
            }

            // Validar que el tiempo seleccionado no sea cero
            if (selectedHours == 0 && selectedMinutes == 0 && selectedSeconds == 0) {
                Toast.makeText(MainActivity.this, "Por favor, seleccione un tiempo mayor que 0", Toast.LENGTH_SHORT).show();
            } else {
                // Crear un Intent para ir a CountdownActivity
                Intent countdownIntent = new Intent(MainActivity.this, CountdownActivity.class);
                countdownIntent.putExtra("selectedHours", selectedHours);
                countdownIntent.putExtra("selectedMinutes", selectedMinutes);
                countdownIntent.putExtra("selectedSeconds", selectedSeconds);
                startActivity(countdownIntent);  // Iniciar la actividad CountdownActivity
            }
        });


        // Configuración del botón de configuración
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            // Aquí iría la acción para abrir la configuración
            Toast.makeText(MainActivity.this, "Configuración", Toast.LENGTH_SHORT).show();
        });

        // Establecer tiempo inicial
        updateTimerText();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void updateTimerText() {
        String formattedTime = String.format("%02d:%02d:%02d", selectedHours, selectedMinutes, selectedSeconds);
        timeText.setText(formattedTime);
    }

    private void startTimer() {
        // Obtener los valores seleccionados
        selectedHours = hoursPicker.getValue();
        selectedMinutes = minutesPicker.getValue();
        selectedSeconds = secondsPicker.getValue();

        // Verificar que haya un tiempo válido
        if (selectedHours == 0 && selectedMinutes == 0 && selectedSeconds == 0) {
            Toast.makeText(this, "Por favor, configure el tiempo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular el tiempo total en segundos
        int totalTimeInSeconds = (selectedHours * 3600) + (selectedMinutes * 60) + selectedSeconds;

        // Crear un temporizador de cuenta regresiva
        countDownTimer = new CountDownTimer(totalTimeInSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int hours = (int) (millisUntilFinished / 3600000);
                int minutes = (int) ((millisUntilFinished % 3600000) / 60000);
                int seconds = (int) ((millisUntilFinished % 60000) / 1000);

                selectedHours = hours;
                selectedMinutes = minutes;
                selectedSeconds = seconds;

                updateTimerText(); // Actualizar la UI con el nuevo tiempo restante
            }

            @Override
            public void onFinish() {
                // Acción cuando el temporizador termina
                timeText.setText("00:00:00");
                Toast.makeText(MainActivity.this, "Temporizador terminado", Toast.LENGTH_SHORT).show();
                isTimerRunning = false;
                startTimerButton.setText("Iniciar");
                // Llamar a la función para enviar la notificación
                sendNotification();
            }
        };

        countDownTimer.start();
        isTimerRunning = true;
        startTimerButton.setText("Detener");
    }

    private void stopTimer() {
        // Detener el temporizador
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            startTimerButton.setText("Iniciar");
        }
    }


    private void sendNotification() {
        // Crear un NotificationChannel para versiones de Android Oreo o superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Temporizador Channel";
            String description = "Canal para notificaciones del temporizador";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("TEMPORIZADOR_CHANNEL", name, importance);
            channel.setDescription(description);

            // Registrar el canal
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Crear la notificación
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "TEMPORIZADOR_CHANNEL")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Reemplazar con tu icono
                .setContentTitle("¡Temporizador Terminado!")
                .setContentText("El temporizador ha llegado a cero.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mostrar la notificación
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

        // Hacer que vibre
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)); // Vibración de 500ms
        }
    }
}
