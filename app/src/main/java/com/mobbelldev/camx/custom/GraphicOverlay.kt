package com.mobbelldev.camx.custom

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.vision.CameraSource

class GraphicOverlay<T : GraphicOverlay.Graphic>(context: Context, attributeSet: AttributeSet) :
    View(context, attributeSet) {
    private val mLock = Object()
    private var mPreviewWidth: Int = 0
    private var mWidthScaleFactor = 1.0f
    private var mPreviewHeight: Int = 0
    private var mHeightScaleFactor = 1.0f
    private var mFacing = CameraSource.CAMERA_FACING_BACK
    private val mGraphics = HashSet<T>()

    abstract class Graphic(private val mOverlay: GraphicOverlay<*>) {

        abstract fun draw(canvas: Canvas)

        fun scaleX(horizontal: Float): Float {
            return horizontal * mOverlay.mWidthScaleFactor
        }

        fun scaleY(vertical: Float): Float {
            return vertical * mOverlay.mHeightScaleFactor
        }

        fun translateX(x: Float): Float {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.width - scaleX(x)
            } else {
                return scaleX(x)
            }
        }

        fun translateY(y: Float): Float {
            return scaleY(y)
        }

        fun postInvalidate() {
            mOverlay.postInvalidate()
        }
    }

    fun clear() {
        synchronized(mLock) {
            mGraphics.clear()
        }
        postInvalidate()
    }

    fun add(graphic: Graphic) {
        synchronized(mLock) {
            mGraphics.add(graphic as T)
        }
        postInvalidate()
    }

    fun remove(graphic: T) {
        synchronized(mLock) {
            mGraphics.remove(graphic)
        }
        postInvalidate()
    }

    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
        synchronized(mLock) {
            mPreviewWidth = previewWidth
            mPreviewHeight = previewHeight
            mFacing = facing
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(mLock) {
            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                mWidthScaleFactor = canvas.width.toFloat() / mPreviewWidth.toFloat()
                mHeightScaleFactor = canvas.height.toFloat() / mPreviewHeight.toFloat()
            }

            for (graphic in mGraphics) {
                graphic.draw(canvas)
            }
        }
    }
}