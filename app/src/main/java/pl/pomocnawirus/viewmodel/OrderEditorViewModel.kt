package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pl.pomocnawirus.model.Order
import pl.pomocnawirus.model.repositories.FirebaseRepository

class OrderEditorViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    fun createNewOrder(order: Order) =
        mFirebaseRepository.createNewOrder(order, app.applicationContext)

    fun updateOrder(order: Order) =
        mFirebaseRepository.updateOrder(order, app.applicationContext)

    fun deleteOrder(orderId: String) =
        mFirebaseRepository.deleteOrder(orderId, app.applicationContext)
}