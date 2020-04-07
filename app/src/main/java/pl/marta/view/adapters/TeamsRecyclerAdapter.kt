package pl.marta.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_team.view.*
import pl.marta.R
import pl.marta.model.TeamSimple
import pl.marta.view.fragments.TeamFindFragment
import java.util.*
import kotlin.collections.ArrayList

class TeamsRecyclerAdapter(val fragment: TeamFindFragment) :
    RecyclerView.Adapter<TeamsRecyclerAdapter.TeamViewHolder>(), Filterable {

    private var mTeamsList = listOf<TeamSimple>()
    private var mTeamsFilteredList = listOf<TeamSimple>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TeamViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_team, parent, false)
    )

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) =
        holder.bindView(mTeamsFilteredList[position])

    override fun getItemCount(): Int = mTeamsFilteredList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence.toString()
                mTeamsFilteredList =
                    if (query.isEmpty()) mTeamsList
                    else {
                        val filteredList = ArrayList<TeamSimple>()
                        for (index in mTeamsList.indices)
                            if (mTeamsList[index].city.toLowerCase(Locale.ROOT)
                                    .contains(query.toLowerCase(Locale.ROOT))
                            ) filteredList.add(mTeamsList[index])
                        filteredList
                    }
                val filterResults = FilterResults()
                filterResults.values = mTeamsFilteredList
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults?
            ) {
                @Suppress("UNCHECKED_CAST")
                mTeamsFilteredList = filterResults?.values as List<TeamSimple>
                notifyDataSetChanged()
            }
        }
    }

    fun setTeamsList(list: List<TeamSimple>) {
        mTeamsList = list
        mTeamsFilteredList = list
        notifyDataSetChanged()
    }


    inner class TeamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(team: TeamSimple) {
            itemView.taskDescriptionTV.text = team.name
            itemView.teamCityTV.text = team.city

            itemView.setOnClickListener {
                fragment.openTeamDetailsBottomSheet(team)
            }
        }
    }
}