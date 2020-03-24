package pl.pomocnawirus.model

import com.google.firebase.firestore.DocumentId
import pl.pomocnawirus.utils.FirestoreUtils

data class Order(
    @DocumentId var id: String = "",
    var teamId: String = "",
    var needyName: String = "",
    var street: String = "",
    var buildingNumber: String = "",
    var houseNumber: String = "",
    var city: String = "",
    var phone: String = "",
    var email: String = "",
    var tasks: ArrayList<Task> = arrayListOf()
) {
    fun getAddressFormatted() =
        "$street $buildingNumber${if (houseNumber.isNotEmpty()) "/$houseNumber" else ""}, $city"

    fun createOrderHashMap(): HashMap<String, Any> = hashMapOf(
        FirestoreUtils.firestoreKeyTeamId to this.teamId,
        FirestoreUtils.firestoreKeyNeedyName to this.needyName,
        FirestoreUtils.firestoreKeyStreet to this.street,
        FirestoreUtils.firestoreKeyBuildingNumber to this.buildingNumber,
        FirestoreUtils.firestoreKeyHouseNumber to this.houseNumber,
        FirestoreUtils.firestoreKeyCity to this.city,
        FirestoreUtils.firestoreKeyPhone to this.phone,
        FirestoreUtils.firestoreKeyEmail to this.email,
        FirestoreUtils.firestoreKeyTasks to this.tasks
    )
}