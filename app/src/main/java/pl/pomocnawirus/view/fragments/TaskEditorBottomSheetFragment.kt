package pl.pomocnawirus.view.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.content_task_editor_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_task_editor_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.utils.format
import pl.pomocnawirus.utils.markRequiredInRed
import pl.pomocnawirus.utils.onDrawableEndClick
import pl.pomocnawirus.utils.setLayoutFullHeight
import java.text.SimpleDateFormat
import java.util.*

class TaskEditorBottomSheetFragment(private val mTask: Task?, val saveAction: (Task) -> Unit) :
    BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private var mRealizationDate = Date()
    private var mRealizationDateSelected = false
    private var mType = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return inflater.inflate(R.layout.fragment_task_editor_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.taskDescriptionInputLayout.markRequiredInRed()
        view.taskTypeInputLayout.markRequiredInRed()

        if (mTask == null) view.toolbarTitle.text = getString(R.string.add_task)
        else {
            mType = mTask.type
            view.toolbarTitle.text = getString(R.string.edit_task)
            view.taskDescriptionET.setText(mTask.description)
            view.taskTypeET.setText(
                when (mType) {
                    Task.TASK_TYPE_SHOPPING -> R.string.shopping
                    Task.TASK_TYPE_PETS -> R.string.animals_care
                    Task.TASK_TYPE_HOME -> R.string.help_at_home
                    else -> R.string.other
                }
            )
            if (mTask.realizationDate != null) {
                mRealizationDate = mTask.realizationDate!!.toDate()
                view.taskRealizationDateET.setText(mRealizationDate.format())
            }
        }

        view.taskTypeET.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.task_type_popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.task_type_shopping -> {
                        mType = Task.TASK_TYPE_SHOPPING
                        view.taskTypeET.setText(R.string.shopping)
                        true
                    }
                    R.id.task_type_pets -> {
                        mType = Task.TASK_TYPE_PETS
                        view.taskTypeET.setText(R.string.animals_care)
                        true
                    }
                    R.id.task_type_home -> {
                        mType = Task.TASK_TYPE_HOME
                        view.taskTypeET.setText(R.string.help_at_home)
                        true
                    }
                    R.id.task_type_other -> {
                        mType = Task.TASK_TYPE_OTHER
                        view.taskTypeET.setText(R.string.other)
                        true
                    }
                    else -> true
                }
            }
            popupMenu.show()
        }
        view.taskRealizationDateET.setOnClickListener(mDateClickListener)
        view.taskRealizationDateET.onDrawableEndClick { mRealizationDateSelected = false }

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarSaveTaskBtn.setOnClickListener {
            val description = view.taskDescriptionET.text.toString().trim()

            var isValid = true
            if (description.isEmpty()) {
                view.taskDescriptionET.error = getString(R.string.task_description_empty_error)
                isValid = false
            }
            if (mType.isEmpty()) {
                view.taskDescriptionET.error = getString(R.string.task_type_empty_error)
                isValid = false
            }
            if (!isValid) return@setOnClickListener

            val returnTask = mTask ?: Task()
            returnTask.description = description
            returnTask.type = mType
            if (mRealizationDateSelected) returnTask.realizationDate = Timestamp(mRealizationDate)
            else returnTask.realizationDate = null
            dismiss()
            saveAction(returnTask)
        }
    }

    private val mDateClickListener = View.OnClickListener {
        (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        val calendar = Calendar.getInstance()
        calendar.time = mRealizationDate
        DatePickerDialog(
            context!!, mDateListener, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private val mDateListener =
        DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val dateString = StringBuilder()
                .append(day).append(".").append(month + 1).append(".").append(year).toString()
            mRealizationDate =
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dateString)!!
            view?.taskRealizationDateET?.setText(dateString)
            mRealizationDateSelected = true
        }
}
