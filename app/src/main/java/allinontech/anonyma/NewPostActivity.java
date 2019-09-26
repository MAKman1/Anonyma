package allinontech.anonyma;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ThrowOnExtraProperties;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import allinontech.anonyma.backend.Util;

public class NewPostActivity extends AppCompatActivity {
    private static final String[] MONTH_NAMES= {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT","NOV", "DEC"};
    private static final int MY_CAMERA_PERMISSION_CODE = 22;
    private static final int CAMERA_REQUEST = 2;
    private static final int READ_GALLERY_CODE = 55;
    private static final int RESULT_LOAD_IMG = 5;
    private static final int ADD_TAG_RESULT = 101;

    Button backTop;
    Button shareButton;
    Button captureImage;
    Button galleryImage;
    Button addTags;

    ProgressBar uploadProgressBar;

    RecyclerView theTags;
    FlexRecyclerAdapter theAdapter;

    ImageView imageSecretView;

    LinearLayout addImageView;

    ProgressDialog dialog;

    TextView deleteImage;

    EditText postText;

    FirebaseFirestore firebase;
    FirebaseStorage firebaseStorage;

    ArrayList<String> currentTagsFinal;

    private Bitmap secretImageBitmap;

    String mCurrentPhotoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        backTop = findViewById( R.id.back);
        shareButton = findViewById( R.id.shareButton);
        postText = findViewById( R.id.postText);
        captureImage = findViewById( R.id.captureImage);
        imageSecretView = findViewById( R.id.imageSecretView);
        addImageView = findViewById( R.id.addImageView);
        deleteImage = findViewById( R.id.deleteImage);
        addTags = findViewById( R.id.addTags);
        galleryImage = findViewById( R.id.galleryImage);
        uploadProgressBar = findViewById( R.id.uploadProgressBar);
        theTags = findViewById( R.id.theTags);

        if( firebase == null){
            firebase = Util.getFirestore();
        }
        if( firebaseStorage == null){
            firebaseStorage = FirebaseStorage.getInstance();
        }

        currentTagsFinal = new ArrayList<>();



        FlexboxLayoutManager flexCurrentLayoutManager = new FlexboxLayoutManager( NewPostActivity.this);
        flexCurrentLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexCurrentLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexCurrentLayoutManager.setJustifyContent(JustifyContent.CENTER);
        theTags.setLayoutManager( flexCurrentLayoutManager);
        theAdapter = new FlexRecyclerAdapter();
        theTags.setAdapter( theAdapter);
        populateCurrentTagsView();

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                secretImageBitmap = null;
                addImageView.setVisibility( View.VISIBLE);
                imageSecretView.setVisibility( View.GONE);
                imageSecretView.setImageBitmap( secretImageBitmap);
                deleteImage.setVisibility( View.GONE);
            }
        });
        addTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(NewPostActivity.this, AddTagsToPost.class);
                i.putStringArrayListExtra( "currentTagsFinal", currentTagsFinal);
                startActivityForResult(i, ADD_TAG_RESULT);
            }
        });
        galleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGalleryClick();
            }
        });
        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCaptureClick();
            }
        });
        shareButton.setOnClickListener( new ShareContentListener());
        backTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText( NewPostActivity.this, "Error while capturing image, please try again", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "allinontech.anonyma.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }
    public void onCaptureClick(){
        if (ActivityCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewPostActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_PERMISSION_CODE);
            return;
        }else{
            dispatchTakePictureIntent();
        }
    }
    public void onGalleryClick(){
        if (ActivityCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewPostActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_GALLERY_CODE);
            return;
        }else{
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        }
    }
    public class ShareContentListener implements View.OnClickListener{
        public void onClick( View v){
            if( isNetworkAvailable()){
                try{
                    dialog = ProgressDialog.show( NewPostActivity.this, "UPLOADING",
                            "Sharing your secret...", true);


                    shareButton.setEnabled( false);
                    if( secretImageBitmap != null){
                        uploadProgressBar.setVisibility( View.VISIBLE);
                        final StorageReference storageRef = firebaseStorage.getReference().child( "secretImagesData").child( HomeScreen.UserId + Calendar.getInstance().toString().hashCode());
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        secretImageBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = storageRef.putBytes(data);
                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                // Continue with the task to get the download URL
                                return storageRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    String postData = postText.getText().toString();
                                    String dateTime = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " " + MONTH_NAMES[ Calendar.getInstance().get(Calendar.MONTH)];
                                    String tags = "/";
                                    for( String temp: currentTagsFinal){
                                        tags = tags + temp + "/";
                                    }
                                    //newPost.put( "tags", tags);

                                    Map<String, Object> newPost = new HashMap<>();
                                    newPost.put("dateTimeText", dateTime);
                                    newPost.put("timeStamp", FieldValue.serverTimestamp());
                                    newPost.put("image_link", downloadUri.toString());
                                    newPost.put("text_matter", postData);
                                    newPost.put( "comment_count", 0);
                                    newPost.put( "like_count", 0);
                                    newPost.put( "sentBy", HomeScreen.userName);
                                    newPost.put( "sentById", HomeScreen.UserId);
                                    newPost.put( "tags", tags);


                                    firebase.collection( "posts").add( newPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            String id = documentReference.getId();
                                            final Map<String, Object> addPost = new HashMap<>();
                                            addPost.put("post_id", id);
                                            addPost.put("timeStamp", FieldValue.serverTimestamp());
                                            firebase.collection( "users").document( HomeScreen.UserId).collection( "posts").add( addPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    String currentCity = HomeScreen.userCity;
                                                    if( currentCity != null){
                                                        firebase.collection( "city_name").document( currentCity).collection( "posts").add( addPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                            @Override
                                                            public void onSuccess(DocumentReference documentReference) {
                                                                if( currentTagsFinal.size() > 0){
                                                                    for( int i = 0; i < currentTagsFinal.size(); i++){
                                                                        final int a = i;
                                                                        firebase.collection( "tags").document( currentTagsFinal.get( i)).collection( "posts").add( addPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                if( a >= currentTagsFinal.size() - 1)
                                                                                    finish();
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.w( "DADDY", "Error adding document", e);
                                                                                shareButton.setEnabled( true);
                                                                                if( dialog != null)
                                                                                    dialog.cancel();
                                                                                Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                                else{
                                                                    finish();
                                                                }
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w( "DADDY", "Error adding document", e);
                                                                shareButton.setEnabled( true);
                                                                if( dialog != null)
                                                                    dialog.cancel();
                                                                Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                    else{
                                                        Toast.makeText( Anonyma.getAppContext(), "City not found", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w( "DADDY", "Error adding document", e);
                                                    shareButton.setEnabled( true);
                                                    if( dialog != null)
                                                        dialog.cancel();
                                                    Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            uploadProgressBar.setVisibility( View.GONE);
                                            Log.w( "DADDY", "Error adding document", e);
                                            shareButton.setEnabled( true);
                                            if( dialog != null)
                                                dialog.cancel();
                                            Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    uploadProgressBar.setVisibility( View.GONE);
                                    shareButton.setEnabled( true);
                                    if( dialog != null)
                                        dialog.cancel();
                                    Toast.makeText( NewPostActivity.this, "Failed to share secret, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                int currentprogress = (int) progress;
                                uploadProgressBar.setProgress(currentprogress);
                            }
                        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText( NewPostActivity.this, "Upload paused, please wait for it to resume", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        String postData = postText.getText().toString();
                        if( postData != null && postData.length() > 0){
                            String dateTime = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " " + MONTH_NAMES[ Calendar.getInstance().get(Calendar.MONTH)];
                            //String dateTime = new Timestamp(System.currentTimeMillis())
                            String tags = "/";
                            for( String temp: currentTagsFinal){
                                tags = tags + temp + "/";
                            }
                            //newPost.put( "tags", tags);


                            Map<String, Object> newPost = new HashMap<>();
                            newPost.put("dateTimeText", dateTime);
                            newPost.put("timeStamp", FieldValue.serverTimestamp());
                            newPost.put("image_link", "");
                            newPost.put("text_matter", postData);
                            newPost.put( "comment_count", 0);
                            newPost.put( "like_count", 0);
                            newPost.put( "sentBy", HomeScreen.userName);
                            newPost.put( "sentById", HomeScreen.UserId);
                            newPost.put( "tags", tags);


                            firebase.collection( "posts").add( newPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    String id = documentReference.getId();

                                    final Map<String, Object> addPost = new HashMap<>();
                                    addPost.put("post_id", id);
                                    addPost.put("timeStamp", FieldValue.serverTimestamp());
                                    firebase.collection( "users").document( HomeScreen.UserId).collection( "posts").add( addPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            String currentCity = HomeScreen.userCity;
                                            if( currentCity != null){
                                                firebase.collection( "city_name").document( currentCity).collection( "posts").add( addPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        if( currentTagsFinal.size() > 0){
                                                            for( int i = 0; i < currentTagsFinal.size(); i++){
                                                                final int a = i;
                                                                firebase.collection( "tags").document( currentTagsFinal.get( i)).collection( "posts").add( addPost).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        if( a >= currentTagsFinal.size() - 1)
                                                                            finish();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w( "DADDY", "Error adding document", e);
                                                                        shareButton.setEnabled( true);
                                                                        if( dialog != null)
                                                                            dialog.cancel();
                                                                        Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                        else{
                                                            finish();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w( "DADDY", "Error adding document", e);
                                                        Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                            else{
                                                Toast.makeText( Anonyma.getAppContext(), "City not found", Toast.LENGTH_SHORT).show();
                                                shareButton.setEnabled( true);
                                                if( dialog != null)
                                                    dialog.cancel();
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w( "DADDY", "Error adding document", e);
                                            shareButton.setEnabled( true);
                                            if( dialog != null)
                                                dialog.cancel();
                                            Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w( "DADDY", "Error adding document", e);
                                    shareButton.setEnabled( true);
                                    if( dialog != null)
                                        dialog.cancel();

                                    Toast.makeText( NewPostActivity.this, "Error sharing your secret, please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            shareButton.setEnabled( true);
                            if( dialog != null)
                                dialog.cancel();
                            Toast.makeText( NewPostActivity.this, "Please type in a secret or upload an image", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch ( Exception e){
                    shareButton.setEnabled( true);
                    if( dialog != null)
                        dialog.cancel();
                    Toast.makeText( NewPostActivity.this, "Failed to share secret, please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                shareButton.setEnabled( true);
                if( dialog != null)
                    dialog.cancel();
                Toast.makeText( NewPostActivity.this, "Internet unavailable, please connect to the internet and try again", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Failed to access camera. Please grant permission and try again.", Toast.LENGTH_SHORT).show();
            }

        }
        if (requestCode == READ_GALLERY_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            } else {
                Toast.makeText(this, "Failed to access gallery. Please grant permission and try again.", Toast.LENGTH_SHORT).show();
            }

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == ADD_TAG_RESULT && resultCode == Activity.RESULT_OK){
            try{
                Bundle extras = data.getExtras();
                if( extras != null){
                    currentTagsFinal = extras.getStringArrayList( "returnedTagList");
                    populateCurrentTagsView();
                }
            } catch( Exception e){
                currentTagsFinal = new ArrayList<>();
                Toast.makeText( NewPostActivity.this, "Error getting tag data, Please try again.", Toast.LENGTH_SHORT).show();
            }

        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            if( mCurrentPhotoPath != null){
                secretImageBitmap = BitmapFactory.decodeFile( mCurrentPhotoPath);

                imageSecretView.setVisibility(View.VISIBLE);
                imageSecretView.setImageBitmap( secretImageBitmap);
                addImageView.setVisibility( View.GONE);
                deleteImage.setVisibility( View.VISIBLE);
            }
            //secretImageBitmap = (Bitmap) data.getExtras().get("data");
        }
        if( requestCode == RESULT_LOAD_IMG){
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    secretImageBitmap = BitmapFactory.decodeStream(imageStream);


                    imageSecretView.setVisibility(View.VISIBLE);
                    imageSecretView.setImageBitmap( secretImageBitmap);
                    addImageView.setVisibility( View.GONE);
                    deleteImage.setVisibility( View.VISIBLE);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText( NewPostActivity.this, "Error loading image, Please try again.", Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(NewPostActivity.this, "No image selected, kindly try again.",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void populateCurrentTagsView(){
        if(currentTagsFinal.size() != 0){
            theTags.setVisibility( View.VISIBLE);
        }
        else{
            theTags.setVisibility( View.GONE);
        }
        theAdapter.notifyDataSetChanged();
        theTags.invalidate();
    }
    private class FlexRecyclerAdapter extends RecyclerView.Adapter<FlexRecyclerAdapter.ViewHolder> {
        private LayoutInflater mInflater;

        // data is passed into the constructor
        FlexRecyclerAdapter() {
            this.mInflater = LayoutInflater.from( NewPostActivity.this);
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            view = mInflater.inflate(R.layout.single_tag_currentfinal, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.setName( currentTagsFinal.get( position));
            holder.deleteTag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //currentTagsAdapter.notifyItemRemoved( position);
                    currentTagsFinal.remove( position);
                    //currentTagsAdapter.notifyDataSetChanged();
                    populateCurrentTagsView();
                }
            });
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return currentTagsFinal.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tagName;
            Button deleteTag;

            ViewHolder(View itemView) {
                super( itemView);
                tagName = itemView.findViewById( R.id.tagName);
                deleteTag = itemView.findViewById( R.id.deleteTag);
            }

            public void setName( String tgname){
                tagName.setText( tgname.trim());
            }
        }
    }
}
