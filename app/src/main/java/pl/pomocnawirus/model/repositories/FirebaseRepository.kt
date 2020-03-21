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
        getCurrentUserDocument().addSnapshotListener { querySnapshot, firebaseFirestoreException ->
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

    fun addUserToTeam(teamId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val teamDocument =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document(teamId)
        mFirestore.runTransaction { transaction ->
            val snapshot = transaction.get(teamDocument)
            if (snapshot.exists()) {
                transaction.update(
                    getCurrentUserDocument(),
                    FirestoreUtils.firestoreKeyTeamId,
                    teamId
                )
            } else {
                throw Exception("Group don't exists")
            }
        }.addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun createNewTeam(team: Team): MutableLiveData<Boolean> {
        val isOperationSuccessful = MutableLiveData<Boolean>()
        val teamDocument =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document()
        mFirestore.runBatch { batch ->
            batch.set(teamDocument, team.createTeamHashMap())
            batch.update(
                getCurrentUserDocument(),
                FirestoreUtils.firestoreKeyTeamId,
                teamDocument.id
            )
        }.addOnSuccessListener { isOperationSuccessful.postValue(true) }
            .addOnFailureListener { isOperationSuccessful.postValue(false) }
        return isOperationSuccessful
    }

    fun getAllTeams(teamsLiveData: MutableLiveData<List<Team>>) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .orderBy(FirestoreUtils.firestoreKeyCity, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                }
                teamsLiveData.postValue(querySnapshot!!.toObjects(Team::class.java))
            }
    }

    private fun getCurrentUserDocument() =
        mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .document(mAuth.currentUser!!.uid)
}