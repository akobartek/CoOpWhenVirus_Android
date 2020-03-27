package pl.pomocnawirus.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.content_order_editor.view.*
import kotlinx.android.synthetic.main.fragment_order_editor.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.model.Order
import pl.pomocnawirus.utils.isValidEmail
import pl.pomocnawirus.utils.isValidPhoneNumber
import pl.pomocnawirus.utils.showBasicAlertDialog
import pl.pomocnawirus.view.activities.MainActivity
import pl.pomocnawirus.view.adapters.TaskNewRecyclerAdapter
import pl.pomocnawirus.viewmodel.OrderEditorViewModel

class OrderEditorFragment : Fragment() {

    companion object {
        var orderChanged = false
    }

    private lateinit var mViewModel: OrderEditorViewModel
    private lateinit var mAdapter: TaskNewRecyclerAdapter
    private var mOrder: Order? = null

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
                view.needyNameET.setText(it.needyName)
                view.addressET.setText(it.address)
                view.cityET.setText(it.city)
                view.phoneET.setText(it.phone)
                view.emailET.setText(it.email)
            }
            activity?.invalidateOptionsMenu()
        }
        inflateToolbarMenu()
        mViewModel = ViewModelProvider(requireActivity()).get(OrderEditorViewModel::class.java)
        mAdapter = TaskNewRecyclerAdapter(childFragmentManager, view.taskEmptyListTV)
        view.orderTasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            adapter = mAdapter
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

        view.needyNameET.markRequiredInRed()
        view.addressET.markRequiredInRed()
        view.cityET.markRequiredInRed()
        view.phoneET.markRequiredInRed()
        view.needyNameET.setOnTouchListener(mTouchListener)
        view.addressET.setOnTouchListener(mTouchListener)
        view.cityET.setOnTouchListener(mTouchListener)
        view.phoneET.setOnTouchListener(mTouchListener)
        view.emailET.setOnTouchListener(mTouchListener)
    }

    fun onBackPressed() {
        if (orderChanged) {
            showUnsavedChangesDialog { findNavController().navigateUp() }
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showTasks() {
        if (mOrder == null) {
            view?.taskEmptyListTV?.visibility = View.VISIBLE
            return
        }
        mAdapter.setTasksList(mOrder!!.tasks)
        if (mOrder!!.tasks.isEmpty()) {
            view?.taskEmptyListTV?.visibility = View.VISIBLE
        } else {
            view?.taskEmptyListTV?.visibility = View.INVISIBLE
        }
    }

    private fun inflateToolbarMenu() {
        view?.orderDetailsToolbar?.title =
            getString(if (mOrder == null) R.string.create_new_order else R.string.edit_order)
        view?.orderDetailsToolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        view?.orderDetailsToolbar?.setNavigationOnClickListener { onBackPressed() }
        view?.orderDetailsToolbar?.inflateMenu(if (mOrder == null) R.menu.order_new_menu else R.menu.order_edit_menu)
        view?.orderDetailsToolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_save_order -> {
                    saveOrder()
                    true
                }
                else -> true
            }
        }
    }

    private fun saveOrder() {
        val needyName = view?.needyNameET?.text.toString().trim()
        val address = view?.addressET?.text.toString().trim()
        val city = view?.cityET?.text.toString().trim()
        val phoneNumber = view?.phoneET?.text.toString().trim()
        val email = view?.emailET?.text.toString().trim()

        if (!areValuesValid(needyName, address, city, phoneNumber, email)) return

        val order = mOrder ?: Order()
        order.apply {
            this.teamId = (requireActivity() as MainActivity).getCurrentUser()!!.teamId
            this.needyName = needyName
            this.address = address
            this.city = city
            this.phone = phoneNumber
            this.email = email
            this.tasks = mAdapter.getTasksList()
        }

        if (mOrder == null) mViewModel.createNewOrder(order)
        else mViewModel.updateOrder(order)

        findNavController().navigateUp()
    }

    private fun areValuesValid(
        name: String, address: String, city: String, phone: String, email: String?
    ): Boolean {
        var isValid = true

        if (name.length < 3) {
            view?.needyNameET?.error = getString(R.string.name_error_too_short)
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

    private fun showUnsavedChangesDialog(discardAction: () -> Unit) =
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.unsaved_changes_dialog_msg)
            .setCancelable(false)
            .setPositiveButton(R.string.discard) { dialog, _ ->
                dialog.dismiss()
                discardAction()
            }
            .setNegativeButton(R.string.keep_editing) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    private val mTouchListener = View.OnTouchListener { _, _ ->
        orderChanged = true
        false
    }
}
