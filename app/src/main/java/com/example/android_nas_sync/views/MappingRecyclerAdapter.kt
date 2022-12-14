package com.example.android_nas_sync.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_nas_sync.R
import com.example.android_nas_sync.utils.TimeUtils
import com.example.android_nas_sync.models.Mapping

class MappingRecyclerAdapter( private var mappings:List<Mapping>,
                              private var onClick: (Mapping) -> Unit) : RecyclerView.Adapter<MappingRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val source = itemView.findViewById<TextView>(R.id.mapping_recycler_item_source)!!
        val destination = itemView.findViewById<TextView>(R.id.mapping_recycler_item_destination_ip)!!
        val filesSynced = itemView.findViewById<TextView>(R.id.mapping_recycler_item_files_synced)!!
        val infoMessage = itemView.findViewById<TextView>(R.id.mapping_recycler_item_last_sync)!!
        val chevron = itemView.findViewById<View>(R.id.mapping_recycler_chevron)!!
        val loading = itemView.findViewById<View>(R.id.mapping_recycler_loading)!!
    }

    fun updateMappings(newMappings:List<Mapping>){
        this.mappings = newMappings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.mapping_recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val mapping: Mapping = mappings[position]

        viewHolder.itemView.setOnClickListener {
            run {
                onClick(mapping)
            }
        }

        val sourceText = "Source: " + mapping.sourceFolder
        viewHolder.source.text = sourceText

        val destinationText = "Destination: " + mapping.serverIp + "/" +
                mapping.destinationShare + "/" + mapping.destinationPath
        viewHolder.destination.text = destinationText

        val filesSyncedText = "${mapping.filesSynced} files synced"
        viewHolder.filesSynced.text = filesSyncedText

        val lastSyncTime =  if (mapping.lastSynced == null)  "never"
                            else TimeUtils.unixTimestampToFormattedDate(mapping.lastSynced!!)
        val lastSyncText = "Last synced: $lastSyncTime"
        if(mapping.error != null){
            viewHolder.infoMessage.text = "Error: " + mapping.error
        }
        else{
            viewHolder.infoMessage.text = lastSyncText
        }

        if(mapping.currentlySyncing){
            viewHolder.chevron.visibility = View.INVISIBLE
            viewHolder.loading.visibility = View.VISIBLE
        }
        else{
            viewHolder.chevron.visibility = View.VISIBLE
            viewHolder.loading.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return mappings.size
    }
}