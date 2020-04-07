package pl.marta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.marta.model.Order
import pl.marta.model.repositories.FirebaseRepository
import pl.marta.utils.Filters

class TasksViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val orders = MutableLiveData<ArrayList<Order>>()
    val filters = MutableLiveData<Filters>(null)
    var teamId = ""

    fun fetchOrders() = mFirebaseRepository.fetchOrders(orders, teamId)

    fun updateOrder(order: Order) = mFirebaseRepository.updateOrderAndWaitForResult(order)
}