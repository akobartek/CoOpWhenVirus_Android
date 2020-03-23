package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.model.repositories.FirebaseRepository

class TasksViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val team = MutableLiveData<Team>()

    fun fetchTeam(teamId: String) = mFirebaseRepository.fetchTeam(team, teamId)

    fun unregisterTeamListener() = mFirebaseRepository.unregisterTeamListener()
}