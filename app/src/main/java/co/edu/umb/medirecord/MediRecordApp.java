package co.edu.umb.medirecord;

import android.app.Application;

import co.edu.umb.medirecord.alarm.NotificationHelper;

public class MediRecordApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.crearCanal(this);
    }
}
