const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.IncLikesCount = functions.firestore
    .document('posts/{postId}/likes/{documentId}')
    .onCreate((snap, event) => {

    const postUid = event.params.postId;

    // ref to the parent document
    const docRef = admin.firestore().collection('posts').doc(postUid);

    return docRef.get().then(snap => {

           // get the total comment count and add one
           const like_count = snap.data().like_count + 1;

            const like = {like_count};
           // run update
           return docRef.update( like);
     });

});
exports.DecLikesCount = functions.firestore
    .document('posts/{postId}/likes/{documentId}')
    .onDelete((snap, event) => {

    const postUid = event.params.postId;

    // ref to the parent document
    const docRef = admin.firestore().collection('posts').doc(postUid);

    return docRef.get().then(snap => {

           // get the total comment count and add one
          const like_count = snap.data().like_count - 1;

           const like = {like_count};
          // run update
          return docRef.update( like);
     });

});
exports.IncCommentsCount = functions.firestore
    .document('posts/{postId}/comments/{documentId}')
    .onCreate((snap, event) => {

    const postUid = event.params.postId;

    // ref to the parent document
    const docRef = admin.firestore().collection('posts').doc(postUid);

    return docRef.get().then(snap => {

           // get the total comment count and add one
           const comment_count = snap.data().comment_count + 1;

            const comments = {comment_count};
           // run update
           return docRef.update( comments);
     });

});
exports.DecCommentsCount = functions.firestore
    .document('posts/{postId}/comments/{documentId}')
    .onDelete((snap, event) => {

    const postUid = event.params.postId;

    // ref to the parent document
    const docRef = admin.firestore().collection('posts').doc(postUid);

    return docRef.get().then(snap => {

               // get the total comment count and add one
               const comment_count = snap.data().comment_count - 1;

                const comments = {comment_count};
               // run update
               return docRef.update( comments);
     });

});
exports.AddTagToRealTimeDB = functions.firestore
    .document('tags/{tagName}/posts/{postId}')
    .onCreate((snap, event) => {

    const postUid = event.params.postId;
    const tagname = event.params.tagName;

    // ref to the parent document
    const docRef = admin.database().ref('tags');

    return docRef.once("value", function(snapshot) {
        if( snapshot !== null){
            var currentSnap = snapshot.child( tagname);
            if (currentSnap.exists()){
                  var currentCount = currentSnap.val() + 1;
                  docRef.update({
                        [tagname]: currentCount
                  });
            }
            else{
                docRef.update({
                    [tagname] : 1
                })
            }
        }
    }, function (errorObject) {
        console.log("The read failed: " + errorObject.code);
    });
});

exports.SendNotification = functions.database.ref('userChats/{theUserId}/{messageId}').onWrite((snapshot, event) => {

	//get the userId of the person receiving the notification because we need to get their token
	const receiverId = event.params.theUserId;
	const original = snapshot.after.val().notificationId;
	console.log("receiverId: ", receiverId);
	console.log("senderId: ", original);

    const docRef = admin.firestore().collection('users').doc( receiverId);

    return docRef.get().then(snap => {

                   // get the total comment count and add one
                   const fcm_id = snap.data().fcm_token;

                   const payload = {
                       data: {
                           uid: original,
                           title: "ANONYMA",
                           message: "Someone just messaged you...",
                       }
                   };
                   return admin.messaging().sendToDevice(fcm_id, payload)
                       .then(function(response) {
                           return console.log("Successfully sent message:", response);
                         })
                         .catch(function(error) {
                           return console.log("Error sending message:", error);
                         });
         });
});

