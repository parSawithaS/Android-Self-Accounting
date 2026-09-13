package ir.androidir.SelfAccounting.ui.debtPage

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.androidir.SelfAccounting.model.dataClasses.DebtModel
import ir.androidir.SelfAccounting.databinding.ItemDebtBinding
import java.io.IOException
import java.text.DecimalFormat

class DebtAdapter(
    private val DebtData: ArrayList<DebtModel>, val debtEvent: DebtEvent
) : RecyclerView.Adapter<DebtAdapter.DebtHolder>() {

    inner class DebtHolder(
        private val binding: ItemDebtBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindData(position: Int) {
            val decimalFormat = DecimalFormat("#,###")

            //general information =>
            binding.txtDebtName.text = DebtData[position].name
            binding.txtDebtValue.text = decimalFormat.format(DebtData[position].totalValue)
            binding.txtDebtTo.text = DebtData[position].debtTo
            binding.txtDebtStartDate.text = DebtData[position].date
            binding.txtDebtFinishDate.text = DebtData[position].dateFinish
            binding.txtTurns.text = DebtData[position].turnsCount!!.toString()
            binding.txtPaiedValue.text = decimalFormat.format(DebtData[position].paidValue)
            binding.txtRemainedValue.text =
                decimalFormat.format(
                    (DebtData[position].totalValue - DebtData[position].paidValue)
                )


            //clickers =>
            itemView.setOnClickListener {
                debtEvent.onClick(DebtData[position])
            }
            itemView.setOnLongClickListener {
                debtEvent.longClick(DebtData[position], adapterPosition)
                true
            }
            binding.btnDelete.setOnClickListener {
                debtEvent.longClick(DebtData[position], adapterPosition)
            }


            //precent =>
            try {
                val paidValue = DebtData[position].paidValue
                val paidPrecent =
                    (paidValue * 100) / DebtData[position].totalValue

                val remainedPrecent = 100 - paidPrecent

                binding.txtRemainedPrecent.text = "$remainedPrecent%"
                binding.txtPaiedPrecent.text = "$paidPrecent%"

                binding.txtProgress.text = "$paidPrecent%"
                binding.Progress.progress = paidPrecent.toFloat()
            } catch (e: IOException) {
                Log.e("error", e.message!!)
                binding.txtRemainedPrecent.text = "0%"
                binding.txtPaiedPrecent.text = "0%"

                binding.txtProgress.text = "0%"
                binding.Progress.progress = 0f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDebtBinding.inflate(layoutInflater, parent, false)
        return DebtHolder(binding)
    }

    override fun onBindViewHolder(holder: DebtHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount(): Int {
        return DebtData.size
    }

    fun deleteItem(item: DebtModel, position: Int) {
        DebtData.remove(item)
        notifyItemRemoved(position)
    }
}