package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pl.pomocnawirus.model.Group
import pl.pomocnawirus.model.User
import pl.pomocnawirus.model.repositories.FirebaseRepository

class GroupJoinViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    fun checkIfGroupExists(groupId: String) =
        mFirebaseRepository.checkIfGroupExists(groupId)

    fun updateUser(user: User) =
        mFirebaseRepository.updateUserData(user)

    fun createNewGroup(group: Group) =
        mFirebaseRepository.createNewGroup(group)
}