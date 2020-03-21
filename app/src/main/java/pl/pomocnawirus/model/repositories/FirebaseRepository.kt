package pl.pomocnawirus.model.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import pl.pomocnawirus.model.Team
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

    fun checkIfTeamExists(teamId: String): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .document(teamId)
            .addSnapshotListener { _, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                    liveData.postValue(false)
                }
                liveData.postValue(true)
            }
        return liveData
    }

    fun createNewTeam(team: Team): MutableLiveData<Boolean> {
        val isOperationSuccessful = MutableLiveData<Boolean>()
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .add(team.createTeamHashMap())
            .addOnSuccessListener { isOperationSuccessful.postValue(true) }
            .addOnFailureListener { isOperationSuccessful.postValue(false) }
        return isOperationSuccessful
    }

    fun getAllTeams(teamsLiveData: MutableLiveData<List<Team>>) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .orderBy(FirestoreUtils.firestoreKeyCity, Query.Direction.ASCENDING)
            .orderBy(FirestoreUtils.firestoreKeyName, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                }
                teamsLiveData.postValue(querySnapshot!!.toObjects(Team::class.java))
            }
    }

}