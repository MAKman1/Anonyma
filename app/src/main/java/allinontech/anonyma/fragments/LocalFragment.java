package allinontech.anonyma.fragments;


import android.arch.paging.PagedList;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import allinontech.anonyma.Anonyma;
import allinontech.anonyma.ChatWindow;
import allinontech.anonyma.HomeScreen;
import allinontech.anonyma.R;
import allinontech.anonyma.SeePostStats;
import allinontech.anonyma.UserPosts;
import allinontech.anonyma.backend.AnotherCallback;
import allinontech.anonyma.backend.MyCallback;
import allinontech.anonyma.backend.Post;
import allinontech.anonyma.backend.TheCallback;
import allinontech.anonyma.backend.Util;
import allinontech.anonyma.elements.PhotoFullPopupWindow;

public class LocalFragment extends Fragment {

    private static final int TOTAL_ITEM_EACH_LOAD = 15;

    FirebaseFirestore firestore;
    FirebaseDatabase rtDatabase;

    SwipeRefreshLayout swiperefresh;
    RecyclerView recycler;

    FirestorePagingAdapter<String, ViewHolder> adapter;
    FirestorePagingOptions<String> options;
    PagedList.Config config;
    Query baseQuery;

    TextView noPosts;

    LinearLayoutManager mLayoutManager;


