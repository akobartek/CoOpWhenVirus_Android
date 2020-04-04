package pl.pomocnawirus.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize
import pl.pomocnawirus.R

@Parcelize
data class Task(
    var type: String = TASK_TYPE_OTHER,
    var description: String = "",
    var status: Int = TASK_STATUS_ADDED,
    var realizationDate: Timestamp? = null,
    var volunteerId: String = ""
) : Parcelable {
    companion object {
        const val TASK_TYPE_SHOPPING = "SHOPPING"
        const val TASK_TYPE_PETS = "PET_CARE"
        const val TASK_TYPE_HOME = "HOME"
        const val TASK_TYPE_OTHER = "OTHER"

        const val TASK_STATUS_ADDED = 0
        const val TASK_STATUS_ACCEPTED = 1
        const val TASK_STATUS_COMPLETE = 2
    }

    @Exclude
    fun getIconDrawableId() =
        if (status == TASK_STATUS_COMPLETE) R.drawable.ic_done
        else when (type) {
            TASK_TYPE_SHOPPING -> R.drawable.ic_task_shopping
            TASK_TYPE_PETS -> R.drawable.ic_task_pets
            TASK_TYPE_HOME -> R.drawable.ic_task_home
            else -> R.drawable.ic_task_other
        }
}