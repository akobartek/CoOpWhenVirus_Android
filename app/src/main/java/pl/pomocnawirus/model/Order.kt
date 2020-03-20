package pl.pomocnawirus.model

import com.google.firebase.firestore.DocumentId

data class Order(
    @DocumentId var id: String = "",
    var needyName: String = "",
    var street: String = "",
    var buildingNumber: String = "",
    var houseNumer: String = "",
    var city: String = "",
    var phone: String = "",
    var email: String = "",
    var tasks: ArrayList<Task> = arrayListOf()
)