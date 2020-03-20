package pl.pomocnawirus.model.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import pl.pomocnawirus.model.Group
import pl.pomocnawirus.model.User
import pl.pomocnawirus.utils.FirestoreUtils

class FirebaseRepository(val app: Application) {

    private val mAuth = FirebaseAuth.getInstance()
    private val mFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(userMutableLiveData: MutableLiveData<User>) =
        mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .document(mAuth.currentUser!!.uid)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                }
                userMutableLiveData.postValue(querySnapshot!!.toObject(User::class.java))
            }

    fun updateUserData(user: User): MutableLiveData<Boolean> {
        val isOperationSuccessful = MutableLiveData<Boolean>()
        mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .document(user.id)
            .set(user.createUserHashMap())
            .addOnSuccessListener { isOperationSuccessful.postValue(true) }
            .addOnFailureListener { isOperationSuccessful.postValue(false) }
        return isOperationSuccessful
    }

    fun checkIfGroupExists(groupId: String): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        mFirestore.collection(FirestoreUtils.firestoreCollectionGroups)
            .document(groupId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                    liveData.postValue(false)
                }
                Log.d("xDDDD", querySnapshot.toString())
                liveData.postValue(true)
            }
        return liveData
    }

    fun createNewGroup(group: Group): MutableLiveData<Boolean> {
        val isOperationSuccessful = MutableLiveData<Boolean>()
        mFirestore.collection(FirestoreUtils.firestoreCollectionGroups)
            .add(group.createGroupHashMap())
            .addOnSuccessListener { isOperationSuccessful.postValue(true) }
            .addOnFailureListener { isOperationSuccessful.postValue(false) }
        return isOperationSuccessful
    }
}