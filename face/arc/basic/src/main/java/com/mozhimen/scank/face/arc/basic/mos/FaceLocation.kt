package com.mozhimen.scank.face.arc.basic.mos

import android.graphics.Rect
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.math.abs

/**
 * @ClassName Location
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:37
 * @Version 1.0
 */
class FaceLocation : Parcelable {
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0

    val width: Int
        get() = abs(right - left)
    val height: Int
        get() = abs(bottom - top)
    val area: Int
        get() = width * height
    val center: IntArray
        get() = intArrayOf(left + width / 2, top + height / 2)

    constructor(parcel: Parcel) {
        left = parcel.readInt()
        top = parcel.readInt()
        right = parcel.readInt()
        bottom = parcel.readInt()
    }

    constructor(left: Int, top: Int, right: Int, bottom: Int) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    fun toRect(): Rect {
        return Rect(left, top, right, bottom)
    }



    //    public Rect toRightRect(int w){
    //        return new Rect(top,w-left, bottom,w-right);
    //    }
    //
    //    public Rect toLeftRect(int w){
    //        return new Rect(bottom,w-right, top,w-left);
    //    }
    //
    //    public Rect toRightRect(int w){
    //        return new Rect(top,w-left, bottom,w-right);
    //    }
    //
    //    public Rect toLeftRect(int w){
    //        return new Rect(bottom,w-right, top,w-left);
    //    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val faceLocation = other as FaceLocation
        return left == faceLocation.left && top == faceLocation.top && right == faceLocation.right && bottom == faceLocation.bottom
    }

    override fun hashCode(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.hash(left, top, right, bottom)
        } else {
            super.hashCode()
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(left)
        dest.writeInt(top)
        dest.writeInt(right)
        dest.writeInt(bottom)
    }

    override fun toString(): String {
        return "FaceLocation(left=$left, top=$top, right=$right, bottom=$bottom)"
    }

    companion object CREATOR : Parcelable.Creator<FaceLocation> {
        override fun createFromParcel(parcel: Parcel): FaceLocation {
            return FaceLocation(parcel)
        }

        override fun newArray(size: Int): Array<FaceLocation?> {
            return arrayOfNulls(size)
        }
    }
}