package com.lee.album.adapter

import android.util.Log
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lee.album.R
import com.lee.album.activity.preview.PicturePreViewModel
import com.lee.album.databinding.ItemPreviewLayoutBinding
import com.lee.album.entity.GalleryInfoEntity


class ViewPagerAdapter(var model: PicturePreViewModel) :
    BaseQuickAdapter<GalleryInfoEntity, BaseViewHolder>(R.layout.item_preview_layout) {


    override fun convert(holder: BaseViewHolder, item: GalleryInfoEntity) {
        try {
            if (DataBindingUtil.getBinding<ItemPreviewLayoutBinding?>(holder.itemView) == null) {
                ItemPreviewLayoutBinding.bind(holder.itemView)
            }

            val binding = holder.getBinding<ItemPreviewLayoutBinding>()
            binding?.model = model

            binding?.photoView?.scaleType=ImageView.ScaleType.FIT_XY

            binding?.photoView?.let {
                Glide.with(context)
                    .asBitmap()
                    .load(item.imgPath)
                    .dontAnimate()
                    .fitCenter()
                    .placeholder(binding.photoView.drawable)
                    .into(it)
            }

            Log.e("TAG","IMG PATH"+item.imgPath)
        } catch (e: Exception) {
        }

    }
    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        // 绑定 view
        DataBindingUtil.bind<ViewDataBinding>(viewHolder.itemView)
    }

//    override fun getDefItemCount(): Int {
//        return Int.MAX_VALUE
//    }
//
//    override fun getItem(position: Int): String {
//        return data[position % data.size]
//    }
//
//    override fun getItemViewType(position: Int): Int {
//        var count = headerLayoutCount + data.size
//        if (count <= 0) {
//            count = 1
//        }
//        return super.getItemViewType(position % count)
//    }
}