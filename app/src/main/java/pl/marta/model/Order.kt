package pl.marta.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import pl.marta.utils.FirestoreUtils

@Parcelize
data class Order(
    var id: String = "",
    var teamId: String = "",
    var martaName: String = "",
    var address: String = "",
    var city: String = "",
    var phone: String = "",
    var email: String = "",
    var tasks: ArrayList<Task> = arrayListOf(),
    var dateAdded: Timestamp = Timestamp.now()
) : Parcelable {
    fun getAddressFormatted() = "$address, $city"

    fun createOrderHashMap(): HashMap<String, Any> = hashMapOf(
        FirestoreUtils.firestoreKeyTeamId to this.teamId,
        FirestoreUtils.firestoreKeyMartaName to this.martaName,
        FirestoreUtils.firestoreKeyAddress to this.address,
        FirestoreUtils.firestoreKeyCity to this.city,
        FirestoreUtils.firestoreKeyPhone to this.phone,
        FirestoreUtils.firestoreKeyEmail to this.email,
        FirestoreUtils.firestoreKeyTasks to this.tasks,
        FirestoreUtils.firestoreKeyDateAdded to this.dateAdded
    )
}