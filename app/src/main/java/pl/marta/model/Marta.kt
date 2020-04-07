package pl.marta.model

import pl.marta.utils.FirestoreUtils

data class Marta(
    var id: String = "",
    var name: String = "",
    var address: String = "",
    var city: String = "",
    var phone: String = "",
    var email: String = ""
) {
    fun createOrderHashMap(): HashMap<String, Any> = hashMapOf(
        FirestoreUtils.firestoreKeyName to this.name,
        FirestoreUtils.firestoreKeyAddress to this.address,
        FirestoreUtils.firestoreKeyCity to this.city,
        FirestoreUtils.firestoreKeyPhone to this.phone,
        FirestoreUtils.firestoreKeyEmail to this.email
    )
}