package com.lee.album.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.lee.album.router.GalleryParam
import com.chad.library.adapter.base.BaseQuickAdapter
import com.lee.album.entity.AlbumData
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lee.album.R
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.lee.album.activity.normal.NormalGalleryViewModel
import com.lee.album.databinding.ItemGalleryLayoutBinding
import com.lee.album.databinding.ItemGalleryListLayoutBinding
import java.lang.Exception

/**
 * Author : 李嘉伦
 * e-mail : lijialun@angogo.cn
 * date   : 2021/3/2217:33
 * Package: com.qtcx.picture.gallery.list
 * desc   :
 */
class GalleryClassifyAdapter(layoutResId: Int, var model: NormalGalleryViewModel?) :
    BaseQuickAdapter<AlbumData, BaseViewHolder>(layoutResId) {


    override fun convert(holder: BaseViewHolder, item: AlbumData) {


        val binding = holder.getBinding<ItemGalleryListLayoutBinding>()
        binding?.model = model
        binding?.data = item
        binding?.position = holder.adapterPosition

        try {
            holder.setText(R.id.tv_name, item.albumName)
                .setText(R.id.tv_count, item.count.toString() + "张")
            val imageView = holder.getView<ShapeableImageView>(R.id.iv_head)
            Glide.with(context)
                .load(item.coverUri)
                .centerCrop()
                .placeholder(imageView.drawable)
                .into(imageView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        // 绑定 view
        DataBindingUtil.bind<ViewDataBinding>(viewHolder.itemView)
    }

}