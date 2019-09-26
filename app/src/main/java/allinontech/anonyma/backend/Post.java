package allinontech.anonyma.backend;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import allinontech.anonyma.Anonyma;
import allinontech.anonyma.HomeScreen;

import static android.content.ContentValues.TAG;

public class Post {

    boolean likedByMe;
    String postId;
    String imageUrl;
    String text;
    String datetime;
    String comments;
    String likes;
    String sentByUser;
    String sentById;

    FirebaseFirestore firestore = Util.getFirestore();

    public Post(String postId, String imageUrl, final String text, String datetime, String comments, String likes, String sentByUser, String sentById){
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.text = text;
        this.datetime = datetime;
        this.sentByUser = sentByUser;
        this.comments = comments;
        this.likes = likes;
        this.sentById = sentById;
        likedByMe = false;
    }
    public void setLikedByMe(final boolean likedBy) {
        if( likedBy){

            likes = Integer.parseInt( likes) + 1 + "";

            final Map<String, Object> data = new HashMap<>();
            data.put("token", "token");

            firestore.collection("posts").document( this.getPostId()).collection( "likes").document( HomeScreen.UserId)
                    .set( data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            likedByMe = likedBy;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d( "ABCD", e.toString());
                            likes = Integer.parseInt( likes) - 1 + "";
                        }
                    });
        }
        else{
            likes = Integer.parseInt( likes) - 1 + "";
            firestore.collection("posts").document( this.getPostId()).collection( "likes").document( HomeScreen.UserId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            likedByMe = likedBy;
                            //Toast.makeText(Anonyma.getAppContext(), "UNLIKE", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error deleting document", e);
                            likes = Integer.parseInt( likes) + 1 + "";
                        }
                    });
        }
    }

    public void isLikedByMe( final TheCallback callback) {
        DocumentReference query = firestore.collection( "posts").document( this.getPostId()).collection( "likes").document(HomeScreen.UserId);
        query.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d( "TESTNODE", "liked by me!");
                    likedByMe = true;
                    callback.onCallback( true);
                } else {
                    Log.d( "TESTNODE", text + "____________________");
                    likedByMe = false;
                    callback.onCallback( false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d( "TESTNODE", text + "____________________");
                likedByMe = false;
                callback.onCallback( false);
            }
        });
    }
    public boolean getLikedByMe(){
        return likedByMe;
    }

    public String getSentById() {
        return sentById;
    }

    public void setSentById(String sentById) {
        this.sentById = sentById;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getComments() {

        return comments;
    }

    public String getLikes() {
        return likes;
    }

    public String getPostId() {
        return postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getSentByUser() {
        return sentByUser;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setSentByUser(String sentByUser) {
        this.sentByUser = sentByUser;
    }
}
