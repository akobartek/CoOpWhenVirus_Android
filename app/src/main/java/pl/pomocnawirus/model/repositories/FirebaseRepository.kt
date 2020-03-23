package pl.pomocnawirus.model.repositories

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.model.TeamSimple
import pl.pomocnawirus.model.User
import pl.pomocnawirus.utils.FirestoreUtils

class FirebaseRepository(val app: Application) {

    private val mAuth = FirebaseAuth.getInstance()
    private val mFirestore = FirebaseFirestore.getInstance()

    private lateinit var mCurrentUserSnapshot: ListenerRegistration
    private lateinit var mTeamSnapshot: ListenerRegistration

    fun fetchCurrentUser(userMutableLiveData: MutableLiveData<User>) {
        mCurrentUserSnapshot =
            getCurrentUserDocument().addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                }
                userMutableLiveData.postValue(querySnapshot!!.toObject(User::class.java))
            }
    }

    fun unregisterUserListener() = mCurrentUserSnapshot.remove()

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

    fun createNewTeam(team: Team): MutableLiveData<String> {
        val isOperationSuccessful = MutableLiveData<String>()
        val teamDocument =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document()
        mFirestore.runBatch { batch ->
            batch.set(teamDocument, team.createTeamHashMap())
            batch.update(
                getCurrentUserDocument(),
                FirestoreUtils.firestoreKeyTeamId,
                teamDocument.id
            )
            batch.update(
                getCurrentUserDocument(),
                FirestoreUtils.firestoreKeyUserType,
                User.USER_TYPE_LEADER
            )
        }.addOnSuccessListener { isOperationSuccessful.postValue(teamDocument.id) }
            .addOnFailureListener { isOperationSuccessful.postValue("") }
        return isOperationSuccessful
    }

    fun getAllTeams(teamsLiveData: MutableLiveData<List<TeamSimple>>) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .orderBy(FirestoreUtils.firestoreKeyCity, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                }
                teamsLiveData.postValue(querySnapshot!!.toObjects(TeamSimple::class.java))
            }
    }

    fun fetchTeam(teamMutableLiveData: MutableLiveData<Team>, teamId: String) {
        mTeamSnapshot =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
                .document(teamId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        Log.e("FirebaseRepository", firebaseFirestoreException.toString())
                    }
                    teamMutableLiveData.postValue(querySnapshot!!.toObject(Team::class.java))
                }
    }

    fun unregisterTeamListener() = mTeamSnapshot.remove()

    private fun getCurrentUserDocument() =
        mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .document(mAuth.currentUser!!.uid)
}