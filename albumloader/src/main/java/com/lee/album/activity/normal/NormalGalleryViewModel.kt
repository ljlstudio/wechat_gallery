package com.lee.album.activity.normal

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.kt.ktmvvm.basic.BaseViewModel
import com.kt.ktmvvm.basic.SingleLiveEvent
import com.lee.album.AlbumLoader
import com.lee.album.AlbumLoaderBuilder
import com.lee.album.Constants
import com.lee.album.R
import com.lee.album.activity.preview.PicturePreViewActivity
import com.lee.album.adapter.GalleryClassifyAdapter
import com.lee.album.adapter.GalleryItemAdapter
import com.lee.album.entity.AlbumData
import com.lee.album.entity.GalleryInfoEntity
import com.lee.album.inter.LoaderDataCallBack
import com.lee.album.inter.LoaderStatus
import com.lee.album.router.GalleryParam
import com.lee.album.widget.GalleryGridLayoutManager
import com.lee.album.widget.GalleryLayoutManager
import com.lee.album.widget.GridSpaceItemDecoration
import com.lee.album.widget.VerticalDrawerLayout


class NormalGalleryViewModel(application: Application) : BaseViewModel(application),
    LoaderDataCallBack,
    VerticalDrawerLayout.VerticalDrawerListener {

    var manager: ObservableField<GalleryGridLayoutManager>? =
        ObservableField(GalleryGridLayoutManager(application, 3))

    var decoration: ObservableField<GridSpaceItemDecoration>? = ObservableField(
        GridSpaceItemDecoration(
            3,
            GalleryParam.instance.listPictureRowSpace,
            GalleryParam.instance.listPictureColumnSpace
        )
    )

    var adapter: ObservableField<GalleryItemAdapter>? = ObservableField(
        GalleryItemAdapter(
            R.layout.item_gallery_layout,
            application,
            GalleryParam.instance,
            this
        )
    )

    var titleManager: ObservableField<GalleryLayoutManager>? =
        ObservableField(GalleryLayoutManager(application))


    var titleAdapter: ObservableField<GalleryClassifyAdapter>? = ObservableField(
        GalleryClassifyAdapter(
            R.layout.item_gallery_list_layout,
            this
        )
    )
    var params: SingleLiveEvent<Boolean>? = SingleLiveEvent(
    )


    var classifyLayoutEvent: SingleLiveEvent<Boolean>? = SingleLiveEvent()
    var ivCenter: ObservableField<Boolean>? = ObservableField()
    var loadStart: SingleLiveEvent<Boolean>? = SingleLiveEvent()
    var loader: AlbumLoader? = null
    var tvContent: ObservableField<String>? = ObservableField("")
    var drawerListener: ObservableField<VerticalDrawerLayout.VerticalDrawerListener>? =
        ObservableField(this)

    var scrollListener: ObservableField<RecyclerView.OnScrollListener>? =
        ObservableField()

    private var galleryParam: GalleryParam? = null
    private var pageSize: Int? = 0
    private var currentPageSize: Int? = 0
    private var lastVisiblePosition: Int? = 0;
    override fun onCreate() {
        super.onCreate()

//        scrollListener?.set(scrollerListener)
        galleryParam = GalleryParam.instance
        loader = AlbumLoader()
        loader?.setAlbumLoaderBuilder(
            AlbumLoaderBuilder().setCallBack(this).setPageSize(
                galleryParam?.pageSize
            ).setShowLastModified(true).setShouldLoadPaging(galleryParam?.shouldLoadPaging)
        )

        pageSize = galleryParam?.pageSize
        params?.postValue(true)


        checkPermission()
        setClassifyStatus(false)
    }


    private fun checkPermission() {
        loadStart?.postValue(true)
    }

    /**
     * 加载相册数据
     */
    fun loadData(context: FragmentActivity?) {

        tvContent?.set("全部图片")
        loader?.loadClassyData(context)
        loader?.loadAllListData(context)

    }


    /**
     * 实则分类列表状态
     *
     * @param open
     */
    private fun setClassifyStatus(open: Boolean) {

        ivCenter?.set(open)
        classifyLayoutEvent?.postValue(ivCenter?.get())
    }

    fun titleClick() {
        setClassifyStatus(ivCenter?.get() == false)
    }

    override fun loadClassyDataSuccess(list: List<AlbumData?>?) {
        list?.let {

            titleAdapter?.get()?.setNewInstance(list as MutableList<AlbumData>)
        }

    }

    override fun loadListDataSuccess(
        pageData: List<GalleryInfoEntity?>?,
        currentAllData: List<GalleryInfoEntity?>?
    ) {

        pageData?.let {
            currentPageSize = it.size
            Log.i(TAG, "the data size=" + pageData.size)
            adapter?.get()?.addData(pageData as MutableList<GalleryInfoEntity>)
        } ?: let {
            currentPageSize = 0
        }

    }

    override fun clearData() {

    }


    /**
     * 选中图片
     */
    fun clickPicture(galleryInfoEntity: GalleryInfoEntity?, position: Int) {
        galleryInfoEntity?.let {
            if (galleryParam?.onGalleryListener != null) {
//                galleryParam?.onGalleryListener?.clickGallery(galleryInfoEntity.imgPath, position)
                //进入预览页
                val bundle = Bundle()
                bundle.putString(Constants.KEY_PATH, galleryInfoEntity.imgPath)
                bundle.putInt(Constants.KEY_POSITION, position)
                bundle.putString(Constants.KEY_NAME, galleryInfoEntity.displayName)
                bundle.putString(Constants.KEY_ID, galleryInfoEntity.displayId)
                startActivity(PicturePreViewActivity::class.java, bundle)
            }
        }
    }

    /**
     * 点击分页列表item
     */
    fun clickClassify(data: AlbumData?, position: Int) {
        setClassifyStatus(false)
        data?.let {
            if (tvContent?.get()?.isNotEmpty() == true && tvContent?.get() == data.albumName) {
                return
            }
            tvContent?.set(data.albumName)
            adapter?.get()?.setNewInstance(null)
            loader?.loadTitleListData(getApplication(), data.albumName, data.id)
        }

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
//            loader?.resumeLoader()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (loader != null) {
            loader?.pauseLoader()
        }
    }

    companion object {
        private val TAG: String = NormalGalleryViewModel::class.java.simpleName
    }

    override fun onVerticalDrawerSlide(drawerView: View?, slideOffset: Float) {

    }

    override fun onVerticalDrawerOpened(drawerView: View?) {
        ivCenter?.set(true)
    }

    override fun onVerticalDrawerClosed(drawerView: View?) {
        ivCenter?.set(false)
    }

    override fun onVerticalDrawerStateChanged(newState: Int) {

    }


//    private var scrollerListener: RecyclerView.OnScrollListener =
//        object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//
//
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && loader?.getLoaderStatus() != LoaderStatus.LOADING) {
//
//                    if (currentPageSize!! >= pageSize!!) {
//                        loader?.loadListMore()
//                        //加载
//                    } else {
//                        //图片已经加载完毕
//                    }
//                }
//
//
//            }
//
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
////                val findFirstVisibleItemPosition = manager?.get()?.findFirstVisibleItemPosition()
////                val findLastVisibleItemPosition =
////                    manager?.get()?.findLastVisibleItemPosition()?.minus(15)
////                lastVisiblePosition = findLastVisibleItemPosition
//
//            }
//        }

}