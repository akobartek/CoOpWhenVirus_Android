package pl.marta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.marta.model.Team
import pl.marta.model.TeamSimple
import pl.marta.model.repositories.FirebaseRepository

class TeamsViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val existingTeams = MutableLiveData<List<TeamSimple>>()

    fun addUserToTeam(teamId: String) =
        mFirebaseRepository.addUserToTeam(teamId)

    fun createNewTeam(team: Team) =
        mFirebaseRepository.createNewTeam(team)

    fun fetchTeams() =
        mFirebaseRepository.getAllTeams(existingTeams)
}