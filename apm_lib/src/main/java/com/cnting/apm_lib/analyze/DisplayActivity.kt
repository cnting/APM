package com.cnting.apm_lib.analyze

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.cnting.apm_lib.APM
import com.cnting.apm_lib.R
import com.cnting.apm_lib.db.IssueEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

/**
 * Created by cnting on 2023/7/31
 *
 */
class DisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apm_display)
        val recyclerView = findViewById<RecyclerView>(R.id.display_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycle.coroutineScope.launch {
            val list = APM.with().dbRepository.getAllIssue()
            recyclerView.adapter = DisplayAdapter(list) {
                val intent = Intent(this@DisplayActivity, AnalyzeActivity::class.java)
                intent.putExtra("issue", it)
                startActivity(intent)
            }
        }
    }
}

private class DisplayAdapter(
    private val list: List<IssueEntity>,
    private val onclick: (issueEntity: IssueEntity) -> Unit
) :
    RecyclerView.Adapter<DisplayAdapter.VH>() {

    private val dateFormat = SimpleDateFormat.getDateTimeInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_display_item, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onclick(item)
        }
    }

    inner class VH(view: View) : ViewHolder(view) {
        private val timeTv = view.findViewById<TextView>(R.id.itemTimeTv)
        private val typeTv = view.findViewById<TextView>(R.id.itemTypeTv)

        fun bind(issueEntity: IssueEntity) {
            timeTv.text = "日期:${dateFormat.format(issueEntity.time)}"
            typeTv.text = "类型:${issueEntity.type?.toString()}"
        }
    }
}