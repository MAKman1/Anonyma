package allinontech.anonyma;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import allinontech.anonyma.backend.Util;
import allinontech.anonyma.backend.singleComment;
import allinontech.anonyma.elements.PhotoFullPopupWindow;


public class SeePostStats extends AppCompatActivity {

    Button back;
    Button addComment;

    EditText currentComment;

    RecyclerView commentsRecycler;

    ImageView secretImage;

    TextView secretText;
    TextView unameText;
    TextView dateView;
    TextView likeStats;

    FirebaseFirestore firestore;
    FirebaseDatabase rtDatabase;
    FirestoreRecyclerAdapter adapter;


    String currentPostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_post_stats);
        back = findViewById( R.id.back);
        addComment = findViewById( R.id.addComment);
        currentComment = findViewById( R.id.currentComment);
        commentsRecycler = findViewById( R.id.commentsRecycler);
        secretImage = findViewById( R.id.secretImage);
        secretText = findViewById( R.id.secretText);
        dateView = findViewById( R.id.dateView);
        unameText = findViewById( R.id.unameText);
        likeStats = findViewById( R.id.likeStats);

        currentPostId = getIntent().getStringExtra("POST_ID");

        if( firestore == null){
            firestore = Util.getFirestore();
        }
        if( rtDatabase == null){
            rtDatabase = Util.getDatabase();
        }

        populateBasics();

        commentsRecycler.setLayoutManager( new LinearLayoutManager( SeePostStats.this));
        commentsRecycler.setNestedScrollingEnabled( false);
        setupCommentsView();


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addThisComment();
            }
        });
    }
    public void addThisComment(){
        String comment = currentComment.getText().toString().trim();
        if( comment != null && !comment.equals("")){
            Map<String, Object> city = new HashMap<>();
            city.put("comment_text", comment);
            city.put("timestamp", FieldValue.serverTimestamp());
            city.put("sentByUser", HomeScreen.userName);
            city.put("sentById", HomeScreen.UserId);
            firestore.collection( "posts").document( currentPostId).collection( "comments").document()
                    .set( city)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d( "ABBA", "DocumentSnapshot successfully written!");
                            currentComment.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText( SeePostStats.this, "Failed to add comment, please try again", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            Toast.makeText( SeePostStats.this, "Please type something, we know you can do that.", Toast.LENGTH_SHORT).show();
        }
    }
    public void populateBasics(){
        if( currentPostId != null){
            firestore.collection( "posts").document( currentPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        if (snapshot.exists()) {
                            if( snapshot != null){
                                final Object text = snapshot.get( "text_matter");
                                final Object image = snapshot.get( "image_link");
                                final Object likes = snapshot.get( "like_count");
                                final Object dateTime = snapshot.get( "dateTimeText");
                                final Object sentByUser = snapshot.get( "sentBy");
                                if( text != null && image != null && likes != null && dateTime != null &&
                                        sentByUser != null){

                                    final String url = image.toString().trim();
                                    if( url.equals( "")){
                                        secretImage.setVisibility( View.GONE);
                                    }
                                    else{
                                        secretImage.setVisibility(View.VISIBLE);
                                        RequestOptions options = new RequestOptions()
                                                .centerCrop()
                                                .placeholder(R.drawable.mainlogo)
                                                .error(R.mipmap.ic_launcher_round);
                                        Glide.with( SeePostStats.this).load( url).apply(options).into( secretImage);
                                        secretImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                new PhotoFullPopupWindow( Anonyma.getAppContext(), R.layout.photo_popup_for_zoom, view, url, null);

                                            }
                                        });
                                    }

                                    secretText.setText( text.toString());
                                    unameText.setText( sentByUser.toString());
                                    likeStats.setText( likes.toString() + " likes");
                                    dateView.setText( dateTime.toString());

                                }
                            }
                            else{
                                Toast.makeText( SeePostStats.this, "Error retrieving data, please try again later", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        } else {
                            Log.d( "ABBA", "No such document");
                            Toast.makeText( SeePostStats.this, "Error retrieving data, please try again later", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    } else {
                        Log.d( "ABBA", "get failed with ", task.getException());
                        onBackPressed();
                    }
                }
            });
        }
    }
    public void setupCommentsView(){
        Query query = firestore
                .collection("posts")
                .document( currentPostId)
                .collection( "comments")
                .orderBy("timestamp");
        FirestoreRecyclerOptions<singleComment> options = new FirestoreRecyclerOptions.Builder<singleComment>()
                .setQuery(query, new SnapshotParser<singleComment>() {
                    @NonNull
                    @Override
                    public singleComment parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        String text = snapshot.get( "comment_text").toString();
                        String dateTime;
                        try{
                            dateTime = snapshot.get( "timestamp").toString();
                        }
                        catch ( Exception e) {
                            dateTime = Calendar.getInstance().getTime().toString();
                        }
                        String sentByUser = snapshot.get( "sentByUser").toString();
                        String sentById = snapshot.get( "sentById").toString();

                        return new singleComment( text, dateTime, sentById, sentByUser);
                    }
                })
                .build();
        adapter = new FirestoreRecyclerAdapter<singleComment, ViewHolder>(options) {
            @Override
            public void onBindViewHolder(ViewHolder holder, int position, final singleComment model) {
                if( model != null){
                    holder.userName.setText( model.getSentByUser().trim());
                    holder.theComment.setText( model.getText().trim());
                    holder.userName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent( SeePostStats.this, UserPosts.class);
                            intent.putExtra("USER_ID", model.getSentById());
                            intent.putExtra("USER_NAME", model.getSentByUser());
                            startActivity(intent);
                        }
                    });
                    holder.messageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if( isNetworkAvailable()){
                                final String randomUserId = model.getSentById();
                                if( randomUserId.equals( HomeScreen.UserId)){
                                    Toast.makeText( SeePostStats.this, "This secret is by you, so please don't try talking to yourself here...", Toast.LENGTH_SHORT).show();
                                } else{
                                    final String randomUserName = model.getSentByUser();
                                    rtDatabase.getReference().child( "userChats").child( HomeScreen.UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if( dataSnapshot.exists()){
                                                if( dataSnapshot.hasChild( randomUserId)){
                                                    final String chatId = dataSnapshot.child( randomUserId).child( "chatId").getValue().toString();
                                                    final Intent i = new Intent( SeePostStats.this, ChatWindow.class);
                                                    i.putExtra("chatId", chatId);
                                                    i.putExtra("senderName", randomUserName);
                                                    i.putExtra("recipientId", randomUserId);
                                                    startActivity(i);
                                                }
                                                else{
                                                    final DatabaseReference temp = rtDatabase.getReference().child("chats").push();
                                                    temp.child( "token").setValue("temp", new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                            final String chatId = temp.getKey().trim();
                                                            final Intent i = new Intent( SeePostStats.this, ChatWindow.class);
                                                            i.putExtra("chatId", chatId);
                                                            i.putExtra("senderName", randomUserName);
                                                            i.putExtra("recipientId", randomUserId);
                                                            startActivity(i);
                                                        }
                                                    });
                                                }
                                            }
                                            else{
                                                final DatabaseReference temp = rtDatabase.getReference().child("chats").push();
                                                temp.child( "token").setValue("temp", new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                        final String chatId = temp.getKey().trim();
                                                        final Intent i = new Intent( SeePostStats.this, ChatWindow.class);
                                                        i.putExtra("chatId", chatId);
                                                        i.putExtra("senderName", randomUserName);
                                                        i.putExtra("recipientId", randomUserId);
                                                        startActivity(i);
                                                    }
                                                });
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup group, int i) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.single_comment, group, false);

                return new ViewHolder(view);
            }
        };
        commentsRecycler.setAdapter( adapter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onPause(){
        super.onPause();
        if( adapter != null){
            adapter.stopListening();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if( adapter != null){
            adapter.startListening();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userName;
        public TextView theComment;
        public Button messageButton;

        public ViewHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById( R.id.userName);
            theComment = itemView.findViewById( R.id.theComment);
            messageButton = itemView.findViewById( R.id.messageButton);
        }
    }
}

