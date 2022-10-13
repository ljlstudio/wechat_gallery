package com.lee.album.activity.normal

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
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
import com.lee.album.router.GalleryParam
import com.lee.album.widget.GalleryGridLayoutManager
import com.lee.album.widget.GalleryLayoutManager
import com.lee.album.widget.GridSpaceItemDecoration


class NormalGalleryViewModel(application: Application) : BaseViewModel(application),
    LoaderDataCallBack {

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

    var classifyLayout: ObservableField<Boolean>? = ObservableField()
    var ivCenter: ObservableField<Boolean>? = ObservableField()
    var loadStart: SingleLiveEvent<Boolean>? = SingleLiveEvent()
    var loader: AlbumLoader? = null
    var tvContent: ObservableField<String>? = ObservableField("")
    private var galleryParam: GalleryParam? = null

    override fun onCreate() {
        super.onCreate()

        galleryParam = GalleryParam.instance
        loader = AlbumLoader()
        loader?.setAlbumLoaderBuilder(
            AlbumLoaderBuilder().setCallBack(this).setPageSize(
                galleryParam?.pageSize
            ).setShowLastModified(true).setShouldLoadPaging(galleryParam?.shouldLoadPaging)
        )

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
        classifyLayout?.set(open)
        ivCenter?.set(open)
    }

    fun titleClick() {
        setClassifyStatus(classifyLayout?.get() == false)
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
            Log.i(TAG, "the data size=" + pageData.size)
            adapter?.get()?.addData(pageData as MutableList<GalleryInfoEntity>)
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
            loader?.resumeLoader()
        }
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

}