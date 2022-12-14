package com.lee.album.activity.normal

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Application
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener
import com.kt.ktmvvm.basic.BaseViewModel
import com.kt.ktmvvm.basic.SingleLiveEvent
import com.lee.album.AlbumLoader
import com.lee.album.AlbumLoaderBuilder
import com.lee.album.Constants
import com.lee.album.R
import com.lee.album.activity.preview.PicturePreViewActivity
import com.lee.album.adapter.GalleryClassifyAdapter
import com.lee.album.adapter.GalleryItemAdapter
import com.lee.album.adapter.ViewPagerAdapter
import com.lee.album.entity.AlbumData
import com.lee.album.entity.GalleryInfoEntity
import com.lee.album.inter.CheckMode
import com.lee.album.inter.LoaderDataCallBack
import com.lee.album.router.GalleryParam
import com.lee.album.widget.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import top.zibin.luban.Luban


class NormalGalleryViewModel(application: Application) : BaseViewModel(application),
    LoaderDataCallBack,
    VerticalDrawerLayout.VerticalDrawerListener,
    DragPhotoView.OnTapListener,
    OnOutsidePhotoTapListener,
    DragPhotoView.OnExitListener {

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

    var leftFinish: SingleLiveEvent<Boolean>? = SingleLiveEvent()
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

    /**
     * --------------?????????????????????------------------------------------
     */
    var currentPosition: Int = 0
    var pageCurrentItem: ObservableField<Int>? = ObservableField(0)
    var previewAdapter: ObservableField<ViewPagerAdapter>? =
        ObservableField(ViewPagerAdapter(this))
    var pagerListener: ObservableField<ViewPager2.OnPageChangeCallback>? = ObservableField()

    var photoTouchListener: ObservableField<DragPhotoView.OnTapListener>? = ObservableField(this)
    var photoOutSideTouchListener: ObservableField<OnOutsidePhotoTapListener>? =
        ObservableField(this)
    var dragPhotoExitListener: ObservableField<DragPhotoView.OnExitListener>? =
        ObservableField(this)

    var status: SingleLiveEvent<Boolean>? = SingleLiveEvent()
    var previewCheckStatus: ObservableField<Boolean>? = ObservableField(false)
    var alpha: SingleLiveEvent<Float>? = SingleLiveEvent()

    /**
     * -------------????????????-----------------------------------------
     */

    private var checkList: ObservableField<MutableList<GalleryInfoEntity>>? =
        ObservableField(arrayListOf())

    private var checkPathList: ObservableField<MutableList<String>>? =
        ObservableField(arrayListOf())


    var tempPreview: ObservableField<Boolean>? =
        ObservableField(false)

    var checkSize: ObservableField<Int>? = ObservableField(0)
    var hasSelected: ObservableField<Boolean>? = ObservableField(false)
    var hasOrigen: ObservableField<Boolean>? = ObservableField(false)


    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "on create is" + System.currentTimeMillis())
        pagerListener?.set(PageListener())
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
     * ??????????????????
     */
    fun loadData(context: FragmentActivity?) {

        tvContent?.set("????????????")
        loader?.loadClassyData(context)
        loader?.loadAllListData(context)

    }


    /**
     * ????????????????????????
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
    ) = pageData?.let { it1 ->
        currentPageSize = it1.size
        val list = pageData as MutableList<GalleryInfoEntity>
        val loadHaveCheck = loadHaveCheck(list)
        adapter?.get()?.addData(loadHaveCheck)
        previewAdapter?.get()?.addData(loadHaveCheck)
        //?????????????????????

    } ?: let {
        currentPageSize = 0
    }

    /**
     * ?????????????????????????????????????????????
     */
    private fun loadHaveCheck(list: MutableList<GalleryInfoEntity>): MutableList<GalleryInfoEntity> {
        val data = checkList?.get()
        data?.let {
            data.forEach(action = {
                val indexOf = list.indexOf(it)
                if (indexOf != -1) {
                    val galleryInfoEntity = list[indexOf]
                    galleryInfoEntity.isSelected = true

                }
            })
        }
        return list
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

    /**
     * ????????????
     */
    fun checkPicture(position: Int, galleryInfoEntity: GalleryInfoEntity?) {
        galleryInfoEntity?.let {

            if (galleryInfoEntity.isSelected) {
                checkList?.get()?.remove(galleryInfoEntity)
                checkPathList?.get()?.remove(galleryInfoEntity.imgPath)
            } else {
                //???????????????????????????????????????
                if (addMaxCount()) return
                checkPathList?.get()?.add(galleryInfoEntity.imgPath.toString())
                checkList?.get()?.add(galleryInfoEntity)
            }
            checkSize?.set(checkList?.get()?.size)
            hasSelected?.set(checkList?.get()?.size!! > 0)
            Log.i(TAG, "check picture size =" + checkList?.get()?.size)
            galleryInfoEntity.isSelected = !galleryInfoEntity.isSelected
            adapter?.get()?.notifyItemChanged(position, GalleryItemAdapter.PLAY_LOAD_CHECK)


        }
    }

    /**
     * ????????????
     */
    private fun addMaxCount(): Boolean {
        when (galleryParam?.checkMode) {
            CheckMode.SINGLE_MODE -> {
                //??????????????????????????????????????????0,
                if (checkList?.get()?.size!! > 0) {
                    return true
                }
            }
            CheckMode.MULTIPLE_MODE -> {
                if (checkList?.get()?.size!! >= galleryParam?.checkMaxCount!!) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * ???????????????????????????
     */
    fun checkPreviewImg() {
        val data = previewAdapter?.get()?.data
        val data1 = adapter?.get()?.data

        if (data != null && data.size > currentPosition) {
            val galleryInfoEntity = data[currentPosition]
            val indexOf = data1?.indexOf(galleryInfoEntity) as Int
            if (galleryInfoEntity.isSelected) {
                previewCheckStatus?.set(false)
            } else {
                if (!addMaxCount()) {
                    previewCheckStatus?.set(true)
                }
            }
            checkPicture(indexOf, galleryInfoEntity)

        }
    }

    /**
     * ????????????
     */
    fun clickPicture(galleryInfoEntity: GalleryInfoEntity?, position: Int) {
        galleryInfoEntity?.let {
            if (galleryParam?.onGalleryListener != null) {
//                galleryParam?.onGalleryListener?.clickGallery(galleryInfoEntity.imgPath, position)
                //???????????????
                if (tempPreview?.get() == true) {
                    tempPreview?.set(false)
                    previewAdapter?.get()?.setNewInstance(adapter?.get()?.data)
                }
                goPicturePreview(position)
            }
        }
    }

    /**
     * ???????????????
     */
    private fun goPicturePreview(
        position: Int
    ) {
        pageCurrentItem?.set(position)
        val bundle = Bundle()
        bundle.putInt(Constants.KEY_POSITION, position)
        startActivity(PicturePreViewActivity::class.java, bundle)
    }


    /**
     * ??????????????????item
     */
    fun clickClassify(data: AlbumData?, position: Int) {
        setClassifyStatus(false)
        data?.let {
            if (tvContent?.get()?.isNotEmpty() == true && tvContent?.get() == data.albumName) {
                return
            }
            tvContent?.set(data.albumName)
            adapter?.get()?.setNewInstance(null)
            previewAdapter?.get()?.setNewInstance(null)
            loader?.loadTitleListData(getApplication(), data.albumName, data.id)
        }

    }


    /**
     * ????????????
     */
    fun checkOrigen() {
        hasOrigen?.set(!hasOrigen?.get()!!)
    }


    /**
     * ????????????
     */
    fun clickPreview() {

        //????????????????????????????????????????????????,???????????????????????????????????????
        tempPreview?.set(true)
        //?????????????????????????????????????????????
        previewAdapter?.get()?.setNewInstance(null)
        //???????????????????????????


        //?????????????????????list
        checkList?.get()?.let {
            val toMutableList = it.toMutableList()
            previewAdapter?.get()?.setNewInstance(toMutableList)
        }



        goPicturePreview(0)
    }


    /**
     * ???????????????
     */
    fun sendPictures() {
        checkPathList?.get()?.let {

            if (hasOrigen?.get() == true) {
                //?????????
                galleryParam?.onGalleryListener?.sendOrigenPictures(checkPathList?.get())
            } else {

                viewModelScope.launch(Dispatchers.Main) {
                    flow {
                        emit(checkPathList?.get()!!)
                    }.onStart {
                        Log.d(TAG, "start" +( Thread.currentThread()==Looper.getMainLooper().thread))
                    }.flowOn(Dispatchers.Main).map {
                        Log.d(TAG, "map" + ( Thread.currentThread()==Looper.getMainLooper().thread))
                        Luban.with(getApplication()).load(it).get()
                    }.flowOn(Dispatchers.IO)
                        .collect {
                            Log.d(TAG, "collect" + ( Thread.currentThread()==Looper.getMainLooper().thread))
                            galleryParam?.onGalleryListener?.sendCompressPictures(checkPathList?.get())
                        }

                }


            }


        }
    }


    /**
     * ???????????????
     */
    fun leftFinish() {
        leftFinish?.postValue(true)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "on cleared ")
    }

    val rect = Rect()
    var itemHeight:Int=0

    /**
     * ?????????????????????
     */
    inner class PageListener : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            val data = previewAdapter?.get()?.data
            currentPosition = position
            data?.let {
                val galleryInfoEntity = data[position]
                previewCheckStatus?.set(galleryInfoEntity.isSelected)

                val data1 = adapter?.get()?.data
                data1?.let {
                    val indexOf = it.indexOf(galleryInfoEntity)
                    Log.i(TAG, "the index of=$indexOf")
                    if (indexOf != -1 && data.size > indexOf) {
                        val viewByPosition = adapter?.get()?.getViewByPosition(indexOf, R.id.img)
                        viewByPosition?.getGlobalVisibleRect(rect)
                        viewByPosition?.height?.let {it1->
                            itemHeight=it1
                        }

                        //??????????????????
                    }
                }
            }


        }


    }


    override fun onOutsidePhotoTap(imageView: ImageView?) {
        status?.postValue(true)
    }

    override fun onExit(
        view: DragPhotoView,
        translateX: Float,
        translateY: Float,
        w: Float,
        h: Float,
        scale: Float,
        px: Float,
        py: Float
    ) {

        performExitAnimation(view, translateX, translateY, w, h, scale)
    }

    override fun onMove(mAlpha: Float) {
        alpha?.postValue(mAlpha)
    }

    override fun onTap(view: DragPhotoView?) {
        status?.postValue(true)
    }


    private fun performExitAnimation(
        view: DragPhotoView,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        scale: Float
    ) {
        view.finishAnimationCallBack()

        val array = IntArray(2) { 0 }


        val mOriginLeft = rect.left
        val mOriginTop = rect.top
        val mOriginHeight = rect.height()
        val mOriginWidth = rect.width()
        val mOriginCenterX = mOriginLeft + mOriginWidth / 2
        val mOriginCenterY = mOriginTop + mOriginHeight / 2


        Log.i(TAG, "mOriginTop=$mOriginTop")
        val currentWidth = w * scale
        val currentHeight = h * scale




        view.getLocationInWindow(array)
        val viewX: Float = w / 2 + x - currentWidth / 2 + array[0]
        val viewY: Float = h / 2 + y - currentHeight / 2 + array[1]



        view.x = viewX
        view.y = viewY


        view.setOutLine(itemHeight,mOriginWidth)




        val centerX = view.x + mOriginWidth / 2
        val centerY = view.y + mOriginHeight / 2

        val translateX = mOriginCenterX - centerX
        val translateY = mOriginCenterY - centerY


        val animatorSet = AnimatorSet()
        animatorSet.duration = 400
        animatorSet.interpolator = DecelerateInterpolator()


        val translateXAnimator: ValueAnimator = ValueAnimator.ofFloat(view.x, view.x + translateX)
        translateXAnimator.addUpdateListener { valueAnimator ->
            view.x = (valueAnimator.animatedValue as Float)
        }

        val translateYAnimator: ValueAnimator =
            ValueAnimator.ofFloat(view.y, view.y + translateY)
        translateYAnimator.addUpdateListener { valueAnimator ->
            view.y = (valueAnimator.animatedValue as Float)

        }



        animatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {}
            override fun onAnimationEnd(animator: Animator) {


                animator.removeAllListeners()
                leftFinish()
            }

            override fun onAnimationCancel(animator: Animator?) {}
            override fun onAnimationRepeat(animator: Animator?) {}
        })

        animatorSet.playTogether(translateXAnimator, translateYAnimator)
        animatorSet.start()
    }

}