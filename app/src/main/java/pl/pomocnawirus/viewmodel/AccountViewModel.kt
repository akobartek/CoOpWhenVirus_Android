package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import pl.pomocnawirus.model.User
import pl.pomocnawirus.model.repositories.FirebaseRepository

class AccountViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    fun updateUserData(user: User) = mFirebaseRepository.updateUserData(user)

    fun leaveTeam(isLeader: Boolean, teamId: String) =
        mFirebaseRepository.leaveTeam(isLeader, teamId)

    fun reAuthenticateUser(password: String) =
        mFirebaseRepository.reAuthenticateUser(password)

    fun updateUserPassword(newPassword: String) =
        mFirebaseRepository.updateUserPassword(newPassword)

    fun updateUserEmail(newEmail: String) =
        mFirebaseRepository.updateUserEmail(newEmail)

    fun deleteUser() = mFirebaseRepository.deleteUser()
}