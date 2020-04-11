package pl.marta.model

import pl.marta.utils.FirestoreUtils

data class Marta(
    var id: String = "",
    var teamId: String = "",
    var name: String = "",
    var address: String = "",
    var city: String = "",
    var phone: String = "",
    var email: String = ""
) {
    fun getAddressFormatted() = "$address, $city"

    fun createMartaHashMap(): HashMap<String, Any> = hashMapOf(
        FirestoreUtils.firestoreKeyTeamId to this.teamId,
        FirestoreUtils.firestoreKeyName to this.name,
        FirestoreUtils.firestoreKeyAddress to this.address,
        FirestoreUtils.firestoreKeyCity to this.city,
        FirestoreUtils.firestoreKeyPhone to this.phone,
        FirestoreUtils.firestoreKeyEmail to this.email
    )
}