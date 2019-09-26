package allinontech.anonyma;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import allinontech.anonyma.backend.MyCallback;
import allinontech.anonyma.backend.SharedPreference;
import allinontech.anonyma.backend.Util;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class Settings extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private Geocoder gcd;

    private LocationRequest mLocationRequest;

    private SharedPreference sharedPreferenceObj;

    GoogleApiClient googleApiClient;

    FirebaseFirestore firestore;

    String currentCityName;
    Location location;
    List<Address> addresses;

    Button back, aboutButton;
    Button updateLocation;
    Button changePassword;

    TextView countryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = findViewById( R.id.back);
        countryName = findViewById( R.id.countryName);
        updateLocation = findViewById( R.id.updateLocation);
        changePassword = findViewById( R.id.changePassword);
        aboutButton = findViewById( R.id.aboutButton);

        currentCityName = HomeScreen.userCity;
        if( currentCityName != null){
            countryName.setText( currentCityName.toUpperCase().trim());
        }

        sharedPreferenceObj = new SharedPreference(Settings.this);

        googleApiClient = new GoogleApiClient.Builder( Settings.this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();


        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( isNetworkAvailable()) {
                    //cityButton.setEnabled( false);
                    if (ActivityCompat.checkSelfPermission(Settings.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Settings.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Settings.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                1);
                        return;
                    } else {
                        displayLocationSettingsRequest();
                    }
                } else {
                    Toast.makeText(Settings.this, "Location cannot be retrieved (internet not available), please connect to network and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        changePassword.setOnClickListener( new PasswordChangeListener());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i  = new Intent( Settings.this, AnonymaIntro.class);
                startActivity( i);
            }
        });
    }

    public int convertToPx( int dp){
        Resources r = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
    }

    class PasswordChangeListener implements View.OnClickListener{
        @Override
        public void onClick( View v){

            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setTitle("CHANGE PASSWORD");

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params1.setMargins(convertToPx(10), convertToPx(50), convertToPx(10), convertToPx(10));

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params2.setMargins(convertToPx(10), convertToPx(10), convertToPx(10), convertToPx(50));


            final EditText pass1 = new EditText(Settings.this);
            pass1.setHint( "ENTER PASSWORD");
            pass1.setLayoutParams(params1);
            pass1.setTextAlignment( View.TEXT_ALIGNMENT_CENTER);
            pass1.setTextSize( 18);
            pass1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            final EditText pass2 = new EditText(Settings.this);
            pass2.setHint( "RE-ENTER PASSWORD");
            pass2.setLayoutParams(params2);
            pass2.setTextAlignment( View.TEXT_ALIGNMENT_CENTER);
            pass2.setTextSize( 18);
            pass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            final LinearLayout layout = new LinearLayout( Settings.this);
            layout.setOrientation( LinearLayout.VERTICAL);
            layout.addView(pass1);
            layout.addView(pass2);

            builder.setView(layout);

            // Set up the buttons
            builder.setPositiveButton("CHANGE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {
                    String password1 = pass1.getText().toString().trim();
                    String password2 = pass2.getText().toString().trim();

                    if( password1 == null || password2 == null || password1.equals("") || password2.equals("")){
                        AlertDialog.Builder a;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            a = new AlertDialog.Builder(Settings.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                        } else {
                            a = new AlertDialog.Builder(Settings.this);
                        }
                        a.setTitle("Error")
                                .setMessage("Failed to update password, Password fields cannot be empty")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                })
                                .show();
                    }
                    else if( !password1.equals( password2)){
                        AlertDialog.Builder b;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            b = new AlertDialog.Builder(Settings.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                        } else {
                            b = new AlertDialog.Builder(Settings.this);
                        }
                        b.setTitle("Error")
                                .setMessage("Passwords do not match, please try again")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                })
                                .show();
                    }
                    else{
                        if (firestore == null) {
                            firestore = Util.getFirestore();
                        }

                        firestore.collection("users").document( HomeScreen.UserId)
                                .update( "password", password1.trim())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText( Settings.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        dialog.cancel();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        AlertDialog.Builder builder;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            builder = new AlertDialog.Builder(Settings.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                                        } else {
                                            builder = new AlertDialog.Builder(Settings.this);
                                        }
                                        builder.setTitle("Error")
                                                .setMessage("Failed to update password, please try again later")
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
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
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }

    public void updateTheLocation( String countryName){
        if( countryName != null){
            HomeScreen.userCity = currentCityName.toUpperCase().trim();
            sharedPreferenceObj.setCity( currentCityName.toUpperCase().trim());


            if (firestore == null) {
                firestore = Util.getFirestore();
            }

            firestore.collection("users").document( HomeScreen.UserId)
                    .update( "current_city", currentCityName.toUpperCase().trim())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText( Settings.this, "Location update successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            AlertDialog.Builder builder;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder = new AlertDialog.Builder(Settings.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                            } else {
                                builder = new AlertDialog.Builder(Settings.this);
                            }
                            builder.setTitle("location retrieval error")
                                    .setMessage("Failed to update location, please try again")
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
                        builder = new AlertDialog.Builder( Settings.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder( Settings.this);
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
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    public void onStop(){
        googleApiClient.disconnect();
        super.onStop();
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
                            builder = new AlertDialog.Builder( Settings.this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder( Settings.this);
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
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {


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
                            getFusedLocationProviderClient(Settings.this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
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
                        getFusedLocationProviderClient( Settings.this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
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
                    if( addresses.get( 0).getCountryName() != null) {
                        currentCityName = addresses.get(0).getCountryName().toUpperCase();
                        countryName.setText(currentCityName.toUpperCase().trim());
                    }
                    else {
                        currentCityName = "DEFAULT";
                        countryName.setText(currentCityName.toUpperCase().trim());
                    }
                    updateTheLocation( currentCityName);
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
            Toast.makeText( Settings.this, "Error retrieving location, please retry again.", Toast.LENGTH_SHORT).show();
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
                                        Settings.this,
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
                            Toast.makeText( Settings.this, "Failed to retrieve location, please try again.", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
