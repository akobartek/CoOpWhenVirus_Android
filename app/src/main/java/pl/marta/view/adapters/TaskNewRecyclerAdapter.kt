package pl.marta.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_task_order_editor.view.*
import pl.marta.R
import pl.marta.model.Task
import pl.marta.utils.format
import pl.marta.view.fragments.TaskAssignMemberBottomSheetFragment
import pl.marta.view.fragments.TaskEditorBottomSheetFragment

class TaskNewRecyclerAdapter(
    val fragmentManager: FragmentManager, val emptyView: View, val parentView: View
) : RecyclerView.Adapter<TaskNewRecyclerAdapter.TaskViewHolder>() {

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
            itemView.taskRealizationDateTV.text = task.realizationDate.toDate().format()
            itemView.taskTypeImage.setImageResource(task.getIconDrawableId())

            itemView.setOnClickListener {
                val taskDetailsBottomSheet = TaskEditorBottomSheetFragment(task) { editedTask ->
                    mTasks[position] = editedTask
                    notifyItemChanged(position)
                }
                taskDetailsBottomSheet.show(fragmentManager, taskDetailsBottomSheet.tag)
            }

            itemView.taskOptionsBtn.setOnClickListener { view ->
                val popupMenu = PopupMenu(itemView.context, view)
                popupMenu.menuInflater.inflate(R.menu.task_options_popup_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_task_assign_user -> {
                            val membersBottomSheet = TaskAssignMemberBottomSheetFragment() { user ->
                                if (user != null) task.volunteerId = user.id
                                else task.volunteerId = ""
                            }
                            membersBottomSheet.show(fragmentManager, membersBottomSheet.tag)
                            true
                        }
                        R.id.action_task_delete -> {
                            mTasks.remove(task)
                            notifyItemRemoved(position)

                            val snackbar =
                                Snackbar.make(
                                    parentView,
                                    R.string.task_deleted,
                                    Snackbar.LENGTH_SHORT
                                )
                            snackbar.setAction(R.string.undo) {
                                mTasks.add(position, task)
                                notifyItemInserted(position)
                                if (emptyView.visibility == View.VISIBLE) emptyView.visibility =
                                    View.INVISIBLE
                            }
                            snackbar.show()
                            if (mTasks.isEmpty()) emptyView.visibility = View.VISIBLE
                            true
                        }
                        else -> true
                    }
                }
                popupMenu.show()
            }
        }
    }
}