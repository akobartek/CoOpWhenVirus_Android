package pl.marta.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_order_editor.view.*
import kotlinx.android.synthetic.main.fragment_order_editor.view.*
import pl.marta.R
import pl.marta.model.Marta
import pl.marta.model.Order
import pl.marta.model.Task
import pl.marta.utils.*
import pl.marta.view.activities.MainActivity
import pl.marta.view.adapters.TaskNewRecyclerAdapter
import pl.marta.viewmodel.OrderEditorViewModel

class OrderEditorFragment : Fragment() {

    companion object {
        var orderChanged = false
    }

    private lateinit var mViewModel: OrderEditorViewModel
    private lateinit var mAdapter: TaskNewRecyclerAdapter
    private var mOrder: Order? = null
    private var mIsTemplateSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        orderChanged = false
        return inflater.inflate(R.layout.fragment_order_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            mOrder = OrderEditorFragmentArgs.fromBundle(bundle).order
            mOrder?.let {
                view.martaNameET.setText(it.martaName)
                view.addressET.setText(it.address)
                view.cityET.setText(it.city)
                view.phoneET.setText(it.phone)
                view.emailET.setText(it.email)
            }
            activity?.invalidateOptionsMenu()
        }
        inflateToolbarMenu()

        if (mOrder == null) {
            showUseTemplateDialog()
        }

        mViewModel = ViewModelProvider(requireActivity()).get(OrderEditorViewModel::class.java)
        mAdapter = TaskNewRecyclerAdapter(
            childFragmentManager, view.taskEmptyListTV, view.orderEditorParentLayout
        )
        view.orderTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0 && !view.addTaskBtn.isShown)
                        view.addTaskBtn.show()
                    else if (dy > 0 && view.addTaskBtn.isShown)
                        view.addTaskBtn.hide()
                }
            })
        }
        showTasks()

        view.addTaskBtn.setOnClickListener {
            val taskDetailsBottomSheet = TaskEditorBottomSheetFragment(null) { newTask ->
                mAdapter.addNewTask(newTask)
            }
            taskDetailsBottomSheet.show(childFragmentManager, taskDetailsBottomSheet.tag)
        }

        view.orderEditorLayout.setOnClickListener {
            (it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }
        view.martaNameInputLayout.markRequiredInRed()
        view.addressInputLayout.markRequiredInRed()
        view.cityInputLayout.markRequiredInRed()
        view.phoneInputLayout.markRequiredInRed()
        view.martaNameET.doOnTextChanged { _, _, _, _ -> orderChanged = true }
        view.addressET.doOnTextChanged { _, _, _, _ -> orderChanged = true }
        view.cityET.doOnTextChanged { _, _, _, _ -> orderChanged = true }
        view.phoneET.doOnTextChanged { _, _, _, _ -> orderChanged = true }
        view.emailET.doOnTextChanged { _, _, _, _ -> orderChanged = true }
    }

    fun onBackPressed() {
        if (orderChanged) requireContext().showUnsavedChangesDialog { findNavController().navigateUp() }
        else findNavController().navigateUp()
    }

    private fun showTasks() {
        if (mOrder == null) {
            view?.taskEmptyListTV?.visibility = View.VISIBLE
            return
        }
        val activeTasks = arrayListOf<Task>()
        activeTasks.addAll(mOrder!!.tasks.filter { it.status != Task.TASK_STATUS_COMPLETE })
        mAdapter.setTasksList(activeTasks)
        if (activeTasks.isEmpty()) {
            view?.taskEmptyListTV?.visibility = View.VISIBLE
        } else {
            view?.taskEmptyListTV?.visibility = View.INVISIBLE
        }
    }

    private fun inflateToolbarMenu() {
        view?.orderEditorToolbar?.title =
            getString(if (mOrder == null) R.string.create_new_order else R.string.edit_order)
        view?.orderEditorToolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        view?.orderEditorToolbar?.setNavigationOnClickListener { onBackPressed() }
        view?.orderEditorToolbar?.inflateMenu(if (mOrder == null) R.menu.order_new_menu else R.menu.order_edit_menu)
        view?.orderEditorToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save_order -> {
                    saveOrder()
                    true
                }
                R.id.action_delete_order -> {
                    showDeleteConfirmationDialog()
                    true
                }
                else -> true
            }
        }
    }

    private fun saveOrder() {
        val martaName = view?.martaNameET?.text.toString().trim()
        val address = view?.addressET?.text.toString().trim()
        val city = view?.cityET?.text.toString().trim()
        val phoneNumber = view?.phoneET?.text.toString().trim()
        val email = view?.emailET?.text.toString().trim()

        if (!areValuesValid(martaName, address, city, phoneNumber, email)) return

        val order = mOrder ?: Order()
        order.apply {
            this.teamId = (requireActivity() as MainActivity).getCurrentUser()!!.teamId
            this.martaName = martaName
            this.address = address
            this.city = city
            this.phone = phoneNumber
            this.email = email
            this.tasks = mAdapter.getTasksList()
        }

        if (mOrder == null) {
            if (!mIsTemplateSelected)
                showSaveTemplateDialog(Marta("", martaName, address, city, phoneNumber, email))
            mViewModel.createNewOrder(order)
        } else mViewModel.updateOrder(order)

        findNavController().navigateUp()
    }

    private fun areValuesValid(
        name: String, address: String, city: String, phone: String, email: String?
    ): Boolean {
        var isValid = true

        if (name.length < 3) {
            view?.martaNameET?.error = getString(R.string.name_error_too_short)
            isValid = false
        }
        if (address.isEmpty()) {
            view?.addressET?.error = getString(R.string.address_empty_error)
            isValid = false
        }
        if (city.isEmpty()) {
            view!!.cityET.error = getString(R.string.city_error_empty)
            isValid = false
        }
        if (phone.isEmpty()) {
            view!!.phoneET.error = getString(R.string.phone_error_empty)
            isValid = false
        } else if (!phone.isValidPhoneNumber()) {
            view!!.phoneET.error = getString(R.string.phone_error_incorrect)
            isValid = false
        }
        if (!email.isNullOrEmpty() && !email.isValidEmail()) {
            view!!.emailET.error = getString(R.string.email_error)
            isValid = false
        }
        if (mAdapter.itemCount == 0) {
            requireContext().showBasicAlertDialog(null, R.string.no_tasks_error_message)
            isValid = false
        }
        return isValid
    }

    private fun showDeleteConfirmationDialog() =
        AlertDialog.Builder(context!!)
            .setMessage(R.string.order_delete_dialog_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                dialog.dismiss()
                mViewModel.deleteOrder(mOrder!!.id)
                findNavController().navigateUp()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    private fun showUseTemplateDialog() =
        AlertDialog.Builder(context!!)
            .setMessage(R.string.use_template_dialog_message)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                val martaSelectBottomSheet = MartaSelectBottomSheetFragment() {
                    mIsTemplateSelected = true
                    view?.martaNameET?.setText(it.name)
                    view?.addressET?.setText(it.address)
                    view?.cityET?.setText(it.city)
                    view?.phoneET?.setText(it.phone)
                    view?.emailET?.setText(it.email)
                }
                martaSelectBottomSheet.show(childFragmentManager, martaSelectBottomSheet.tag)
            }
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    private fun showSaveTemplateDialog(marta: Marta) =
        AlertDialog.Builder(context!!)
            .setMessage(R.string.save_template_dialog_title)
            .setMessage(R.string.save_template_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                mViewModel.addNewMarta(marta)
            }
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
}
