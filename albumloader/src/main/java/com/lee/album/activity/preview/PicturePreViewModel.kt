package com.lee.album.activity.preview

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.kt.ktmvvm.basic.BaseViewModel
import com.lee.album.AlbumLoader
import com.lee.album.AlbumLoaderBuilder
import com.lee.album.adapter.ViewPagerAdapter
import com.lee.album.entity.AlbumData
import com.lee.album.entity.GalleryInfoEntity
import com.lee.album.inter.LoaderDataCallBack
import com.lee.album.router.GalleryParam

class PicturePreViewModel(application: Application) : BaseViewModel(application),
    LoaderDataCallBack {

    companion object {
        val TAG: String = PicturePreViewModel::class.java.simpleName
    }

    var pageCurrentItem: ObservableField<Int>? = ObservableField(0)
    var adapter: ObservableField<ViewPagerAdapter>? =
        ObservableField(ViewPagerAdapter(this))
    var pageListener: ObservableField<ViewPager2.OnPageChangeCallback>? =
        ObservableField(PageListener())

    var position: Int? = 0

    inner class PageListener : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
//            val size: Int? = adapter?.get()?.data?.size
//            var currentPosition = size?.let {
//                BannerUtils.getRealPosition(
//                    isCanLoop(), position,
//                    it
//                )
//            }
//            Log.e(TAG, "the size is$position")
//            if (size!! > 0 && isCanLoop() && position == 0 || position == Int.MAX_VALUE - 1) {
//                currentPosition?.let { setCurrentItem(it) }
//            }

        }


    }

    private var loader: AlbumLoader? = null
    private var galleryParam: GalleryParam? = null


    fun initData() {
        galleryParam = GalleryParam.instance
        loader = AlbumLoader()
        loader?.setAlbumLoaderBuilder(
            AlbumLoaderBuilder().setCallBack(this).setPageSize(
                galleryParam?.pageSize
            ).setShowLastModified(true).setShouldLoadPaging(galleryParam?.shouldLoadPaging)
        )
    }


    /**
     * 加载相册数据
     */
    fun loadData(context: Context?, name: String?, id: String, position: Int) {
        this.position = position
        if (name?.isNotEmpty() == true) {

            loader?.loadTitleListData(context, name, id)
        } else {
            loader?.loadAllListData(context)
        }

        Log.i(TAG, "the load data  name=" + name + "id=" + id + "position=" + position)

    }

    override fun loadClassyDataSuccess(list: List<AlbumData?>?) {

    }

    override fun loadListDataSuccess(
        pageData: List<GalleryInfoEntity?>?,
        currentAllData: List<GalleryInfoEntity?>?
    ) {
        Log.i(TAG, "the data size" + currentAllData?.size)
        pageData?.let {
            adapter?.get()?.addData(pageData as MutableList<GalleryInfoEntity>)
//            pageCurrentItem?.set(position)
        }
    }

    override fun clearData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        if (loader != null) {
            loader?.destroyLoader()
        }
    }

    override fun onResume() {
        super.onResume()
        if (loader != null) {
            loader?.resumeLoader()
        }
    }

    override fun onStop() {
        super.onStop()
        if (loader != null) {
            loader?.pauseLoader()
        }
    }

}