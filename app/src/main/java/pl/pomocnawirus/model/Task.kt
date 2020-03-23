package pl.pomocnawirus.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Task(
    @DocumentId var id: String = "",
    var type: String = TASK_TYPE_OTHER,
    var description: String = "",
    var status: Int = 0,
    var realizationDate: Timestamp? = null,
    var volunteerId: String = ""
) {
    companion object {
        const val TASK_TYPE_SHOPPING = "SHOPPING"
        const val TASK_TYPE_PETS = "PETS"
        const val TASK_TYPE_HOME = "HOME"
        const val TASK_TYPE_OTHER = "OTHER"

        const val TASK_STATUS_ADDED = 0
        const val TASK_STATUS_ACCEPTED = 1
        const val TASK_STATUS_COMPLETE = 2
    }
}