package allinontech.anonyma;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.type.LatLng;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import allinontech.anonyma.backend.MyCallback;
import allinontech.anonyma.backend.SharedPreference;
import allinontech.anonyma.backend.User;
import allinontech.anonyma.backend.Util;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class Login extends AppCompatActivity {
    private SharedPreference sharedPreferenceObj;

    static FirebaseFirestore firestore;
    static FirebaseDatabase database;

    private FusedLocationProviderClient mFusedLocationClient;
    private Geocoder gcd;

    private LocationRequest mLocationRequest;

    GoogleApiClient googleApiClient;

    Button signUp;
    Button logIn;
    Button openSignUp;
    Button openLogIn;
    Button cityButton;

    ScrollView loginForm;
    ScrollView signupForm;

    ProgressDialog dialog;

    EditText loginuname;
    EditText loginpass;
    EditText signupuname;
    EditText signuppass;

    ProgressBar progress;

    String currentCityName;
    Location location;
    List<Address> addresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //FirebaseApp.initializeApp( this);

        signUp = findViewById(R.id.signupbutton);
        logIn = findViewById(R.id.loginbutton);
        openSignUp = findViewById(R.id.opensignupbutton);
        openLogIn = findViewById(R.id.openloginbutton);
        cityButton = findViewById(R.id.city_button);

        loginForm = findViewById(R.id.logInForm);
        signupForm = findViewById(R.id.signUpForm);

        loginuname = findViewById(R.id.loginusername);
        loginpass = findViewById(R.id.loginpassword);
        signupuname = findViewById(R.id.signupusername);
        signuppass = findViewById(R.id.signuppassword);

        progress = findViewById(R.id.progress);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        googleApiClient = new GoogleApiClient.Builder( Login.this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        cityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {
                    //cityButton.setEnabled( false);
                    if (ActivityCompat.checkSelfPermission(Login.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Login.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Login.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                1);
                    } else {
                        displayLocationSettingsRequest();
                    }
                } else {
                    Toast.makeText(Login.this, "Location cannot be retrieved (internet not available), please connect to network and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    try {
                        logIn.setEnabled(false);


                        String uname = loginuname.getText().toString().trim();
                        String pass = loginpass.getText().toString().trim();

                        if (uname == null || pass == null || uname.equals("") || pass.equals("")) {
                            Toast.makeText(Login.this, "Kindly enter a username and password!", Toast.LENGTH_SHORT).show();
                            logIn.setEnabled(true);

                        } else {
                            loginForm.setAlpha((float) 0.2);
                            progress.setVisibility(View.VISIBLE);
                            logUserIn(uname, pass);
                        }
                    } catch (Exception e) {
                        Log.d("DADDY", e.toString());
                        logIn.setEnabled(true);
                    }
                } else {
                    Toast.makeText(Login.this, "Login failed (internet not available), please connect to network and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    signUp.setEnabled(false);

                    String uname = signupuname.getText().toString().trim();
                    String pass = signuppass.getText().toString().trim();

                    if (currentCityName == null) {
                        signUp.setEnabled(true);
                        Toast.makeText(Login.this, "Kindly add your city before proceeding", Toast.LENGTH_SHORT).show();
                    } else if (uname == null || pass == null || uname.equals("") || pass.equals("")) {
                        signUp.setEnabled(true);
                        Toast.makeText(Login.this, "Kindly enter a username and password!", Toast.LENGTH_SHORT).show();
                    } else {
                        signupForm.setAlpha((float) 0.2);
                        progress.setVisibility(View.VISIBLE);
                        addUser(uname, pass, currentCityName);
                    }
                } else {
                    signUp.setEnabled(true);
                    Toast.makeText(Login.this, "Sign-up failed (internet not available), please connect to network and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        openLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupForm.setVisibility(View.GONE);
                loginForm.setVisibility(View.VISIBLE);
            }
        });

        openSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginForm.setVisibility(View.GONE);
                signupForm.setVisibility(View.VISIBLE);
            }
        });



    }

    public void logUserIn(final String u_name, final String password) {
        if (firestore == null) {
            firestore = Util.getFirestore();
        }
        if (database == null) {
            database = Util.getDatabase();
        }


        CollectionReference docRef = firestore.collection("users");
        Query query = docRef.whereEqualTo("u_name", u_name.trim()).whereEqualTo("password", password.trim());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (final QueryDocumentSnapshot document : task.getResult()) {
                        sharedPreferenceObj.setLogin(true);
                        Log.d("DADDY", document.getId().toString());
                        database.getReference().child("users").child(document.getId().toString()).child("u_name").setValue(u_name.trim());
                        User.getUserLocation(document.getId().trim(), new MyCallback() {
                            @Override
                            public void onCallback(String value) {
                                if (value != null) {
                                    Log.d("DADDY", value.toString());
                                    sharedPreferenceObj.setCity(value.trim());
                                }
                            }
                        });
                        sharedPreferenceObj.setUName(u_name.trim());
                        sharedPreferenceObj.setUid(document.getId().trim());
                        Intent i = new Intent(Login.this, HomeScreen.class);
                        startActivity(i);
                        finish();
                        return;
                    }
                }
                Log.d(TAG, "get failed with ", task.getException());
                logIn.setEnabled(true);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(Login.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(Login.this);
                }
                loginForm.setAlpha((float) 1.0);
                progress.setVisibility(View.GONE);
                builder.setTitle("Login failed")
                        .setMessage("Invalid username or password.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNeutralButton("RETRY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();
                return;
            }
        });
    }

    public void addUser(final String u_name, final String password, final String city_name) {
        if (firestore == null) {
            firestore = Util.getFirestore();
        }

        final MyCallback callback = new MyCallback() {
            @Override
            public void onCallback(String value) {
                if (value.equals("1")) {
                    signupForm.setAlpha((float) 1.0);
                    progress.setVisibility(View.GONE);
                    signUp.setEnabled(true);
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(Login.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(Login.this);
                    }
                    builder.setTitle("Signup error")
                            .setMessage("Username already exists, please try another one.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNeutralButton("RETRY", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            })
                            .show();
                } else {
                    final Map<String, Object> data = new HashMap<>();
                    data.put("u_name", u_name);
                    data.put("password", password);
                    data.put("current_city", city_name);

                    firestore.collection("users").document()
                            .set(data, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("ABBA", "DocumentSnapshot successfully written!");
                                    sharedPreferenceObj.setCity(city_name);
                                    logUserIn(u_name, password);
                                    signupForm.setAlpha((float) 1.0);
                                    progress.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("ABBA", "Error writing document", e);
                                    signupForm.setAlpha((float) 1.0);
                                    progress.setVisibility(View.GONE);
                                    signUp.setEnabled(true);
                                    AlertDialog.Builder builder;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        builder = new AlertDialog.Builder(Login.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                                    } else {
                                        builder = new AlertDialog.Builder(Login.this);
                                    }
                                    builder.setTitle("Signup error")
                                            .setMessage("Failed to sign-up, please try again")
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setNeutralButton("RETRY", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.cancel();
                                                }
                                            })
                                            .show();
                                }
                            });
                }
            }
        };

        CollectionReference docRef = firestore.collection("users");
        Query query = docRef.whereEqualTo("u_name", u_name);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        callback.onCallback("1");
                        return;
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
                callback.onCallback("0");
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        sharedPreferenceObj = new SharedPreference(Login.this);

        if( sharedPreferenceObj.getRunFirst()){
            sharedPreferenceObj.setLogin( false);

            signupForm.setVisibility(View.VISIBLE);
            loginForm.setVisibility(View.GONE);

            Intent i = new Intent( Login.this, AnonymaIntro.class);
            startActivity( i);
        }
        if (sharedPreferenceObj.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, HomeScreen.class));
        }
    }


    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        dialog = ProgressDialog.show(Login.this, "RETRIEVING LOCATION",
                "Please wait...", true);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        getFusedLocationProviderClient( this).getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            onLocationChanged(location);
                        }
                        else{
                            getFusedLocationProviderClient(Login.this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                                        @Override
                                        public void onLocationResult(LocationResult locationResult) {
                                            Log.d( "ABBA", "location updated");
                                            onLocationChanged(locationResult.getLastLocation());
                                        }
                                    },
                                    Looper.myLooper());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ABBA", "Error trying to get last GPS location");
                        getFusedLocationProviderClient( Login.this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        Log.d( "ABBA", "location updated");
                                        onLocationChanged(locationResult.getLastLocation());
                                    }
                                },
                                Looper.myLooper());
                    }
                });
    }
    public void onLocationChanged(Location loc) {
        Log.d( "ABBA", "changed");
        location = loc;
        if( mFusedLocationClient == null){ mFusedLocationClient = getFusedLocationProviderClient(this);}
        if( gcd == null){ gcd = new Geocoder( this, Locale.getDefault());}
        if (location != null) {
            try {
                if( addresses == null){
                    addresses = gcd.getFromLocation( location.getLatitude(), location.getLongitude(), 1);
                }
                if (addresses != null && addresses.size() > 0) {
                    if( dialog != null)
                        dialog.cancel();

                    if( addresses.get( 0).getCountryName() != null) {
                        currentCityName = addresses.get(0).getCountryName().toUpperCase();
                        cityButton.setText("COUNTRY: " + currentCityName);
                    }
                    else {
                        currentCityName = "DEFAULT";
                        cityButton.setText("LOCATION: " + currentCityName);
                    }
                }
                else{
                    addresses = gcd.getFromLocation( location.getLatitude(), location.getLongitude(), 1);
                    onLocationChanged( location);
                }
            } catch (IOException e) {
                onLocationChanged( location);
            }
        }
        else{
            Toast.makeText( Login.this, "Error retrieving location, please retry again.", Toast.LENGTH_SHORT).show();
        }
    }


	private void displayLocationSettingsRequest() {

        Log.d( "ABBA", "came in");
		mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(60*1000);
        mLocationRequest.setFastestInterval(1*500);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest( mLocationRequest);
        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    startLocationUpdates();
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.i("ABBA", "All location settings are satisfied.");
                            startLocationUpdates();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        Login.this,
                                        150);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            Toast.makeText( Login.this, "Failed to retrieve location, please try again.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			// Check for the integer request code originally supplied to startResolutionForResult().
			case 150:
				switch (resultCode) {
					case Activity.RESULT_OK:
					    Log.d( "ABBA", "bismillah");
						startLocationUpdates();
						break;
					case Activity.RESULT_CANCELED:
						AlertDialog.Builder builder;
                        Log.d( "ABBA", "subhanallah");
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
							builder = new AlertDialog.Builder( Login.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
						} else {
							builder = new AlertDialog.Builder( Login.this);
						}
						builder.setTitle("Error!")
								.setMessage("Anonyma needs to know your city for localized content. Please switch location services on.")
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setNeutralButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialogInterface, int i) {
										dialogInterface.cancel();
									}
								})
								.show();//keep asking if imp or do whatever
						break;
				}
				break;
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {

				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 1
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
					displayLocationSettingsRequest();
				} else {
					AlertDialog.Builder builder;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						builder = new AlertDialog.Builder( Login.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
					} else {
						builder = new AlertDialog.Builder( Login.this);
					}
					builder.setTitle("Error!")
							.setMessage("Anonyma needs to know your city for localized content. Please allow location access.")
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setNeutralButton("OK", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialogInterface, int i) {
									dialogInterface.cancel();
								}
							})
							.show();
				}
				break;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}
	@Override
	public void onResume(){
    	super.onResume();
    	logIn.setEnabled( true);
    	signUp.setEnabled( true);
	}
	@Override
    public void onStop(){
        googleApiClient.disconnect();
        super.onStop();
    }
}