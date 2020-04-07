package pl.marta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.marta.model.Order
import pl.marta.model.User
import pl.marta.model.repositories.FirebaseRepository

class OrderEditorViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val teamMembers = MutableLiveData<List<User>>()

    fun createNewOrder(order: Order) =
        mFirebaseRepository.createNewOrder(order)

    fun updateOrder(order: Order) =
        mFirebaseRepository.updateOrder(order)

    fun deleteOrder(orderId: String) =
        mFirebaseRepository.deleteOrder(orderId)

    fun fetchTeamMembers(teamId: String) = mFirebaseRepository.fetchTeamMembers(teamMembers, teamId)
}