    public LocalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_local, container,false);


        swiperefresh = v.findViewById(R.id.swiperefresh);
        recycler = v.findViewById( R.id.latestRecycler);
        noPosts = v.findViewById( R.id.noPosts);

        if( firestore == null){
            firestore = Util.getFirestore();
        }
        if( rtDatabase == null){
            rtDatabase = Util.getDatabase();
        }

        mLayoutManager = new LinearLayoutManager(Anonyma.getAppContext());


        refreshEverything();


        swiperefresh.setOnRefreshListener( new SwipeRefreshListener());
        swiperefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        return v;
    }

    class SwipeRefreshListener implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh(){
            swiperefresh.setRefreshing( true);
            refreshEverything();
            //fakeShit();
            swiperefresh.setRefreshing( false);

        }
    }
    private void fakeShit(){
        final CollectionReference docRef = firestore.collection("users");
        Query query = docRef;
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d( "ABBA", "" + task.getResult().size());
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if( document.exists()){
                            Log.d( "ABBA", "parent");
                            final String id = document.getId();
                            Query q = docRef.document( id).collection( "posts");
                            q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Log.d( "ABBA", "" + task.getResult().size());
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            if( document.exists()){
                                                Log.d( "ABBA", "parent");
                                                final String docId = document.getId();


                                                firestore.collection("users").document( id).collection( "posts").document( docId).update("timeStamp", FieldValue.serverTimestamp());

                                            }
                                        }
                                    }
                                }
                            });

                        }
                    }
                }
            }
        });

    }
    public void refreshEverything(){
        if( HomeScreen.userCity != null){
            baseQuery = firestore.collection( "city_name").document( HomeScreen.userCity).collection( "posts").orderBy( "timeStamp",Query.Direction.DESCENDING);

            config = new PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setPrefetchDistance(10)
                    .setPageSize(TOTAL_ITEM_EACH_LOAD)
                    .build();

            options = new FirestorePagingOptions.Builder<String>()
                    .setLifecycleOwner( getActivity())
                    .setQuery(baseQuery, config, new SnapshotParser<String>() {
                        @NonNull
                        @Override
                        public String parseSnapshot(@NonNull DocumentSnapshot snap) {
                            if( snap != null) {
                                return snap.get("post_id").toString();
                            }
                            else
                                return null;
                        }
                    })
                    .build();

            adapter = new FirestorePagingAdapter<String, ViewHolder>(options) {
                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate( R.layout.singlesecretlayout, parent, false);
                    return new ViewHolder(v);
                }

                @Override
                protected void onBindViewHolder(@NonNull final ViewHolder holder,
                                                int position,
                                                final @NonNull String postId) {
                    noPosts.setVisibility( View.GONE);

                    firestore.collection( "posts").document( postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snapshot = task.getResult();
                            if( snapshot != null && snapshot.exists()){
                                Object id = snapshot.getId();
                                Object text = snapshot.get("text_matter");
                                Object image = snapshot.get("image_link");
                                Object likes = snapshot.get("like_count");
                                Object comments = snapshot.get("comment_count");
                                Object dateTime = snapshot.get("dateTimeText");
                                Object sentByUser = snapshot.get("sentBy");
                                Object sentById = snapshot.get("sentById");

                                if (id != null && text != null && image != null && likes != null && comments != null && dateTime != null &&
                                        sentByUser != null && sentById != null) {
                                    final Post item = new Post(id.toString(), image.toString(), text.toString(), dateTime.toString(),
                                            comments.toString(), likes.toString(), sentByUser.toString(), sentById.toString());


                                    final String itemText = item.getText();

                                    if( itemText.length() < 250){
                                        holder.textView.setText( itemText);

                                        holder.textView.setVisibility( View.VISIBLE);
                                        holder.seeMore.setVisibility( View.GONE);
                                        holder.textViewLong.setVisibility( View.GONE);
                                    }
                                    else{
                                        holder.textView.setText( itemText.substring(0, 250) + "...");
                                        holder.textViewLong.setText( itemText);

                                        holder.textView.setVisibility( View.VISIBLE);
                                        holder.seeMore.setVisibility( View.VISIBLE);
                                        holder.textViewLong.setVisibility( View.GONE);

                                        holder.seeMore.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if( holder.textView.getVisibility() == View.VISIBLE){
                                                    holder.seeMore.setText( "SEE LESS");

                                                    holder.textView.setVisibility( View.GONE);
                                                    holder.seeMore.setVisibility( View.VISIBLE);
                                                    holder.textViewLong.setVisibility( View.VISIBLE);
                                                }
                                                else{
                                                    holder.seeMore.setText( "SEE MORE");

                                                    holder.textView.setVisibility( View.VISIBLE);
                                                    holder.seeMore.setVisibility( View.VISIBLE);
                                                    holder.textViewLong.setVisibility( View.GONE);
                                                }
                                            }
                                        });
                                    }


                                    holder.unameText.setText(item.getSentByUser());
                                    holder.dateView.setText(item.getDatetime());
                                    holder.commentLikeStat.setText( item.getLikes() + " likes  .  " + item.getComments() + " comments");

                                    final String url = item.getImageUrl();
                                    if( url.equals( "")){
                                        holder.secretImage.setVisibility( View.GONE);
                                    }
                                    else{
                                        holder.secretImage.setVisibility(View.VISIBLE);
                                        RequestOptions options = new RequestOptions()
                                                .centerCrop()
                                                .placeholder(R.drawable.mainlogo)
                                                .error(R.mipmap.ic_launcher_round);
                                        Glide.with( getContext()).load( url).apply(options).into( holder.secretImage);
                                        holder.secretImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                new PhotoFullPopupWindow( Anonyma.getAppContext(), R.layout.photo_popup_for_zoom, view, url, null);

                                            }
                                        });
                                    }
                                    item.isLikedByMe(new TheCallback() {
                                        @Override
                                        public void onCallback(boolean value) {
                                            if (value) {
                                                holder.likeIcon.setBackgroundResource(R.drawable.likedoneicon);
                                            } else {
                                                holder.likeIcon.setBackgroundResource(R.drawable.likeicon);
                                            }
                                        }
                                    });

                                    holder.unameText.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent( getContext(), UserPosts.class);
                                            intent.putExtra("USER_ID", item.getSentById());
                                            intent.putExtra("USER_NAME", item.getSentByUser());
                                            startActivity(intent);
                                        }
                                    });

                                    holder.likeButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            item.isLikedByMe(new TheCallback() {
                                                @Override
                                                public void onCallback(boolean value) {
                                                    if (value) {
                                                        item.setLikedByMe( false);
                                                        holder.likeIcon.setBackgroundResource(R.drawable.likeicon);
                                                        holder.commentLikeStat.setText( item.getLikes() + " likes  .  " + item.getComments() + " comments");
                                                    } else {
                                                        item.setLikedByMe( true);
                                                        holder.likeIcon.setBackgroundResource(R.drawable.likedoneicon);
                                                        holder.commentLikeStat.setText( item.getLikes() + " likes  .  " + item.getComments() + " comments");
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    holder.commentLikeStat.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            //Toast.makeText( Anonyma.getAppContext(), "Show stats", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent( getContext(), SeePostStats.class);
                                            intent.putExtra("POST_ID", item.getPostId());
                                            startActivity(intent);
                                        }
                                    });
                                    holder.commentButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent( getContext(), SeePostStats.class);
                                            intent.putExtra("POST_ID", item.getPostId());
                                            startActivity(intent);
                                        }
                                    });
                                    holder.messageButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if( isNetworkAvailable()){
                                                final String randomUserId = item.getSentById();
                                                if( randomUserId.equals( HomeScreen.UserId)){
                                                    Toast.makeText( getContext(), "This secret is by you, so please don't try talking to yourself here...", Toast.LENGTH_SHORT).show();
                                                } else{
                                                    final String randomUserName = item.getSentByUser();
                                                    rtDatabase.getReference().child( "userChats").child( HomeScreen.UserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if( dataSnapshot.exists()){
                                                                if( dataSnapshot.hasChild( randomUserId)){
                                                                    final String chatId = dataSnapshot.child( randomUserId).child( "chatId").getValue().toString();
                                                                    final Intent i = new Intent( getContext(), ChatWindow.class);
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
                                                                            final Intent i = new Intent( getContext(), ChatWindow.class);
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
                                                                        final Intent i = new Intent( getContext(), ChatWindow.class);
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
                                else {
                                    holder.itemView.setVisibility( View.GONE);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.itemView.setVisibility( View.GONE);
                        }
                    });
                }
            };
            recycler.setLayoutManager( mLayoutManager);
            recycler.setAdapter( adapter);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;

        public AppCompatTextView textViewLong;
        public Button seeMore;

        public AppCompatTextView textView;
        public AppCompatTextView unameText;
        public AppCompatTextView dateView;
        public AppCompatTextView commentLikeStat;
        public ImageView secretImage;
        public ImageView likeIcon;
        public RelativeLayout messageButton;
        public RelativeLayout likeButton;
        public RelativeLayout commentButton;

        public ViewHolder(View it) {
            super( it);

            itemView = it;
            //image = (ImageView) itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.secretText);
            unameText = itemView.findViewById(R.id.unameText);
            dateView = itemView.findViewById(R.id.dateView);
            commentLikeStat = itemView.findViewById(R.id.commentLikeStat);
            secretImage = itemView.findViewById(R.id.secretImage);
            messageButton = itemView.findViewById(R.id.messageButton);
            likeButton = itemView.findViewById(R.id.likeButton);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            commentButton = itemView.findViewById( R.id.commentButton);
            textViewLong = itemView.findViewById( R.id.secretTextLong);
            seeMore = itemView.findViewById( R.id.seeMore);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if( adapter != null){
            adapter.startListening();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if( adapter != null){
            adapter.stopListening();
        }
    }

}