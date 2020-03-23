package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.User
import pl.pomocnawirus.model.repositories.FirebaseRepository

class MainViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val currentUser = MutableLiveData<User>()

    init {
        currentUser.postValue(null)
    }

    fun fetchUser() = mFirebaseRepository.fetchCurrentUser(currentUser)

    fun unregisterUserListener() = mFirebaseRepository.unregisterUserListener()
}