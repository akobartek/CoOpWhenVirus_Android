package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.model.Task.Companion.TASK_TYPE_HOME
import pl.pomocnawirus.model.Task.Companion.TASK_TYPE_PETS
import pl.pomocnawirus.model.Task.Companion.TASK_TYPE_SHOPPING
import pl.pomocnawirus.utils.format

class TasksRecyclerAdapter(val openBottomSheetFunction: () -> Unit) :
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

            itemView.taskTypeImage.setImageResource(
                when (task.type) {
                    TASK_TYPE_SHOPPING -> R.drawable.ic_task_shopping
                    TASK_TYPE_PETS -> R.drawable.ic_task_pets
                    TASK_TYPE_HOME -> R.drawable.ic_task_home
                    else -> R.drawable.ic_task_other
                }
            )

            itemView.setOnClickListener {
                openBottomSheetFunction()
            }
        }
    }
}