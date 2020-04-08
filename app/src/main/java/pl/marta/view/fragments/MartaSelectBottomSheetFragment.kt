package pl.marta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.content_martas_list.view.*
import kotlinx.android.synthetic.main.fragment_marta_select_bottom_sheet.view.*
import pl.marta.R
import pl.marta.model.Marta
import pl.marta.utils.setLayoutFullHeight
import pl.marta.view.activities.MainActivity
import pl.marta.view.adapters.MartasRecyclerAdapter
import pl.marta.viewmodel.MartasViewModel

class MartaSelectBottomSheetFragment(val onSelect: (Marta) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mViewModel: MartasViewModel
    private lateinit var mAdapter: MartasRecyclerAdapter

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
        return inflater.inflate(R.layout.fragment_marta_select_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(MartasViewModel::class.java)
        view.emptyMartasView.setText(R.string.empty_templates_list)

        mAdapter = MartasRecyclerAdapter() { marta -> onSelect(marta) }
        view.martasRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
        }
        mViewModel.fetchMartas((requireActivity() as MainActivity).getCurrentUser()!!.teamId)
        mViewModel.martas.observe(viewLifecycleOwner, Observer { martas ->
            mAdapter.setMartasList(martas)
            view.martasRecyclerView.scheduleLayoutAnimation()
            view.martasLoadingIndicator.hide()
            view.emptyMartasView.visibility = if (martas.isEmpty()) View.VISIBLE else View.INVISIBLE
        })

        view.toolbarCancelBtn.setOnClickListener { dismiss() }
    }
}
