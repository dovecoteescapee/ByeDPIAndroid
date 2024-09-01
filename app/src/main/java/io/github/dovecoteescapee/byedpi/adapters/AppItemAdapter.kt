package io.github.dovecoteescapee.byedpi.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.Predicate
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import io.github.dovecoteescapee.byedpi.databinding.ItemAppBinding

class AppItemAdapter(
    private val context: Context,
    private val checker: Predicate<Model>,
    private val onChange: (Model, Boolean) -> Unit,
) : RecyclerView.Adapter<AppItemAdapter.ViewHolder>() {

    private val comparator = compareBy<Model?>({ !checker.test(it) }, { it?.label })

    private val list: SortedList<Model> = SortedList(Model::class.java, SortedList.BatchedCallback(object :
        SortedList.Callback<Model>() {

        override fun compare(o1: Model?, o2: Model?): Int {
            return comparator.compare(o1, o2)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun areItemsTheSame(item1: Model?, item2: Model?): Boolean =
            item1?.packageName == item2?.packageName

        override fun areContentsTheSame(oldItem: Model?, newItem: Model?): Boolean =
            oldItem == newItem

    }))

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(ItemAppBinding.inflate(LayoutInflater.from(context), parent, false))

    override fun getItemCount(): Int {
        return list.size()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.binding.apply {
            appLabel.text = model.label
            appPackage.text = model.packageName
            icon.setImageDrawable(model.icon)

            checkbox.isChecked = checker.test(model)

            root.setOnClickListener {
                val checked = !checkbox.isChecked
                checkbox.isChecked = checked
                onChange(model, checked)
            }
        }
    }

    fun setList(newList: List<Model>) {
        list.beginBatchedUpdates()
        list.clear()
        list.addAll(newList)
        list.endBatchedUpdates()
    }

    class ViewHolder(
        val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root)

    data class Model(
        val label: String,
        val packageName: String,
        val icon: Drawable
    )
}