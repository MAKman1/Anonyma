package allinontech.anonyma.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import allinontech.anonyma.R;
import allinontech.anonyma.SeePostsByTag;
import allinontech.anonyma.backend.TagSearchResult;
import allinontech.anonyma.backend.Util;

public class TagsFragment extends Fragment {


    RecyclerView recentTagsView;
    RecyclerView searchedTagsView;
    TextView recentHeading;

    EditText searchInput;


    FlexRecyclerAdapter recentTagsAdapter;

    SearchResultAdapter searchResultAdapter;

    ArrayList<String> recentTags;

    FirebaseDatabase rtDatabase;

    RefreshSearch rf;

    public TagsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_tags, container,false);

        recentTagsView = v.findViewById( R.id.recentTags);
        searchedTagsView = v.findViewById( R.id.searchedTags);
        recentHeading  = v.findViewById( R.id.recentHeading);
        searchInput = v.findViewById( R.id.searchInput);


        if( rtDatabase == null)
            rtDatabase = Util.getDatabase();

        recentTags = new ArrayList<>();

;

        recentTagsAdapter = new FlexRecyclerAdapter();
        searchedTagsView.setLayoutManager( new LinearLayoutManager( getActivity()));

        recentTagsView.setLayoutManager( new LinearLayoutManager( getActivity()));
        recentTagsView.setAdapter( recentTagsAdapter);

        populateRecentTags();

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

        //add code
        return v;
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

        // data is passed into the constructor
        FlexRecyclerAdapter() {
            this.mInflater = LayoutInflater.from( getActivity());
        }

        // inflates the row layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            view = mInflater.inflate(R.layout.single_tag_current, parent, false);
            return new ViewHolder(view);
        }

        // binds the data to the TextView in each row
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.setName( recentTags.get( position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText( getActivity(), "Found Tag", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return recentTags.size();
        }


        // stores and recycles views as they are scrolled off screen
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tagName;

            ViewHolder(View itemView) {
                super( itemView);
                tagName = itemView.findViewById( R.id.tagName);
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
            this.mInflater = LayoutInflater.from( getActivity());
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
                        searchInput.setText( "");
                        Intent intent = new Intent( getContext(), SeePostsByTag.class);
                        intent.putExtra("TAG_NAME", data.get( position).getTagName());
                        startActivity(intent);
                    }
                });
            }
        }

        // total number of rows
        @Override
        public int getItemCount() {
            return data.size();
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
}