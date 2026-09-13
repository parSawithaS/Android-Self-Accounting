package ir.androidir.SelfAccounting.ui.homeScreen.categoiesPage.iconBsh

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.R
import ir.androidir.SelfAccounting.databinding.ItemIconBinding

class IconAdapter(
    private val IconData: ArrayList<Int>, val iconEvent: IconEvent
) : RecyclerView.Adapter<IconAdapter.IconHolder>() {

    inner class IconHolder(
        private val binding: ItemIconBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(position: Int) {
            val sharedPreferences =
                binding.root.context.getSharedPreferences("sharedData", Context.MODE_PRIVATE)
            val theme = sharedPreferences.getString("theme", "light")!!

            binding.root.setImageResource(IconData[position])

            if (theme == "light") {
                binding.root.setColorFilter(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.black
                    )
                )
            } else {
                binding.root.setColorFilter(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.white
                    )
                )
            }


            itemView.setOnClickListener {
                iconEvent.onClickIcon(IconData[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemIconBinding.inflate(layoutInflater, parent, false)
        return IconHolder(binding)
    }

    override fun onBindViewHolder(holder: IconHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return IconData.size
    }
}