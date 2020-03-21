package pl.pomocnawirus.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_team_find.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.view.adapters.TeamsRecyclerAdapter
import pl.pomocnawirus.viewmodel.TeamsViewModel

class TeamFindFragment : Fragment() {

    private lateinit var mViewModel: TeamsViewModel
    private lateinit var mAdapter: TeamsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team_find, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.teamFindToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.teamFindToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        mAdapter = TeamsRecyclerAdapter(this@TeamFindFragment)
        view.teamsRecyclerView.layoutManager = LinearLayoutManager(view.context)
        view.teamsRecyclerView.itemAnimator = DefaultItemAnimator()
        view.teamsRecyclerView.adapter = mAdapter

        mViewModel = ViewModelProvider(requireActivity()).get(TeamsViewModel::class.java)
        mViewModel.fetchTeams()
        mViewModel.existingTeams.observe(viewLifecycleOwner, Observer { teams ->
            mAdapter.setTeamsList(teams)
            view.teamsRecyclerView.scheduleLayoutAnimation()
            view.teamsLoadingIndicator.hide()
            if (teams.isEmpty()) {
                view.emptyTeamsView.visibility = View.VISIBLE
            } else {
                view.emptyTeamsView.visibility = View.INVISIBLE
            }
        })
    }

    fun openTeamDetailsBottomSheet(team: Team) {
        val teamDetailsBottomSheet = TeamDetailsBottomSheetFragment(team)
        teamDetailsBottomSheet.show(childFragmentManager, teamDetailsBottomSheet.tag)
    }
}
