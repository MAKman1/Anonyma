package allinontech.anonyma;

import android.app.Application;
import android.content.Context;

public class Anonyma extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        Anonyma.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Anonyma.context;
    }
}