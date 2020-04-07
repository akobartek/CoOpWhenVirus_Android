package pl.marta.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_filter.view.*
import pl.marta.R
import pl.marta.model.Task

class FilterRecyclerAdapter(val taskTypes: ArrayList<String>) :
    RecyclerView.Adapter<FilterRecyclerAdapter.FilterViewHolder>() {

    private val allTypes = arrayListOf(
        Task.TASK_TYPE_SHOPPING, Task.TASK_TYPE_PETS, Task.TASK_TYPE_HOME, Task.TASK_TYPE_OTHER
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FilterViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
    )

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) =
        holder.bindView(allTypes[position])

    override fun getItemCount(): Int = allTypes.size


    inner class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(type: String) {
            itemView.filterOptionCheckBox.text = itemView.context.getString(
                when (type) {
                    Task.TASK_TYPE_SHOPPING -> R.string.shopping
                    Task.TASK_TYPE_PETS -> R.string.animals_care
                    Task.TASK_TYPE_HOME -> R.string.help_at_home
                    else -> R.string.other
                }
            )
            itemView.filterOptionCheckBox.isChecked = taskTypes.contains(type)

            itemView.filterOptionCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !taskTypes.contains(type)) taskTypes.add(type)
                else if (!isChecked and taskTypes.contains(type)) taskTypes.remove(type)
            }
        }
    }
}