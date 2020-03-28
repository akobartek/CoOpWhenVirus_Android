package pl.pomocnawirus.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_task_order_editor.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.utils.format
import pl.pomocnawirus.view.fragments.TaskEditorBottomSheetFragment

class TaskNewRecyclerAdapter(val childFragmentManager: FragmentManager, val emptyView: View) :
    RecyclerView.Adapter<TaskNewRecyclerAdapter.TaskViewHolder>() {

    private var mTasks = arrayListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TaskViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_task_order_editor, parent, false)
    )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) =
        holder.bindView(mTasks[position], position)

    override fun getItemCount(): Int = mTasks.size

    fun setTasksList(list: ArrayList<Task>) {
        mTasks = list
        notifyDataSetChanged()
    }

    fun getTasksList() = mTasks

    fun addNewTask(task: Task) {
        if (emptyView.visibility == View.VISIBLE) emptyView.visibility = View.INVISIBLE
        mTasks.add(task)
        notifyDataSetChanged()
    }


    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(task: Task, position: Int) {
            itemView.taskDescriptionTV.text = task.description
            itemView.taskRealizationDateTV.text = task.realizationDate?.toDate()?.format() ?: ""
            itemView.taskRealizationDateTV.visibility =
                if (task.realizationDate != null) View.VISIBLE else View.GONE
            itemView.taskTypeImage.setImageResource(task.getIconDrawableId())

            itemView.setOnClickListener {
                val taskDetailsBottomSheet = TaskEditorBottomSheetFragment(task) { editedTaks ->
                    mTasks[position] = editedTaks
                    notifyItemChanged(position)
                }
                taskDetailsBottomSheet.show(childFragmentManager, taskDetailsBottomSheet.tag)
            }

            // TODO() -> Set user to task
            itemView.taskDeleteImage.setOnClickListener {
                // TODO () Show undo snackbar
                mTasks.remove(task)
                notifyDataSetChanged()
                if (mTasks.isEmpty()) emptyView.visibility = View.VISIBLE
            }
        }
    }
}