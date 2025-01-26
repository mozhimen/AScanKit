package com.mozhimen.scank.face.arc.basic.mos

import com.mozhimen.serialk.gson.UtilKGsonFormat


/**
 * @ClassName PoseData
 * @Description TODO
 * @Author mozhimen / Kolin Zhao
 * @Date 2022/12/7 23:12
 * @Version 1.0
 */
class PoseData {
    private var upPitch = -15f
    private var downPitch = 15f
    private var leftRoll = 20f
    private var rightRoll = 20f
    private var rightYaw = 15f
    private var leftYaw = -15f

    constructor(upPitch: Float, downPitch: Float, leftRoll: Float, rightRoll: Float, rightYaw: Float, leftYaw: Float) {
        this.upPitch = upPitch
        this.downPitch = downPitch
        this.leftRoll = leftRoll
        this.rightRoll = rightRoll
        this.rightYaw = rightYaw
        this.leftYaw = leftYaw
    }

    fun getUpPitch(): Float {
        return upPitch
    }

    fun setUpPitch(upPitch: Float) {
        this.upPitch = upPitch
    }

    fun getDownPitch(): Float {
        return downPitch
    }

    fun setDownPitch(downPitch: Float) {
        this.downPitch = downPitch
    }

    fun getLeftRoll(): Float {
        return leftRoll
    }

    fun setLeftRoll(leftRoll: Float) {
        this.leftRoll = leftRoll
    }

    fun getRightRoll(): Float {
        return rightRoll
    }

    fun setRightRoll(rightRoll: Float) {
        this.rightRoll = rightRoll
    }

    fun getRightYaw(): Float {
        return rightYaw
    }

    fun setRightYaw(rightYaw: Float) {
        this.rightYaw = rightYaw
    }

    fun getLeftYaw(): Float {
        return leftYaw
    }

    fun setLeftYaw(leftYaw: Float) {
        this.leftYaw = leftYaw
    }

    fun getJson(): String {
        return  UtilKGsonFormat.t2strJson_ofGson(this)
    }
}