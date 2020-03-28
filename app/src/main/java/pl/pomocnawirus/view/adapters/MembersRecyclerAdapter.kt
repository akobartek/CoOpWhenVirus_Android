package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_member.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.User

class MembersRecyclerAdapter(val showPopup: (View, User) -> Unit) :
    RecyclerView.Adapter<MembersRecyclerAdapter.MemberViewHolder>() {

    private var mMembers = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MemberViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
    )

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) =
        holder.bindView(mMembers[position])

    override fun getItemCount(): Int = mMembers.size

    fun setMembersList(list: List<User>) {
        mMembers = list
        notifyDataSetChanged()
    }

    fun getMembersList() = mMembers


    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(member: User) {
            itemView.memberNameTV.text = member.name
            itemView.leaderIcon.visibility =
                if (member.userType == User.USER_TYPE_LEADER) View.VISIBLE
                else View.GONE

            itemView.memberOptionsBtn.setOnClickListener { showPopup(it, member) }
        }
    }
}