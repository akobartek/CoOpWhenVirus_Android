package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task_assign_member.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.User

class TaskAssignMemberRecyclerAdapter(val selectUser: (User) -> Unit) :
    RecyclerView.Adapter<TaskAssignMemberRecyclerAdapter.TaskAssignMemberViewHolder>() {

    private var mMembers = listOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TaskAssignMemberViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_task_assign_member,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: TaskAssignMemberViewHolder, position: Int) =
        holder.bindView(mMembers[position])

    override fun getItemCount(): Int = mMembers.size

    fun setMembersList(list: List<User>) {
        mMembers = list.sortedWith(compareBy({ it.userType }, { it.name }))
        notifyDataSetChanged()
    }


    inner class TaskAssignMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(member: User) {
            itemView.memberEmailTV.text = member.email
            itemView.memberNameTV.text = member.name
            itemView.leaderIcon.visibility =
                if (member.userType == User.USER_TYPE_LEADER) View.VISIBLE
                else View.GONE

            itemView.setOnClickListener { selectUser(member) }
        }
    }
}