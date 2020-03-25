package pl.pomocnawirus.view.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_task_filter_bottom_sheet.view.*
import pl.pomocnawirus.R
import pl.pomocnawirus.utils.setLayoutFullHeight

class TaskFilterBottomSheetFragment() : BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        val view = View.inflate(requireContext(), R.layout.fragment_task_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setOnShowListener { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
            requireActivity().setLayoutFullHeight(bottomSheet)
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
        view.toolbarSaveFilterBtn.setOnClickListener {
            // TODO() -> Save filters
        }

        return bottomSheetDialog
    }

}
