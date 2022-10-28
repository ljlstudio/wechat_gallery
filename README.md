# wechat_gallery

# 仿微信相册以及图片编辑库，持续更新中....

## 此库 使用kotlin 开发、涉及架构为MVVM ,支持多选和单选、支持分页、查询速度快、无需管理生命周期问题。

先看看效果：

<img src="https://user-images.githubusercontent.com/70507884/198530353-5c94805c-a534-4e74-9776-f977814cbdb2.gif" width = "250"/>  <img src="https://user-images.githubusercontent.com/70507884/198529868-d5dfc4bb-9559-42b3-bc1a-6251f078afe3.gif" width = "250"/>   <img src="https://user-images.githubusercontent.com/70507884/198529459-4321a84b-e8ba-4999-bf68-88a43058f642.gif" width = "250"/>  






## 使用方式

* Step1 集成
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```java
	dependencies {
	        implementation 'com.github.ljlstudio:wechat_gallery:1.0.0'
	}
```

* 使用方式

```java
 GalleryEngine.from(this@MainActivity)
            .setGalleryBuilder(this@MainActivity)
            .widthListPictureMargin(2)
            .widthListPictureColumnSpace(2)
            .widthListPictureRowSpace(2)
            .widthListPictureCorner(1)
            .withShouldLoadPaging(false)
            .widthPageSize(15)
            .widthCheckMode(CheckMode.MULTIPLE_MODE)
            .widthListPicturePlaceholder(com.kt.ktmvvm.lib.R.color.color_3C3B39)
            .widthOnGalleryListener(object : OnGalleryListener {
                override fun sendOrigenPictures(list: MutableList<String>?) {
                    //原图
                }

                override fun sendCompressPictures(list: MutableList<String>?) {
                    //压缩后的图片路径
                }
            })
            .startGallery()

```

* 参数说明



|  参数   | 说明  |
|  ----  | ----  |
| listPictureCorner  | 列表图片圆角 |
| listPictureColumnSpace  | 列表图片间距 |
|listPictureRowSpace|列表行间距|
|listPictureMargin|列表左右margin(距离屏幕)|
|listPicturePlaceholder|列表展位图或颜色|
|onGalleryListener|接口回调|
|shouldLoadPaging|是否分页|
|pageSize|分页条数|
|shouldClickCloseBottomSheet|是否点击图片关闭bottomSheet(已过期)|
|canTouchDrag|是否可触摸拖拽(已过期)|
|checkMode|单选或多选|
|checkMaxCount|多选模式下最大选择数量|



### 持续更新中...会不断迭代增加图片编辑逻辑...编辑功能会包括(涂鸦笔、贴纸、文字、马赛克、滤镜、消除笔)




