package pl.pomocnawirus.model

data class Order(
    var id: String = "",
    var needyName: String,
    var street: String,
    var buildingNumber: String,
    var houseNumer: String,
    var city: String,
    var phone: String = "",
    var email: String = "",
    var tasksIds: String = ""
)