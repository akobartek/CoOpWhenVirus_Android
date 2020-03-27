package pl.pomocnawirus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.model.repositories.FirebaseRepository

class TeamEditorViewModel(val app: Application) : AndroidViewModel(app) {

    private val mFirebaseRepository = FirebaseRepository(app)

    val team = MutableLiveData<Team>()

    init {
        team.postValue(null)
    }

    fun fetchTeam() = mFirebaseRepository.fetchTeam(team)

    fun updateTeam(team: Team) =
        mFirebaseRepository.updateTeam(team, app.applicationContext)
}