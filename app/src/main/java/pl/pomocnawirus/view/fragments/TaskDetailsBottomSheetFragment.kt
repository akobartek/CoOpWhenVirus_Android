package pl.pomocnawirus.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_task_details_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_task_details_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Order
import pl.pomocnawirus.model.Task
import pl.pomocnawirus.utils.format
import pl.pomocnawirus.utils.setLayoutFullHeight
import pl.pomocnawirus.utils.showShortToast
import pl.pomocnawirus.utils.tryToRunFunctionOnInternet
import pl.pomocnawirus.viewmodel.TasksViewModel

class TaskDetailsBottomSheetFragment(private val mOrder: Order, private val mTask: Task) :
    BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mViewModel: TasksViewModel
    private lateinit var mLoadingDialog: AlertDialog

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
        return inflater.inflate(R.layout.fragment_task_details_bottom_sheet, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(TasksViewModel::class.java)
        mLoadingDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading)
            .setCancelable(false)
            .create()

        setupToolbarIcons()
        view.taskRealizationDateTV.text = mTask.realizationDate?.toDate()?.format() ?: ""
        view.taskTypeImage.setImageResource(mTask.getIconDrawableId())
        view.taskDescriptionTV.text = mTask.description
        view.taskPersonName.text = mOrder.needyName
        view.taskAddressTV.text = mOrder.getAddressFormatted()
        view.taskPhoneTV.text = mOrder.phone
        view.taskEmailTV.text = mOrder.email

        view.taskPhoneTV.visibility = if (mOrder.phone.isNotEmpty()) View.VISIBLE else View.GONE
        view.taskEmailTV.visibility = if (mOrder.email.isNotEmpty()) View.VISIBLE else View.GONE

        view.taskAddressTV.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(mOrder.getAddressFormatted()))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
        view.taskPhoneTV.setOnClickListener {
            val smsIntent = Intent(Intent.ACTION_SENDTO)
            smsIntent.data = Uri.parse("smsto:${mOrder.phone}")
            startActivity(smsIntent)
        }
        view.taskEmailTV.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mOrder.email))
            try {
                startActivity(
                    Intent.createChooser(emailIntent, getString(R.string.send_email_chooser))
                )
            } catch (ex: android.content.ActivityNotFoundException) {
                requireContext().showShortToast(R.string.send_email_error)
            }
        }

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarAcceptTaskBtn.setOnClickListener {
            changeTaskStatus(Task.TASK_STATUS_ACCEPTED)
        }
        view.toolbarAbandonTaskBtn.setOnClickListener {
            changeTaskStatus(Task.TASK_STATUS_ADDED)
        }
        view.toolbarCompleteTaskBtn.setOnClickListener {
            changeTaskStatus(Task.TASK_STATUS_COMPLETE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mLoadingDialog.isShowing) mLoadingDialog.hide()
    }

    private fun changeTaskStatus(newStatus: Int) {
        mLoadingDialog.show()
        val index = mOrder.tasks.indexOf(mTask)
        mTask.status = newStatus
        mOrder.tasks[index] = mTask
        requireActivity().tryToRunFunctionOnInternet({
            mViewModel.updateOrder(mOrder).observe(viewLifecycleOwner, Observer {
                requireContext().showShortToast(
                    when (newStatus) {
                        Task.TASK_STATUS_ADDED -> R.string.task_abandoned
                        Task.TASK_STATUS_ACCEPTED -> R.string.task_accepted
                        Task.TASK_STATUS_COMPLETE -> R.string.task_completed
                        else -> 0
                    }
                )
                setupToolbarIcons()
                if (mLoadingDialog.isShowing) mLoadingDialog.hide()
            })
        }, {
            mLoadingDialog.hide()
            requireContext().showShortToast(R.string.operation_failed_message)
        })
    }

    private fun setupToolbarIcons() {
        view?.toolbarAcceptTaskBtn?.visibility =
            if (mTask.status == Task.TASK_STATUS_ADDED) View.VISIBLE else View.GONE
        view?.toolbarAbandonTaskBtn?.visibility =
            if (mTask.status == Task.TASK_STATUS_ACCEPTED) View.VISIBLE else View.GONE
        view?.toolbarCompleteTaskBtn?.visibility =
            if (mTask.status == Task.TASK_STATUS_ACCEPTED) View.VISIBLE else View.GONE
    }
}
