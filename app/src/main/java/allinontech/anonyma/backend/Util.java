package allinontech.anonyma.backend;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import allinontech.anonyma.Anonyma;

public class Util {
    private static FirebaseDatabase mDatabase;
    private static FirebaseFirestore firestore;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            FirebaseApp.initializeApp(Anonyma.getAppContext());
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
    public static FirebaseFirestore getFirestore(){
        if( firestore == null){
            FirebaseApp.initializeApp(Anonyma.getAppContext());
            firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build();
            firestore.setFirestoreSettings(settings);
        }
        return firestore;
    }

}