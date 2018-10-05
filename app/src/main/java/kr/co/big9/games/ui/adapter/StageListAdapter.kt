package kr.co.big9.games.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_stage.view.*
import kr.co.big9.games.model.Stage
import kr.co.big9.games.ui.viewholder.StageViewHolder


class StageListAdapter(val stages: List<Stage>)
    : RecyclerView.Adapter<StageViewHolder>() {
    var onClick: ((View)->Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StageViewHolder =
            StageViewHolder(parent)

    override fun getItemCount(): Int = stages.count()

    override fun onBindViewHolder(holder: StageViewHolder, position: Int) {
        val stage = stages[position]
//        val score = scores[position]
        with(holder.itemView) {
            stageTextView.text = stage.stageName
//            ratingBar.rating = score.toFloat()
            onClick?.let {
                setOnClickListener(onClick)
            }
        }
    }
}