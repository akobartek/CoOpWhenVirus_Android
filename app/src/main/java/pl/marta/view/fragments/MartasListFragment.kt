package pl.marta.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_martas_list.view.*
import kotlinx.android.synthetic.main.fragment_martas_list.view.*
import pl.marta.R
import pl.marta.view.activities.MainActivity
import pl.marta.view.adapters.MartasRecyclerAdapter
import pl.marta.viewmodel.MartasViewModel

class MartasListFragment : Fragment() {

    private lateinit var mViewModel: MartasViewModel
    private lateinit var mAdapter: MartasRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_martas_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.martasListToolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        view.martasListToolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        mViewModel = ViewModelProvider(requireActivity()).get(MartasViewModel::class.java)
        mAdapter = MartasRecyclerAdapter() { marta ->
            val editorBottomSheetFragment = MartaEditorBottomSheetFragment(marta)
            editorBottomSheetFragment.show(childFragmentManager, editorBottomSheetFragment.tag)
        }
        view.martasRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context)
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy < 0 && !view.addMartaBtn.isShown)
                        view.addMartaBtn.show()
                    else if (dy > 0 && view.addMartaBtn.isShown)
                        view.addMartaBtn.hide()
                }
            })
        }
        var teamId = arguments?.let { MartasListFragmentArgs.fromBundle(it).teamId }
        if (teamId == null)
            teamId = (requireActivity() as MainActivity).getCurrentUser()!!.teamId
        if (!teamId.isNullOrEmpty()) mViewModel.fetchMartas(teamId)
        else requireActivity().recreate()

        mViewModel.martas.observe(viewLifecycleOwner, Observer { martas ->
            mAdapter.setMartasList(martas)
            view.martasRecyclerView.scheduleLayoutAnimation()
            view.martasLoadingIndicator.hide()
            view.emptyMartasView.visibility = if (martas.isEmpty()) View.VISIBLE else View.INVISIBLE
        })

        view.addMartaBtn.setOnClickListener {
            val editorBottomSheetFragment = MartaEditorBottomSheetFragment(null)
            editorBottomSheetFragment.show(childFragmentManager, editorBottomSheetFragment.tag)
        }
    }
}
