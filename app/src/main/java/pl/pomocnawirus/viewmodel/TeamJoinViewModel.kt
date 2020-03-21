package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.model.User
import pl.pomocnawirus.model.repositories.FirebaseRepository

class TeamJoinViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val existingTeams = MutableLiveData<List<Team>>()

    fun checkIfTeamExists(groupId: String) =
        mFirebaseRepository.checkIfTeamExists(groupId)

    fun updateUser(user: User) =
        mFirebaseRepository.updateUserData(user)

    fun createNewTeam(team: Team) =
        mFirebaseRepository.createNewTeam(team)

    fun fetchTeams() =
        mFirebaseRepository.getAllTeams(existingTeams)
}