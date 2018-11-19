package com.toly1994.biggerview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.toly1994.biggerview.R;


/**
 * 作者：张风捷特烈<br/>
 * 时间：2018/11/19 0019:0:14<br/>
 * 邮箱：1981462002@qq.com<br/>
 * 说明：宽高自适应图片视图
 */
public class FitImageView extends View {

    private Paint mPaint;//主画笔

    private Drawable mFitSrc;//自定义属性获取的Drawable
    private Bitmap mBitmapSrc;//源图片
    protected Bitmap mFitBitmap;//适应宽高的缩放图片

    private float scaleRateW2fit = 1;//宽度缩放适应比率
    private float scaleRateH2fit = 1;//高度缩放适应比率
    protected int mImageW, mImageH;//图片显示的宽高

    public FitImageView(Context context) {
        this(context, null);
    }

    public FitImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FitImageView);
        mFitSrc = a.getDrawable(R.styleable.FitImageView_z_fit_src);
        a.recycle();
        init();//初始化
    }

    private void init() {
        //初始化主画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapSrc = ((BitmapDrawable) mFitSrc).getBitmap();//获取图片
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mImageW = dealWidth(widthMeasureSpec);//显示图片宽
        mImageH = dealHeight(heightMeasureSpec);//显示图片高
        float bitmapWHRate = mBitmapSrc.getHeight() * 1.f / mBitmapSrc.getWidth();//图片宽高比
        if (mImageH >= mImageW) {
            mImageH = (int) (mImageW * bitmapWHRate);//宽小，以宽为基准
        } else {
            mImageW = (int) (mImageH / bitmapWHRate);//高小，以高为基准
        }
        setMeasuredDimension(mImageW, mImageH);
    }

    /**
     * @param heightMeasureSpec
     * @return
     */
    private int dealHeight(int heightMeasureSpec) {
        int result = 0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            //控件尺寸已经确定：如：
            // android:layout_height="40dp"或"match_parent"
            scaleRateH2fit = size * 1.f / mBitmapSrc.getHeight() * 1.f;
            result = size;
        } else {
            result = mBitmapSrc.getHeight();
            if (mode == MeasureSpec.AT_MOST) {//最多不超过
                result = Math.min(result, size);

            }
        }
        return result;
    }


    /**
     * @param widthMeasureSpec
     */
    private int dealWidth(int widthMeasureSpec) {
        int result = 0;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            //控件尺寸已经确定：如：
            // android:layout_XXX="40dp"或"match_parent"
            scaleRateW2fit = size * 1.f / mBitmapSrc.getWidth();
            result = size;

        } else {
            result = mBitmapSrc.getWidth();
            if (mode == MeasureSpec.AT_MOST) {//最多不超过
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mFitBitmap = createBigBitmap(Math.min(scaleRateW2fit, scaleRateH2fit), mBitmapSrc);
        mBitmapSrc = null;//原图已无用将原图置空
    }

    /**
     * 创建一个rate倍的图片
     *
     * @param rate 缩放比率
     * @param src  图片源
     * @return 缩放后的图片
     */
    protected Bitmap createBigBitmap(float rate, Bitmap src) {
        Matrix matrix = new Matrix();
        //设置变换矩阵:扩大3倍
        matrix.setValues(new float[]{
                rate, 0, 0,
                0, rate, 0,
                0, 0, 1
        });
        return Bitmap.createBitmap(src, 0, 0,
                src.getWidth(), src.getHeight(), matrix, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mFitBitmap, 0, 0, mPaint);
    }

    ////////////////////////////----------------------get set


    public Bitmap getFitBitmap() {
        return mFitBitmap;
    }

    public int getImageW() {
        return mImageW;
    }

    public int getImageH() {
        return mImageH;
    }

    public void setFitSrc(Drawable fitSrc) {
        mFitSrc = fitSrc;
    }
}
