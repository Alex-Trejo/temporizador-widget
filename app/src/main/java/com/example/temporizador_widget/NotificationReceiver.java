package com.example.temporizador_widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationManager;
import android.os.Vibrator;
import android.widget.Toast;


public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("ACTION_CANCEL".equals(action)) {

            // Enviar un intent a la actividad principal para que cancele el temporizador
            Intent activityIntent = new Intent(context, CountdownActivity.class);
            activityIntent.setAction("ACTION_CANCEL_FROM_NOTIFICATION");
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(activityIntent);


            // Obtener el NotificationManager para cancelar la notificación
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(0); // Cancela la notificación con ID 0
            }

            // Detener vibración y sonido
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.cancel();
            }

            // Mostrar un mensaje al usuario
            Toast.makeText(context, "Temporizador cancelado desde la notificación", Toast.LENGTH_SHORT).show();
        }
    }

}
