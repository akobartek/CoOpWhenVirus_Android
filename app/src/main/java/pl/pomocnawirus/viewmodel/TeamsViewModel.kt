package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.model.TeamSimple
import pl.pomocnawirus.model.repositories.FirebaseRepository

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