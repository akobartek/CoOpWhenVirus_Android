package pl.pomocnawirus.model.repositories

import android.app.Application
import android.content.Context
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
    private lateinit var mOrdersSnapshot: ListenerRegistration
    private lateinit var mMembersSnapshot: ListenerRegistration

    fun unregisterListeners() {
        if (::mTeamSnapshot.isInitialized) mTeamSnapshot.remove()
        if (::mMembersSnapshot.isInitialized) mMembersSnapshot.remove()
        if (::mOrdersSnapshot.isInitialized) mOrdersSnapshot.remove()
    }

    // region USERS
    fun fetchCurrentUser(userMutableLiveData: MutableLiveData<User>) {
        mCurrentUserSnapshot =
            getUserDocument(mAuth.currentUser!!.uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    userMutableLiveData.postValue(null)
                    return@addSnapshotListener
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
                    getUserDocument(mAuth.currentUser!!.uid),
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

    private fun getUserDocument(userId: String) =
        mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .document(userId)
    // endregion USERS


    //region TEAMS
    fun fetchTeam(teamMutableLiveData: MutableLiveData<Team>, teamId: String) {
        mTeamSnapshot =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
                .document(teamId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) {
                        return@addSnapshotListener
                    }
                    teamMutableLiveData.postValue(querySnapshot!!.toObject(Team::class.java))
                }
    }

    fun createNewTeam(team: Team): MutableLiveData<String> {
        val isOperationSuccessful = MutableLiveData<String>()
        val teamDocument =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document()
        mFirestore.runBatch { batch ->
            batch.set(teamDocument, team.createTeamHashMap())
            batch.update(
                getUserDocument(mAuth.currentUser!!.uid),
                FirestoreUtils.firestoreKeyTeamId,
                teamDocument.id
            )
            batch.update(
                getUserDocument(mAuth.currentUser!!.uid),
                FirestoreUtils.firestoreKeyUserType,
                User.USER_TYPE_LEADER
            )
        }.addOnSuccessListener { isOperationSuccessful.postValue(teamDocument.id) }
            .addOnFailureListener { isOperationSuccessful.postValue("") }
        return isOperationSuccessful
    }

    fun updateTeam(team: Team, context: Context) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .document(team.id)
            .set(team.createTeamHashMap())
            .addOnSuccessListener { context.showShortToast(R.string.team_updated) }
            .addOnFailureListener { context.showShortToast(R.string.team_update_error_message) }
    }

    fun fetchTeamMembers(membersLiveData: MutableLiveData<List<User>>, teamId: String) {
        mMembersSnapshot = mFirestore.collection(FirestoreUtils.firestoreCollectionUsers)
            .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    membersLiveData.postValue(listOf())
                    return@addSnapshotListener
                }
                membersLiveData.postValue(querySnapshot?.toObjects(User::class.java))
            }
    }

    fun makeNewLeader(user: User): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mFirestore.runBatch { batch ->
            batch.update(
                getUserDocument(user.id),
                FirestoreUtils.firestoreKeyUserType,
                User.USER_TYPE_LEADER
            )
            batch.update(
                getTeamDocument(user.teamId),
                FirestoreUtils.firestoreKeyLeaders,
                FieldValue.arrayUnion(user.id)
            )
        }.addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun removeLeaderRole(user: User): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mFirestore.runBatch { batch ->
            batch.update(
                getUserDocument(user.id), FirestoreUtils.firestoreKeyUserType, User.USER_TYPE_USER
            )
            batch.update(
                getTeamDocument(user.teamId),
                FirestoreUtils.firestoreKeyLeaders,
                FieldValue.arrayRemove(user.id)
            )
        }.addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun removeUserFromTeam(user: User): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        mFirestore.runTransaction { transaction ->
            Tasks.await(resetUserActivities(user.teamId, transaction, user.id))
            if (user.userType == User.USER_TYPE_LEADER) {
                transaction.update(
                    mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document(user.teamId),
                    FirestoreUtils.firestoreKeyLeaders,
                    FieldValue.arrayRemove(user.id)
                )
                transaction.update(
                    getUserDocument(user.id),
                    FirestoreUtils.firestoreKeyUserType,
                    User.USER_TYPE_USER
                )
            }
            transaction.update(
                getUserDocument(user.id), FirestoreUtils.firestoreKeyTeamId, ""
            )
        }.addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    fun getAllTeams(teamsLiveData: MutableLiveData<List<TeamSimple>>) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionTeams)
            .orderBy(FirestoreUtils.firestoreKeyCity, Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    teamsLiveData.postValue(listOf())
                    return@addSnapshotListener
                }
                teamsLiveData.postValue(querySnapshot!!.toObjects(TeamSimple::class.java))
            }
    }

    fun leaveTeam(isLeader: Boolean, teamId: String): MutableLiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val teamDocument =
            mFirestore.collection(FirestoreUtils.firestoreCollectionTeams).document(teamId)
        mFirestore.runTransaction { transaction ->
            val team = transaction.get(teamDocument).toObject(Team::class.java)!!
            if (!isLeader) {
                Tasks.await(resetUserActivities(teamId, transaction, mAuth.currentUser!!.uid))
                unregisterListeners()
                transaction.update(
                    getUserDocument(mAuth.currentUser!!.uid),
                    FirestoreUtils.firestoreKeyTeamId,
                    ""
                )
            } else if (team.leaders.size > 1) {
                Tasks.await(resetUserActivities(teamId, transaction, mAuth.currentUser!!.uid))
                unregisterListeners()
                transaction.update(
                    teamDocument,
                    FirestoreUtils.firestoreKeyLeaders,
                    FieldValue.arrayRemove(mAuth.currentUser!!.uid)
                )
                transaction.update(
                    getUserDocument(mAuth.currentUser!!.uid),
                    FirestoreUtils.firestoreKeyUserType,
                    User.USER_TYPE_USER
                )
                transaction.update(
                    getUserDocument(mAuth.currentUser!!.uid),
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
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            querySnapshot?.documents?.forEach { transaction.delete(it.reference) }
                        }
                    unregisterListeners()
                    transaction.delete(teamDocument)
                    transaction.update(
                        getUserDocument(mAuth.currentUser!!.uid),
                        FirestoreUtils.firestoreKeyUserType,
                        User.USER_TYPE_USER
                    )
                    transaction.update(
                        getUserDocument(mAuth.currentUser!!.uid),
                        FirestoreUtils.firestoreKeyTeamId,
                        ""
                    )
                } else throw Exception("User in the only leader in the group!")
            }
        }.addOnSuccessListener { result.postValue(true) }
            .addOnFailureListener { result.postValue(false) }
        return result
    }

    private fun resetUserActivities(
        teamId: String, transaction: Transaction, userId: String
    ): com.google.android.gms.tasks.Task<QuerySnapshot> {
        val ordersDocument = mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
        return ordersDocument
            .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId)
            .get()
            .addOnSuccessListener {
                it.toObjects(Order::class.java).forEach { order ->
                    order.tasks.forEach { task ->
                        if (task.volunteerId == userId) {
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
        mOrdersSnapshot = mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
            .whereEqualTo(FirestoreUtils.firestoreKeyTeamId, teamId)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    ordersMutableLiveData.postValue(arrayListOf())
                    return@addSnapshotListener
                }
                val arrayList = arrayListOf<Order>()
                arrayList.addAll(querySnapshot!!.toObjects(Order::class.java))
                arrayList.sortByDescending { it.dateAdded }
                ordersMutableLiveData.postValue(arrayList)
            }
    }

    fun createNewOrder(order: Order, context: Context) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
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
        mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
            .document(order.id)
            .set(order.createOrderHashMap())
            .addOnSuccessListener { context.showShortToast(R.string.order_saved) }
            .addOnFailureListener { context.showShortToast(R.string.order_save_error_message) }
    }

    fun deleteOrder(orderId: String, context: Context) {
        mFirestore.collection(FirestoreUtils.firestoreCollectionOrders)
            .document(orderId)
            .delete()
            .addOnSuccessListener { context.showShortToast(R.string.order_delete_successfully) }
            .addOnFailureListener { context.showShortToast(R.string.order_delete_error) }
    }
    // endregion ORDERS
}