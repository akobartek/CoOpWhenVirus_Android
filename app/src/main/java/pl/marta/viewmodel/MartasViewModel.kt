package pl.marta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.marta.model.Marta
import pl.marta.model.repositories.FirebaseRepository

class MartasViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val martas = MutableLiveData<ArrayList<Marta>>()

    fun fetchMartas(teamId: String) = mFirebaseRepository.fetchMartas(martas, teamId)

    fun addNewMarta(marta: Marta) = mFirebaseRepository.addNewMarta(marta, true)

    fun updateMarta(marta: Marta) = mFirebaseRepository.updateMarta(marta)
}