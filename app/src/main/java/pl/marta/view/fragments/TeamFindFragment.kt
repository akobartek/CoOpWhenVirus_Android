package pl.marta.view.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_team_find.view.*
import pl.marta.R
import pl.marta.model.TeamSimple
import pl.marta.view.adapters.TeamsRecyclerAdapter
import pl.marta.viewmodel.TeamsViewModel

class TeamFindFragment : Fragment() {

    private lateinit var mViewModel: TeamsViewModel
    private lateinit var mAdapter: TeamsRecyclerAdapter
    private lateinit var mSearchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_team_find, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateToolbarMenu(view.teamFindToolbar)

        mAdapter = TeamsRecyclerAdapter(this@TeamFindFragment)
        view.teamsRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
        }

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

    fun onBackPressed(): Boolean {
        return if (!mSearchView.isIconified) {
            mSearchView.onActionViewCollapsed()
            false
        } else {
            true
        }
    }

    fun openTeamDetailsBottomSheet(team: TeamSimple) {
        val teamDetailsBottomSheet = TeamDetailsBottomSheetFragment(team)
        teamDetailsBottomSheet.show(childFragmentManager, teamDetailsBottomSheet.tag)
    }

    private fun inflateToolbarMenu(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        toolbar.inflateMenu(R.menu.search_menu)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchView = toolbar.menu.findItem(R.id.action_search).actionView as SearchView
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        mSearchView.maxWidth = Integer.MAX_VALUE

        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mAdapter.filter.filter(newText)
                return false
            }
        })
    }
}
