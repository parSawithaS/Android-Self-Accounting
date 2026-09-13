package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.colorBsh

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.databinding.ItemColorBinding

class ColorAdapter(
    private val ColorData: ArrayList<Int>, val colorEvent: ColorEvent
) : RecyclerView.Adapter<ColorAdapter.ColorHolder>() {

    inner class ColorHolder(
        private val binding: ItemColorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context, ColorData[position]
                )
            )


            itemView.setOnClickListener {
                colorEvent.onClickColor(ColorData[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemColorBinding.inflate(layoutInflater, parent, false)
        return ColorHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return ColorData.size
    }
}