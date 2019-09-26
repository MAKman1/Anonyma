package allinontech.anonyma;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import allinontech.anonyma.backend.ChatCover;
import allinontech.anonyma.backend.MyCallback;
import allinontech.anonyma.backend.User;
import allinontech.anonyma.backend.Util;
import allinontech.anonyma.elements.OnSwipeTouchListener;

public class ChatActivity extends AppCompatActivity {
    String currentUserId;

    RelativeLayout chatMainLayout;

    TextView test;
    TextView noChats;

    Button back;
    Button randomChat;

    ProgressBar progressBar;

    RecyclerView allChatsRecycler;

    FirebaseDatabase rtDatabase;

    ArrayList<ChatCover> allChats;

    int errors;

    FirebaseRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatMainLayout = findViewById( R.id.chatMainLayout);
        allChatsRecycler = findViewById( R.id.allChatsRecycler);
        progressBar = findViewById( R.id.progressBar);
        back = findViewById( R.id.back);
        test = findViewById( R.id.test);
        noChats = findViewById( R.id.noChats);
        randomChat = findViewById( R.id.randomChat);


        currentUserId = HomeScreen.UserId;

        rtDatabase = Util.getDatabase();

        allChats = new ArrayList<>();




        //fakeChat();
        //getAllChats();
        populateRecycler();



        randomChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				randomChat.setEnabled( false);
            	openRandomChat();
            }
        });

        chatMainLayout.setOnTouchListener( new OnSwipeTouchListener(ChatActivity.this) {
            public void onSwipeRight() {
            	Log.d( "DADDY", "Left");
                openActivityFromLeft();
                onBackPressed();
            }
        });
        allChatsRecycler.setOnTouchListener( new OnSwipeTouchListener(ChatActivity.this) {
			public void onSwipeRight() {
				Log.d( "DADDY", "Left");
                openActivityFromLeft();
				onBackPressed();
			}
		});
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    protected void openActivityFromLeft() {
        //overridePendingTransition( 0, 0);
        overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
    }
    public void openRandomChat(){
    	try{
			if( isNetworkAvailable()){
				rtDatabase.getReference().child( "users").addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						int count = Math.abs((int) dataSnapshot.getChildrenCount());
						Log.d( "DADDY", " count: " + count);
						int index = (int) (Math.random() * count);
						Log.d( "DADDY", " index: " + index);
						int currentIndex = 0;
						for( DataSnapshot randomUser: dataSnapshot.getChildren()){
							if( currentIndex == index){
								final String randomUserId = randomUser.getKey().trim();
								if( randomUserId.equals( currentUserId)){
									randomChat.setEnabled( true);
									Toast.makeText( ChatActivity.this, "Error retrieving user data, please retry", Toast.LENGTH_SHORT).show();
								} else{
									final String randomUserName = randomUser.child( "u_name").getValue().toString();
									rtDatabase.getReference().child( "userChats").child( currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
										@Override
										public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
											if( dataSnapshot.exists()){
												if( dataSnapshot.hasChild( randomUserId)){
													final String chatId = dataSnapshot.child( randomUserId).child( "chatId").getValue().toString();
													final Intent i = new Intent(ChatActivity.this, ChatWindow.class);
													i.putExtra("chatId", chatId);
													i.putExtra("senderName", randomUserName);
													i.putExtra("recipientId", randomUserId);
													overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
													randomChat.setEnabled( true);
													startActivity(i);
												}
												else{
													final DatabaseReference temp = rtDatabase.getReference().child("chats").push();
													temp.child( "token").setValue("temp", new DatabaseReference.CompletionListener() {
														@Override
														public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
															final String chatId = temp.getKey().trim();
															final Intent i = new Intent(ChatActivity.this, ChatWindow.class);
															i.putExtra("chatId", chatId);
															i.putExtra("senderName", randomUserName);
															i.putExtra("recipientId", randomUserId);
															overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
															randomChat.setEnabled( true);
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
														final Intent i = new Intent(ChatActivity.this, ChatWindow.class);
														i.putExtra("chatId", chatId);
														i.putExtra("senderName", randomUserName);
														i.putExtra("recipientId", randomUserId);
														overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
														randomChat.setEnabled( true);
														startActivity(i);
													}
												});
											}
										}
										@Override
										public void onCancelled(@NonNull DatabaseError databaseError) {
											randomChat.setEnabled( true);
											Log.d( "DADDY", "yok");
										}
									});
									break;
								}
							}
							currentIndex++;
						}

					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						randomChat.setEnabled( true);
					}
				});
			}
			else{
				randomChat.setEnabled( true);
				Toast.makeText( ChatActivity.this, "Cannot load chat( internet unavailable)", Toast.LENGTH_SHORT).show();
			}
		} catch( Exception e){
			randomChat.setEnabled( true);
    		Log.d( "DADDY", e.toString());
		}
	}

    public void populateRecycler(){
        Query query = rtDatabase
                .getReference()
                .child("userChats/" + currentUserId)
                .orderByKey()
                .limitToLast( 50);
        Log.d( "ABBA", "puhanche");


        FirebaseRecyclerOptions<ChatCover> options =
                new FirebaseRecyclerOptions.Builder<ChatCover>()
                        .setQuery(query, new SnapshotParser<ChatCover>() {
                            @NonNull
                            @Override
                            public ChatCover parseSnapshot( DataSnapshot snapshot) {
                                if( snapshot != null && snapshot.child("chatId").getValue() != null &&
                                        snapshot.child("message").getValue() != null &&
                                        snapshot.child("oppositeId").getValue() != null &&
                                        snapshot.child("timeStamp").getValue() != null){
                                    try{
                                        String chatId = snapshot.child("chatId").getValue().toString();
                                        String message = snapshot.child("message").getValue().toString();
                                        String oppositeName = snapshot.child("oppositeId").getValue().toString();
                                        String timeStamp = snapshot.child("timeStamp").getValue().toString();

                                        ChatCover temp = new ChatCover( oppositeName, message, timeStamp, chatId);
                                        //ChatCover temp = new ChatCover( "arham", "hi", "oct", "500");
                                        return temp;
                                    }catch ( Exception e){
                                        Log.d( "ABBA", "Error! "+ e.toString());
                                        return new ChatCover();
                                    }
                                }
                                else {
                                    Log.d("ABBA", "lag gaye");
                                    if( adapter.getItemCount() <= 0){
                                        noChats.setVisibility( View.VISIBLE);
                                    }
                                    return new ChatCover();
                                }
                            }
                        })
                        .build();
        adapter = new FirebaseRecyclerAdapter<ChatCover, ItemViewHolder>(options) {
            @Override
            public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Log.d( "ABBA", "nahin puhanche abhi");
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.singlechat, parent, false);
                return new ItemViewHolder(view);
            }
            @Override
            public void onDataChanged(){
                if( adapter.getItemCount() > 0){
                    noChats.setVisibility( View.GONE);
                }
                else{
					noChats.setVisibility( View.VISIBLE);
				}
            }

            @Override
            protected void onBindViewHolder(final ItemViewHolder holder, int position, final ChatCover model) {
                if( model.getChatId() != null){
                    holder.setData( model.getSenderName(), model.getLastMessage(), model.getTimeOfChat());
                    holder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        	holder.mView.setEnabled( false);
                            if( isNetworkAvailable()){
                                final Intent i = new Intent(ChatActivity.this, ChatWindow.class);
                                i.putExtra("chatId", model.getChatId());
                                i.putExtra("recipientId", model.getSenderName());
                                overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                                User.getUserName( model.getSenderName(), new MyCallback() {
                                    @Override
                                    public void onCallback(String value) {
                                        i.putExtra("senderName", value);
										holder.mView.setEnabled( true);
                                        startActivity(i);
                                    }
                                });
                            }
                            else{
								holder.mView.setEnabled( true);
                                Toast.makeText( ChatActivity.this, "Cannot load chat( internet unavailable)", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    this.notifyItemRemoved( position);
                }

            }
        };
        LinearLayoutManager layoutManager = new LinearLayoutManager( ChatActivity.this);
        layoutManager.setReverseLayout(true);
        allChatsRecycler.setLayoutManager( layoutManager);
        allChatsRecycler.setAdapter( adapter);
        adapter.startListening();
        progressBar.setVisibility( View.GONE);
        if( adapter.getItemCount() <= 0){
            noChats.setVisibility( View.VISIBLE);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView name;
        TextView chat;
        TextView lastTime;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            name = mView.findViewById( R.id.userName);
            chat = mView.findViewById( R.id.lastChat);
            lastTime = mView.findViewById( R.id.lastTime);
        }

        public void setData( String t_name, String t_chat, String t_time){
        	User.getUserName(t_name, new MyCallback() {
				@Override
				public void onCallback(String value) {
					name.setText( value);
				}
			});
            chat.setText( t_chat);
            lastTime.setText( t_time);
        }
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
        adapter.stopListening();
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onResume(){
        super.onResume();
        populateRecycler();
    }
}
