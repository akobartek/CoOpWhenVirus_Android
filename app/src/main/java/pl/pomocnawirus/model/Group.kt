package pl.pomocnawirus.model

data class Group(
    var id: String = "",
    var name: String,
    var city: String,
    var admins: String,
    var email: String = "",
    var phone: String = ""
)