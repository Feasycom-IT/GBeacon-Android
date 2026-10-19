package com.glowbird.gbeacon.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 圆形进度条
 */

public class CircleNumberProgress extends View {
    private static final String TAG = "CircleProgressBar";

    private int mMaxProgress = 100;
    private int mProgress = 30;
    private final RectF mRectF;
    private final Paint mPaint;
    private String mTxtHint1;
    private String mTxtHint2;

    public CircleNumberProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRectF = new RectF();
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();
        if (width != height) {
            int min = Math.min(width, height);
            width = min;
            height = min;
        }
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.rgb(0xe9, 0xe9, 0xe9));
        canvas.drawColor(Color.TRANSPARENT);
        int circleLineStrokeWidth = 18;
        mPaint.setStrokeWidth(circleLineStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mRectF.left = circleLineStrokeWidth / 2;
        mRectF.top = circleLineStrokeWidth / 2;
        mRectF.right = width - circleLineStrokeWidth / 2;
        mRectF.bottom = height - circleLineStrokeWidth / 2;
        canvas.drawArc(mRectF, -90, 360, false, mPaint);
        mPaint.setColor(Color.rgb(0x00, 0x78, 0xd7));
        canvas.drawArc(mRectF, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint);
        int txtStrokeWidth = 2;
        mPaint.setStrokeWidth(txtStrokeWidth);
        String text = mProgress + "%";
        int textHeight = height / 4;
        mPaint.setTextSize(textHeight);
        int textWidth = (int) mPaint.measureText(text, 0, text.length());
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, mPaint);
        if (!"".equals(mTxtHint1) && null != mTxtHint1) {
            mPaint.setStrokeWidth(txtStrokeWidth);
            text = mTxtHint1;
            textHeight = height / 8;
            mPaint.setTextSize(textHeight);
            mPaint.setColor(Color.rgb(0x99, 0x99, 0x99));
            textWidth = (int) mPaint.measureText(text, 0, text.length());
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, width / 2 - textWidth / 2, height / 4 + textHeight / 2, mPaint);
        }
        if (!"".equals(mTxtHint1) && null != mTxtHint2) {
            mPaint.setStrokeWidth(txtStrokeWidth);
            text = mTxtHint2;
            textHeight = height / 8;
            mPaint.setTextSize(textHeight);
            textWidth = (int) mPaint.measureText(text, 0, text.length());
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(text, width / 2 - textWidth / 2, 3 * height / 4 + textHeight / 2, mPaint);
        }
    }

    public int getMaxProgress() {
        return mMaxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.mMaxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        this.invalidate();
    }

    public void setProgressNotInUiThread(int progress) {
        this.mProgress = progress;
        this.postInvalidate();
    }

    public int getProgress() {
        return mProgress;
    }

    public String getmTxtHint1() {
        return mTxtHint1;
    }

    public void setmTxtHint1(String mTxtHint1) {
        this.mTxtHint1 = mTxtHint1;
    }

    public String getmTxtHint2() {
        return mTxtHint2;
    }

    public void setmTxtHint2(String mTxtHint2) {
        this.mTxtHint2 = mTxtHint2;
    }
}