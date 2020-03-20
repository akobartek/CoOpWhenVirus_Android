package pl.pomocnawirus.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Task(
    @DocumentId var id: String = "",
    var type: String = "",
    var description: String = "",
    var status: Int = 0,
    var realizationDate: Timestamp = Timestamp.now(),
    var volunteerId: String = ""
)