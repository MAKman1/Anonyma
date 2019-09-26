package allinontech.anonyma.backend;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static android.content.ContentValues.TAG;

public class User {
	//Temp veriables
	static String url;



	static FirebaseFirestore firestore;
	static FirebaseDatabase database;

	public User(){
		firestore = Util.getFirestore();
	}

	public static void getUserLocation( String uid, final MyCallback callback){
		if( firestore == null){
			firestore = Util.getFirestore();
		}
		final DocumentReference docRef = firestore.collection("users").document( uid.trim());
		docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if (task.isSuccessful()) {
					DocumentSnapshot document = task.getResult();
					if (document.exists()) {
						if( document != null && document.get( "current_city") != null){
							callback.onCallback( document.get( "current_city").toString());
						}
					} else {
						Log.d(TAG, "No such document");
						callback.onCallback( null);
					}
				} else {
					Log.d(TAG, "get failed with ", task.getException());
					callback.onCallback( null);
				}
			}
		});
	}

	public static void getUserName(String uid, final MyCallback callback){
		url = "";
		if( database == null){
			database = Util.getDatabase();
			database.getReference().child( "users").keepSynced( true);
		}
		database.getReference().child( "users").child( uid.trim()).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if( dataSnapshot.exists()){
					if( dataSnapshot != null && dataSnapshot.child( "u_name").getValue() != null){
						callback.onCallback( dataSnapshot.child( "u_name").getValue().toString());
					}
					else
						callback.onCallback( null);
				}
				else{
					callback.onCallback( null);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				callback.onCallback( null);
			}
		});
		/*
		DocumentReference docRef = firestore.collection("users").document( uid);
		docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
			@Override
			public void onComplete(@NonNull Task<DocumentSnapshot> task) {
				if (task.isSuccessful()) {
					DocumentSnapshot document = task.getResult();
					if (document.exists()) {
						//Log.d( "ABBA", "DocumentSnapshot data: " + document.getData());
						url = document.get( "u_name").toString();
						callback.onCallback( url);
					} else {
						Log.d( "ABBA", "No such document");
						callback.onCallback( null);
					}
				} else {
					Log.d(TAG, "get failed with ", task.getException());
				}
			}
		});
		*/
	}
	public static void getUserId(String u_name, final MyCallback callback){
		if( database == null){
			database = Util.getDatabase();
			database.getReference().child( "users").keepSynced( true);
		}
		database.getReference().child( "users").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				for( DataSnapshot temp: dataSnapshot.getChildren()){
					if( temp != null && temp.child( "u_name").getValue() != null){

					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});


		/*
		if( firestore == null){
			firestore = Util.getFirestore();
		}
		CollectionReference docRef = firestore.collection("users");
		Query query = docRef.whereEqualTo("u_name", u_name);
		query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if (task.isSuccessful()) {
					for (QueryDocumentSnapshot document : task.getResult()) {
						callback.onCallback(document.getId().toString());
						return;
					}

				} else {
					Log.d(TAG, "get failed with ", task.getException());
				}
				callback.onCallback( null);
			}
		});
		*/

	}
    /*
    readData(new MyCallback() {
    @Override
    public void onCallback(String value) {
        Log.d("TAG", value);
    }
});
     */
}
