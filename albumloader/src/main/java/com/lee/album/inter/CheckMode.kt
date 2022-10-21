package com.lee.album.inter

import androidx.annotation.IntDef

@IntDef(
    flag = true,
    value = [CheckMode.SINGLE_MODE, CheckMode.MULTIPLE_MODE]
)
annotation class CheckMode {

    companion object {
        const val SINGLE_MODE = 1 //单选
        const val MULTIPLE_MODE = 2 //多选

    }
}