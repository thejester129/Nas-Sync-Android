package com.example.android_nas_sync.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_nas_sync.R
import com.example.android_nas_sync.databinding.FragmentHomeBinding
import com.example.android_nas_sync.models.Mapping
import com.example.android_nas_sync.viewmodels.MappingsViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MappingsViewModel by activityViewModels()
    private var mappingAdapter: MappingRecyclerAdapter = MappingRecyclerAdapter(listOf()) { mapping ->
        onRecyclerItemClick(
            mapping
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        initRecycler(view)
        initAddButton(view)
        initObservers(view)

        return binding.root
    }

    private fun initRecycler(view:View){
        val mappingRecycler = view.findViewById<View>(R.id.mapping_recycler) as RecyclerView
        mappingRecycler.layoutManager = LinearLayoutManager(view.context);
        mappingRecycler.adapter = mappingAdapter
    }

    private fun initAddButton(view: View) {
        val addButton = view.findViewById<View>(R.id.mapping_add_button)
        addButton.setOnClickListener { v ->
            viewModel.currentlyEditedMapping.value = Mapping("", "", "", "")
            viewModel.canDeleteCurrentlyEdited = false
            val navController = findNavController()
            navController.navigate(R.id.MappingDialog)
        }
    }

    private fun initObservers(view:View){
        viewModel.mappings.observe(viewLifecycleOwner) { mappings ->
            mappingAdapter.updateMappings(mappings)
        }
        viewModel.unseenSnackMessages.observe(viewLifecycleOwner){ messages ->
            if(messages.isNotEmpty()){
                messages.forEach { message ->
                    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
                }
                viewModel.unseenSnackMessages.value = mutableListOf()
            }
        }
    }

    private fun onRecyclerItemClick(mapping: Mapping):Unit{
        viewModel.currentlyEditedMapping.value = Mapping(mapping)
        viewModel.canDeleteCurrentlyEdited = true
        val navController = findNavController()
        navController.navigate(R.id.MappingDialog)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}