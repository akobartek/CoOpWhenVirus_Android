package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.Order
import pl.pomocnawirus.model.repositories.FirebaseRepository
import pl.pomocnawirus.utils.Filters

class TasksViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val orders = MutableLiveData<ArrayList<Order>>()

    val filters = MutableLiveData<Filters>(null)

    fun fetchOrders(teamId: String) = mFirebaseRepository.fetchOrders(orders, teamId)

    fun updateOrder(order: Order) = mFirebaseRepository.updateOrder(order)
}