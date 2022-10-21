package com.lee.album.adapter


import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.imageview.ShapeableImageView
import com.lee.album.R
import com.lee.album.activity.normal.NormalGalleryViewModel
import com.lee.album.databinding.ItemGalleryLayoutBinding
import com.lee.album.entity.GalleryInfoEntity
import com.lee.album.router.GalleryParam
import com.lee.album.utils.Utils

/**
 * Author : 李嘉伦
 * e-mail : lijialun@angogo.cn
 * date   : 2021/2/2416:35
 * Package: com.shyz.picture.gallery
 * desc   :
 */
class GalleryItemAdapter(
    layoutResId: Int, context: Context?,
    private val galleryParam: GalleryParam?,
    var model: NormalGalleryViewModel?
) :
    BaseQuickAdapter<GalleryInfoEntity, BaseViewHolder>(layoutResId) {
    private val height: Int =
        ((Utils.getScreenWidth(context) - (galleryParam?.listPictureColumnSpace?.times(
            2
        ) ?: 0) - (galleryParam?.listPictureMargin?.times(2) ?: 0)) / 3f).toInt()

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val layoutParams = holder.getView<View>(R.id.layout).layoutParams
        layoutParams.height = height
        layoutParams.width = height
        holder.getView<View>(R.id.layout).layoutParams = layoutParams
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun convert(holder: BaseViewHolder, galleryInfoEntity: GalleryInfoEntity) {

        val binding = holder.getBinding<ItemGalleryLayoutBinding>()
        binding?.model = model
        binding?.data = galleryInfoEntity
        binding?.position = holder.adapterPosition

        holder.setVisible(R.id.check_bg, galleryInfoEntity.isSelected)
        holder.getView<ImageView>(R.id.iv_check).isSelected = galleryInfoEntity.isSelected

        val iv = holder.getView<ShapeableImageView>(R.id.img)
        Glide.with(context)
            .load(galleryInfoEntity.imgPath)
            .placeholder(
                if (galleryParam?.listPicturePlaceholder == -1) iv.drawable else galleryParam?.listPicturePlaceholder?.let {
                    context.resources.getDrawable(
                        it
                    )
                }
            )
            .into(iv)
        iv.shapeAppearanceModel =
            iv.shapeAppearanceModel.withCornerSize(
                galleryParam?.listPictureCorner?.toFloat() ?: 1.0f
            )
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        // 绑定 view
        DataBindingUtil.bind<ViewDataBinding>(viewHolder.itemView)
    }


    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {

        if (payloads.size <= 0) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val pay = payloads[0] as String
            if (pay.isNotEmpty() && pay == PLAY_LOAD_CHECK) {
                val galleryInfoEntity = data[position]
                holder.setVisible(R.id.check_bg, galleryInfoEntity.isSelected)
                holder.getView<ImageView>(R.id.iv_check).isSelected = galleryInfoEntity.isSelected
            }
        }
    }

    companion object {
        const val PLAY_LOAD_CHECK = "PLAY_LOAD_CHECK"
    }
}