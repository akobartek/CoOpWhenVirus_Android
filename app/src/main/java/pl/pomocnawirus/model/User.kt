package pl.pomocnawirus.model

import pl.pomocnawirus.utils.FirestoreUtils

data class User(
    var id: String = "",
    val email: String,
    var name: String,
    var phone: String = "",
    var groupId: String = "",
    val tasksToDo: String = ""
) {
    fun createUserHashMap(): HashMap<String, Any> {
        val user = HashMap<String, Any>()
        user[FirestoreUtils.firestoreKeyEmail] = this.email
        user[FirestoreUtils.firestoreKeyName] = this.name
        user[FirestoreUtils.firestoreKeyPhone] = this.phone
        user[FirestoreUtils.firestoreKeyGroupId] = this.groupId
        user[FirestoreUtils.firestoreKeyTasks] = this.tasksToDo
        return user
    }
}