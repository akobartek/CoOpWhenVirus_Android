package pl.pomocnawirus.model.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.*
import pl.pomocnawirus.utils.FirestoreUtils
import pl.pomocnawirus.utils.showShortToast

class FirebaseRepository(val app: Application) {

    private val mAuth = FirebaseAuth.getInstance()
    private val mFirestore = FirebaseFirestore.getInstance()

    private lateinit var mCurrentUserSnapshot: ListenerRegistration
    private lateinit var mTeamSnapshot: ListenerRegistration

    // region USERS
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
        mFirestore.runTransaction { transaction ->
            val snapshot = transaction.get(getTeamDocument(teamId))
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

    fun reAuthenticateUser(password: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val credential = EmailAuthProvider.getCredential(mAuth.currentUser!!.email!!, password)
        mAuth.currentUser!!.reauthenticate(credential)
            .addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun updateUserPassword(newPassword: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mAuth.currentUser!!.updatePassword(newPassword)
            .addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun updateUserEmail(newEmail: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mAuth.currentUser!!.updateEmail(newEmail)
            .addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun deleteUser(): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mAuth.currentUser!!.delete()
            .addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    private fun getCurrentUserDocument() =
        mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .document(mAuth.currentUser!!.uid)
    // endregion USERS


    //region TEAMS
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

    fun leaveTeam(isLeader: Boolean, teamId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val teamDocument = mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document()
        mFirestore.runTransaction { transaction ->
            val team = transaction.get(teamDocument).toObject(Team::class.java)!!
            if (!isLeader) {
                Tasks.await(resetUserActivities(teamId, transaction))
                unregisterTeamListener()
                transaction.update(
                    getCurrentUserDocument(),
                    FirestoreUtils.firestoreKeyTeamId,
                    ""
                )
            } else if (team.leaders.size > 1) {
                Tasks.await(resetUserActivities(teamId, transaction))
                unregisterTeamListener()
                transaction.update(
                    teamDocument,
                    FirestoreUtils.firestoreKeyLeaders,
                    team.leaders.remove(mAuth.currentUser!!.uid)
                )
                transaction.update(
                    getCurrentUserDocument(),
                    FirestoreUtils.firestoreKeyTeamId,
                    ""
                )
            } else {
                val usersSnap =
                    Tasks.await(mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
                        .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId).get()
                        .addOnFailureListener { throw Exception("Something went wrong") })
                if (usersSnap.documents.size == 1) {
                    mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
                        .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId)
                        .addSnapshotListener { querySnapshot, _ ->
                            querySnapshot?.documents?.forEach { transaction.delete(it.reference) }
                        }
                    unregisterTeamListener()
                    transaction.delete(teamDocument)
                    transaction.update(
                        getCurrentUserDocument(),
                        FirestoreUtils.firestoreKeyTeamId,
                        ""
                    )
                } else throw Exception("User in the only leader in the group!")
            }
        }.addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun unregisterTeamListener() {
        if (::mTeamSnapshot.isInitialized) mTeamSnapshot.remove()
    }

    private fun resetUserActivities(
        teamId: String,
        transaction: Transaction
    ): com.google.android.gms.tasks.Task<QuerySnapshot> {
        val ordersDocument = mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
        return ordersDocument
            .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId)
            .get()
            .addOnSuccessListener {
                it.toObjects(Order::class.java).forEach { order ->
                    order.tasks.forEach { task ->
                        if (task.volunteerId == mAuth.currentUser!!.uid) {
                            task.volunteerId = ""
                            if (task.status == Task.TASK_STATUS_ACCEPTED)
                                task.status = Task.TASK_STATUS_ADDED
                        }
                    }
                    transaction.update(
                        ordersDocument.document(order.id),
                        FirestoreUtils.firestoreKeyTasks,
                        order.tasks
                    )
                }
            }
    }

    private fun getTeamDocument(teamId: String) =
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document(teamId)
    // endregion TEAMS


    // region ORDERS
    fun fetchOrders(ordersMutableLiveData: MutableLiveData<ArrayList<Order>>, teamId: String) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
            .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId)
            .orderBy(FirestoreUtils.firestoreKeyDateAdded, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { orders ->
                val arrayList = arrayListOf<Order>()
                arrayList.addAll(orders!!.toObjects(Order::class.java))
                ordersMutableLiveData.postValue(arrayList)
            }
            .addOnFailureListener { ordersMutableLiveData.postValue(arrayListOf()) }
    }

    fun createNewOrder(order: Order, context: Context) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .add(order.createOrderHashMap())
            .addOnSuccessListener { context.showShortToast(R.string.order_saved) }
            .addOnFailureListener { context.showShortToast(R.string.order_save_error_message) }
    }

    fun updateOrder(order: Order): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
            .document(order.id)
            .set(order.createOrderHashMap())
            .addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun updateOrder(order: Order, context: Context) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .document(order.id)
            .set(order.createOrderHashMap())
            .addOnSuccessListener { context.showShortToast(R.string.order_saved) }
            .addOnFailureListener { context.showShortToast(R.string.order_save_error_message) }
    }

    fun deleteOrder(orderId: String, context: Context) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .document(orderId)
            .delete()
            .addOnSuccessListener { context.showShortToast(R.string.order_delete_successfully) }
            .addOnFailureListener { context.showShortToast(R.string.order_delete_error) }
    }
    // endregion ORDERS
}