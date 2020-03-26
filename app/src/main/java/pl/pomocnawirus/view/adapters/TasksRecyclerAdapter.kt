package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.utils.format

class TasksRecyclerAdapter(val openBottomSheetFunction: (Task) -> Unit) :
    RecyclerView.Adapter<TasksRecyclerAdapter.TaskViewHolder>() {

    private var mTasks = listOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TaskViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
    )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bindView(mTasks[position])

    override fun getItemCount(): Int = mTasks.size

    fun setTasksList(list: List<Task>) {
        mTasks = list
        notifyDataSetChanged()
    }


    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(task: Task) {
            itemView.taskDescriptionTV.text = task.description
            itemView.taskRealizationDateTV.text = task.realizationDate?.toDate()?.format() ?: ""
            itemView.taskRealizationDateTV.visibility =
                if (task.realizationDate != null) View.VISIBLE else View.GONE

            itemView.taskTypeImage.setImageResource(task.getIconDrawableId())

            itemView.setOnClickListener {
                openBottomSheetFunction(task)
            }
        }
    }
}