package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_team.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Team
import pl.pomocnawirus.view.fragments.TeamFindFragment

class TeamsRecyclerAdapter(val fragment: TeamFindFragment) :
    RecyclerView.Adapter<TeamsRecyclerAdapter.TeamViewHolder>() {

    private var mTeamsList = listOf<Team>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder =
        TeamViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
        )

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) =
        holder.bindView(mTeamsList[position])

    override fun getItemCount(): Int = mTeamsList.size

    fun setTeamsList(list: List<Team>) {
        mTeamsList = list
        notifyDataSetChanged()
    }

    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(team: Team) {
            itemView.teamNameTV.text = team.name
            itemView.teamCityTV.text = team.city

            itemView.setOnClickListener {
                fragment.openTeamDetailsBottomSheet(team)
            }
        }
    }
}