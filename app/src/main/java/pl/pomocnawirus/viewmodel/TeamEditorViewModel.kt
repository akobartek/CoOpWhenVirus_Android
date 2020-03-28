package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.model.User
import pl.pomocnawirus.model.repositories.FirebaseRepository

class TeamEditorViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val team = MutableLiveData<Team>()

    val teamMembers = MutableLiveData<List<User>>()

    init {
        team.postValue(null)
    }

    fun fetchTeam(teamId: String) = mFirebaseRepository.fetchTeam(team, teamId)

    fun updateTeam(team: Team) =
        mFirebaseRepository.updateTeam(team, app.applicationContext)

    fun fetchTeamMembers() = mFirebaseRepository.fetchTeamMembers(teamMembers, team.value!!.id)

    fun makeNewLeader(user: User) = mFirebaseRepository.makeNewLeader(user)

    fun removeLeaderRole(user: User) = mFirebaseRepository.removeLeaderRole(user)

    fun removeUserFromTeam(user: User) = mFirebaseRepository.removeUserFromTeam(user)
}