package com.lee.album.binding

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.chrisbanes.photoview.OnPhotoTapListener
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.imageview.ShapeableImageView
import com.lee.album.widget.AutoTranslateView
import com.lee.album.widget.PreviewStatusView
import com.lee.album.widget.RotateImageView
import com.lee.album.widget.VerticalDrawerLayout
import java.util.*

object BindingAdapter {


    @BindingAdapter("bindMoreVerticalPage")
    @JvmStatic
    fun setViewVerticalPagerMorePage(viewPager2: ViewPager2, boolean: Boolean) {
        if (boolean) {
            viewPager2.apply {
                offscreenPageLimit = 3

            }
            val compositePageTransformer = CompositePageTransformer()
//            compositePageTransformer.addTransformer(ScaleInTransformer())
            compositePageTransformer.addTransformer(MarginPageTransformer(25))
            viewPager2.setPageTransformer(compositePageTransformer)
        }

    }


    @BindingAdapter("layoutManager")
    @JvmStatic
    fun setViewPagerLayoutManager(
        viewPager2: ViewPager2,
        linearLayoutManager: LinearLayoutManager
    ) {
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView

        recyclerView.apply {
            layoutManager = linearLayoutManager
        }
    }


    @BindingAdapter("bindPageListener")
    @JvmStatic
    fun setViewPagerListener(viewPager2: ViewPager2, callback: ViewPager2.OnPageChangeCallback) {
        viewPager2.registerOnPageChangeCallback(callback)
    }


    @BindingAdapter("bindPageCurrentItem")
    @JvmStatic
    fun setViewPagerCurrentItem(viewPager2: ViewPager2, currentItem: Int) {
        viewPager2.setCurrentItem(currentItem, false)
    }

    @BindingAdapter("bindViewPagerAdapter")
    @JvmStatic
    fun <T> setViewPagerAdapter(
        viewPager2: ViewPager2,
        adapter: BaseQuickAdapter<in T, BaseViewHolder>
    ) {
        viewPager2.adapter = adapter
    }

    /**
     * ------------------------------recyclerview 相关配置-------------------------------------------
     */


    @BindingAdapter("bindRcvManager")
    @JvmStatic
    fun setRecyclerLayoutManager(
        recyclerView: RecyclerView,
        layoutManager: RecyclerView.LayoutManager
    ) {
        recyclerView.layoutManager = layoutManager
    }

    @BindingAdapter("bindRcvAdapter")
    @JvmStatic
    fun <T> setRecyclerAdapter(
        recyclerView: RecyclerView,
        adapter: BaseQuickAdapter<T, BaseViewHolder>
    ) {
        recyclerView.adapter = adapter
    }

    @BindingAdapter("bindRcvItemDecoration")
    @JvmStatic
    fun setRecyclerDuration(
        recyclerView: RecyclerView,
        itemDecoration: RecyclerView.ItemDecoration
    ) {
        recyclerView.addItemDecoration(itemDecoration)
    }

    @BindingAdapter("bindRcvHashSize")
    @JvmStatic
    fun setRcvHashSize(recyclerView: RecyclerView, boolean: Boolean) {
        recyclerView.setHasFixedSize(boolean)
    }


    @BindingAdapter("bindRcvListener")
    @JvmStatic
    fun setRcvListener(recyclerView: RecyclerView, listener: RecyclerView.OnScrollListener) {
        recyclerView.addOnScrollListener(listener)
    }


    /**
     * ------------------------------other----------------------------------------------------------
     */

    @BindingAdapter("bindViewTouchListener")
    @JvmStatic
    fun setViewTouchListener(view: View, touchListener: View.OnTouchListener) {
        view.setOnTouchListener(touchListener)
    }

    @BindingAdapter("bindRcvParams")
    @JvmStatic
    fun setRcvParams(recyclerView: RecyclerView, params: ConstraintLayout.LayoutParams) {
        recyclerView.layoutParams = params
    }

    @BindingAdapter("bindIvSelected")
    @JvmStatic
    fun setImageViewSelected(imageView: ImageView, boolean: Boolean) {
        imageView.isSelected = boolean
    }


    @BindingAdapter("bindRLVisible")
    @JvmStatic
    fun setRelativeVisible(relativeLayout: VerticalDrawerLayout, boolean: Boolean) {


    }

    @BindingAdapter("bindDrawerListener")
    @JvmStatic
    fun setDrawerListener(
        verticalDrawerLayout: VerticalDrawerLayout,
        listener: VerticalDrawerLayout.VerticalDrawerListener
    ) {
        verticalDrawerLayout.setDrawerListener(listener)
    }


    @BindingAdapter("bindRotateImg")
    @JvmStatic
    fun setRotateImageViewRotate(rotateImageView: RotateImageView, boolean: Boolean) {
        rotateImageView.rotate(boolean)
    }

    @BindingAdapter("bindImgSelected")
    @JvmStatic
    fun setImageSelected(imageView: ImageView, selected: Boolean) {
        imageView.isSelected = selected
    }

    @BindingAdapter("bindTvSelected")
    @JvmStatic
    fun setTextSelected(textView: TextView, selected: Boolean) {
        textView.isSelected = selected
    }

    @BindingAdapter("bindTvContentSend")
    @JvmStatic
    fun setTextSend(textView: TextView, count: Int) {
        val name: String = if (count > 0) {
            "发送($count)"
        } else {
            "发送"
        }
        textView.text = name
    }

    @SuppressLint("ClickableViewAccessibility")
    @BindingAdapter("bindPhotoTouchListener")
    @JvmStatic
    fun setPhotoViewTouch(photoView: PhotoView,tapListener: OnPhotoTapListener){
        photoView.attacher.setOnPhotoTapListener(tapListener)
    }


    @BindingAdapter("bindStatusView")
    @JvmStatic
    fun setPhotoViewStatus(previewStatusView: PreviewStatusView,boolean: Boolean){
        previewStatusView.setStatus()
    }


}