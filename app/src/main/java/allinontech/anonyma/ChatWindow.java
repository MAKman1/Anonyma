package allinontech.anonyma;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ThrowOnExtraProperties;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import allinontech.anonyma.backend.Chat;
import allinontech.anonyma.backend.Util;

public class ChatWindow extends AppCompatActivity {
    private static final String[] MONTH_NAMES= {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT","NOV", "DEC"};

    Button back;
    Button send;


    RelativeLayout chatMainLayout;

    TextView chatTitle;

    EditText currentMessage;

    FirebaseDatabase rtDatabase;
    FirebaseRecyclerAdapter adapter;

    RecyclerView chatRecyclerView;
    LinearLayoutManager mLayoutManager;

    String senderName;
    String chatId;
    String RecipientUid;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        back = findViewById( R.id.back);
        send = findViewById( R.id.sendMessage);
        chatTitle = findViewById( R.id.chatTitle);
        currentMessage = findViewById( R.id.currentMessage);
        chatMainLayout = findViewById( R.id.chatMainLayout);
        chatRecyclerView = findViewById( R.id.chatRecyclerView);

        Intent intent = getIntent();
        senderName = intent.getExtras().getString("senderName");
        chatId = intent.getExtras().getString("chatId");
        RecipientUid = intent.getExtras().getString("recipientId");

        currentUserId = HomeScreen.UserId;

        chatTitle.setText( senderName.toUpperCase());

        rtDatabase = Util.getDatabase();


        mLayoutManager = new LinearLayoutManager( ChatWindow.this);
        mLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager( mLayoutManager);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        send.setOnClickListener( new sendListener());
        currentMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                //refreshScroll();
            }
        });
    }

    public void refreshScroll(){
        chatRecyclerView.smoothScrollToPosition( adapter.getItemCount() - 1);
        Log.d( "ABBA", "Refreshed");

    }
    private class sendListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String message = currentMessage.getText().toString().trim();
            currentMessage.setText("");
            if( message != null && !message.equals("")){
                sendChat( message);
            }
            adapter.notifyDataSetChanged();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView chat;
        TextView lastTime;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            chat = mView.findViewById( R.id.chat_message);
            lastTime = mView.findViewById( R.id.chat_time);
        }

        public void setData( String t_chat, String t_time){
            chat.setText( t_chat);
            lastTime.setText( t_time);
        }
    }

    public void sendChat( String message){
        final DatabaseReference newChat;
        if( message != null){
            try{
            	rtDatabase.getReference().child( "chats").child( chatId).child( "token").removeValue();

                newChat = rtDatabase.getReference().child("chats").child( chatId).push();

                Map<String, String> chatData = new HashMap<String, String>();
                String currentTime =  Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + " " + MONTH_NAMES[ Calendar.getInstance().get(Calendar.MONTH)];

                chatData.put("timeStamp", currentTime);
                chatData.put("message", "" + message);
                chatData.put("senderId", currentUserId);
                chatData.put("recipientId", RecipientUid);
                newChat.setValue( chatData);


                final DatabaseReference sender = rtDatabase.getReference().child("userChats").child( currentUserId);
                final DatabaseReference receiver = rtDatabase.getReference().child("userChats").child( RecipientUid);


                Map<String, String> senderData = new HashMap<String, String>();

                senderData.put("timeStamp", currentTime);
                senderData.put("message", "" + message);
                senderData.put("oppositeId", RecipientUid);
                senderData.put("notificationId", RecipientUid);
                senderData.put("chatId", chatId);

                sender.child( RecipientUid).setValue(senderData);

                Map<String, String> receiverData = new HashMap<String, String>();
                receiverData.put("timeStamp", currentTime);
                receiverData.put("message", "" + message);
                receiverData.put("notificationId", RecipientUid);
                receiverData.put("oppositeId", HomeScreen.UserId);

                receiverData.put("chatId", chatId);

                receiver.child( currentUserId).setValue( receiverData);
            } catch ( Exception e){
                Toast.makeText( ChatWindow.this, "Chat failed to send", Toast.LENGTH_SHORT).show();
            }

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    @Override
    public void onPause(){
        super.onPause();
        adapter.stopListening();
    }
    @Override
    public void onResume(){
        super.onResume();
        new populateRecyclerView().execute( "");
    }
    @Override
    public void onStart(){
        super.onStart();
        new populateRecyclerView().execute( "");
    }


    private class populateRecyclerView extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try{
                Query query = rtDatabase
                        .getReference()
                        .child("chats/" + chatId)
                        .limitToLast( 30);

                FirebaseRecyclerOptions<Chat> options =
                        new FirebaseRecyclerOptions.Builder<Chat>()
                                .setQuery(query, new SnapshotParser<Chat>() {
                                    @Override
                                    public Chat parseSnapshot(@NonNull DataSnapshot snapshot) {
                                        if( snapshot != null && snapshot.child("message").getValue() != null &&
                                                snapshot.child("timeStamp").getValue() != null &&
                                                snapshot.child("senderId").getValue() != null){

                                            String message = snapshot.child("message").getValue().toString();
                                            String senderId = snapshot.child("senderId").getValue().toString();
                                            String timeStamp = snapshot.child("timeStamp").getValue().toString();
                                            boolean me;
                                            if( senderId.equals( currentUserId))
                                                me = true;
                                            else
                                                me = false;

                                            Chat temp = new Chat( me, message, timeStamp);
                                            return temp;
                                        }
                                        else
                                            return new Chat( true, "temp", ".");
                                    }
                                })
                                .build();
                adapter = new FirebaseRecyclerAdapter<Chat, ItemViewHolder>(options) {
                    @Override
                    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        // Create a new instance of the ViewHolder, in this case we are using a custom
                        // layout called R.layout.message for each item
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate( viewType, parent, false);
                        return new ItemViewHolder(view);
                    }
                    @Override
                    public int getItemViewType(int position) {
                    	try{
							Chat temp = getItem( position);
							if( temp.isMe() == true)
								return R.layout.singlechatsent;
							else
								return R.layout.singlechatreceived;
						} catch ( Exception e){
                    		return R.layout.singlechatsent;
						}
                    }

                    @Override
                    protected void onBindViewHolder(ItemViewHolder holder, int position, final Chat model) {
                    	if( model.getTime().equals( ".")){
                    		holder.mView.setVisibility( View.GONE);
						}
						else{
                            holder.mView.setVisibility( View.VISIBLE);
							holder.setData(  model.getChat(), model.getTime());
						}
                    }
                    @Override
                    public void onDataChanged(){
                        if( adapter.getItemCount() >= 1){
                            refreshScroll();
                        }
                    }
                };

                chatRecyclerView.setAdapter( adapter);
                adapter.startListening();
            } catch( Exception e){
                Log.d( "DADDY", e.toString());
            }
			return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
