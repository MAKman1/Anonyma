package allinontech.anonyma;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import allinontech.anonyma.backend.Chat;
import allinontech.anonyma.backend.TagSearchResult;
import allinontech.anonyma.backend.Util;

public class AddTagsToPost extends AppCompatActivity {

    RecyclerView currentTagsView;
    RecyclerView recentTagsView;
    RecyclerView searchedTagsView;
    TextView recentHeading;

    EditText searchInput;

    Button back;
    Button doneButton;

    FlexboxLayoutManager flexCurrentLayoutManager;
    FlexboxLayoutManager flexRecentLayoutManager;

    FlexRecyclerAdapter currentTagsAdapter;
    FlexRecyclerAdapter recentTagsAdapter;

    SearchResultAdapter searchResultAdapter;

    ArrayList<String> currentTags;
    ArrayList<String> recentTags;

    FirebaseDatabase rtDatabase;

    RefreshSearch rf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tags_to_post);

        currentTagsView = findViewById( R.id.currentTags);
        recentTagsView = findViewById( R.id.recentTags);
        searchedTagsView = findViewById( R.id.searchedTags);
        recentHeading  = findViewById( R.id.recentHeading);
        searchInput = findViewById( R.id.searchInput);
        back = findViewById( R.id.back);
        doneButton = findViewById( R.id.doneButton);

        try{
            Intent i = getIntent();
            currentTags = i.getStringArrayListExtra("currentTagsFinal");
        } catch( Exception e){
            currentTags = new ArrayList<>();
        }
        if( rtDatabase == null)
            rtDatabase = Util.getDatabase();

        recentTags = new ArrayList<>();


        flexCurrentLayoutManager = new FlexboxLayoutManager( AddTagsToPost.this);
        flexCurrentLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexCurrentLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexCurrentLayoutManager.setJustifyContent(JustifyContent.CENTER);
        flexRecentLayoutManager = new FlexboxLayoutManager( AddTagsToPost.this);
        flexRecentLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexRecentLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexRecentLayoutManager.setJustifyContent(JustifyContent.CENTER);

        currentTagsAdapter = new FlexRecyclerAdapter( true);
        recentTagsAdapter = new FlexRecyclerAdapter( false);
        searchedTagsView.setLayoutManager( new LinearLayoutManager( AddTagsToPost.this));

        currentTagsView.setLayoutManager( flexCurrentLayoutManager);
        currentTagsView.setAdapter( currentTagsAdapter);
        recentTagsView.setLayoutManager( flexRecentLayoutManager);
        recentTagsView.setAdapter( recentTagsAdapter);

        populateRecentTags();
        populateCurrentTagsView();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if( input.length() > 0){
                    searchedTagsView.setVisibility( View.VISIBLE);
                    recentTagsView.setVisibility( View.GONE);
                    recentHeading.setVisibility( View.GONE);
                    if( rf == null){
                        rf = new RefreshSearch();
                        rf.execute( input);
                    }
                    else{
                        rf.cancel( true);
                        rf = new RefreshSearch();
                        rf.execute( input);
                    }
                }
                else {
                    searchedTagsView.setVisibility(View.GONE);
                    recentTagsView.setVisibility( View.VISIBLE);
                    recentHeading.setVisibility( View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    void tempFake(){
        rtDatabase.getReference( "tags").child("arham").setValue( "0");
        rtDatabase.getReference( "tags").child("arkhan").setValue( "1");
        rtDatabase.getReference( "tags").child("makman").setValue( "3");
        rtDatabase.getReference( "tags").child("makintosh").setValue( "5");
        rtDatabase.getReference( "tags").child("abc").setValue( "7");
        rtDatabase.getReference( "tags").child("abcd").setValue( "110");
        rtDatabase.getReference( "tags").child("abcdef").setValue( "440");
    }
    void populateCurrentTagsView(){
        if(currentTags.size() != 0){
            currentTagsView.setVisibility( View.VISIBLE);
        }
        else{
            currentTagsView.setVisibility( View.GONE);
        }
        currentTagsAdapter.notifyDataSetChanged();
        currentTagsView.invalidate();
    }
    void populateRecentTags(){
        //function


        if( recentTags.size() != 0){
            recentHeading.setVisibility( View.VISIBLE);
            recentTagsView.setVisibility( View.VISIBLE);
        }
        else{
            recentHeading.setVisibility( View.GONE);
            recentTagsView.setVisibility( View.GONE);
        }
        recentTagsAdapter.notifyDataSetChanged();
        recentTagsView.invalidate();
    }
    private class FlexRecyclerAdapter extends RecyclerView.Adapter<FlexRecyclerAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private boolean isCurrent;

        // data is passed into the constructor
        FlexRecyclerAdapter( boolean isCurrent) {
            this.mInflater = LayoutInflater.from( AddTagsToPost.this);
            this.isCurrent = isCurrent;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if( isCurrent)
                view = mInflater.inflate(R.layout.single_tag_current, parent, false);
            else
                view = mInflater.inflate(R.layout.single_tag, parent, false);
            return new ViewHolder(view, isCurrent);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if( isCurrent){
                holder.setName( currentTags.get( position));
                holder.deleteTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //currentTagsAdapter.notifyItemRemoved( position);
                        currentTags.remove( position);
                        //currentTagsAdapter.notifyDataSetChanged();
                        populateCurrentTagsView();
                    }
                });
            }
            else{
                holder.setName( recentTags.get( position));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean found = false;
                        String newTag = recentTags.get( position);
                        for( String temp: currentTags){
                            if( temp.equals( newTag)) {
                                found = true;
                                Toast.makeText(AddTagsToPost.this, "Tag already exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if( !found){
                            currentTags.add( recentTags.get( position));
                            //Toast.makeText( AddTagsToPost.this, "New tag added to secret", Toast.LENGTH_SHORT).show();
                            populateCurrentTagsView();
                        }
                    }
                });
            }
        }

        // total number of rows
        @Override
        public int getItemCount() {
            if( isCurrent)
                return currentTags.size();
            else
                return recentTags.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tagName;
            Button deleteTag;

            ViewHolder(View itemView, boolean isCurrent) {
                super( itemView);
                if( isCurrent){
                    tagName = itemView.findViewById( R.id.tagName);
                    deleteTag = itemView.findViewById( R.id.deleteTag);
                }
                else{
                    tagName = itemView.findViewById( R.id.tagName);
                }
            }

            public void setName( String tgname){
                tagName.setText( tgname.trim());
            }
        }
    }
    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private ArrayList<TagSearchResult> data;

        // data is passed into the constructor
        SearchResultAdapter( ArrayList<TagSearchResult> data) {
            this.mInflater = LayoutInflater.from( AddTagsToPost.this);
            this.data = data;
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            view = mInflater.inflate(R.layout.single_tag_search, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if( position < data.size()){
                holder.setData( data.get( position).getTagName(), data.get( position).getResults());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean found = false;
                        String thething = data.get( position).getTagName();
                        for( String temp: currentTags){
                            if( temp.equals( thething)) {
                                found = true;
                                Toast.makeText(AddTagsToPost.this, "Tag already exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                        if( !found){
                            currentTags.add( thething);
                            populateCurrentTagsView();
                            searchInput.setText( "");
                        }
                    }
                });
            }
            else{
                holder.setData( "CREATE THIS TAG", "");
                holder.tagName.setTextColor(Color.BLACK);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean found = false;
                        String thething = searchInput.getText().toString().trim();
                        if( thething.length() > 0){
                            for( String temp: currentTags){
                                if( temp.equals( thething)) {
                                    found = true;
                                    Toast.makeText(AddTagsToPost.this, "Tag already exists", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if( !found){
                                currentTags.add( thething);
                                populateCurrentTagsView();
                                searchInput.setText( "");
                            }
                        }
                    }
                });
            }
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return data.size() + 1;
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tagName;
            TextView result;

            ViewHolder(View itemView) {
                super( itemView);
                tagName = itemView.findViewById( R.id.tagName);
                result = itemView.findViewById( R.id.resultCount);
            }

            public void setData( String tName, String resultCount){
                tagName.setText( tName.trim());
                result.setText( resultCount + " secrets");
            }
        }
    }
    class RefreshSearch extends AsyncTask<String, Integer, String> {
        ArrayList<TagSearchResult> data;
        @Override
        protected String doInBackground(String... params) {
            try{
                final String input = params[0];
                data= new ArrayList<>();
                rtDatabase.getReference().child( "tags").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if( dataSnapshot != null){
                            for( DataSnapshot snap: dataSnapshot.getChildren()){
                                if( snap.getKey().contains( input)){
                                    data.add( new TagSearchResult( snap.getKey(), snap.getValue().toString()));
                                }
                            }
                            publishProgress( 100);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

                //searchedTagsView.invalidate();
                //adapter.startListening();
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
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int temp = values[0];
            if( temp == 100){
                Log.d( "ABA", "ao ji");
                searchResultAdapter = new SearchResultAdapter( data);
                searchedTagsView.setAdapter( searchResultAdapter);
                if( searchResultAdapter != null) {
                    searchResultAdapter.notifyDataSetChanged();
                    searchedTagsView.invalidate();
                }
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            View mView;
            TextView tagName;
            TextView resultCount;

            public ItemViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                tagName = mView.findViewById( R.id.tagName);
                resultCount = mView.findViewById( R.id.resultCount);
            }

            public void setData( String tName, String rCount){
                tagName.setText( tName);
                resultCount.setText( rCount);
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent();
        intent.putStringArrayListExtra("returnedTagList", currentTags);
        setResult(RESULT_OK, intent);
        finish();
    }
}
