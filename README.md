#### 零、前言：
##### 本文的知识点一览  
>1.自定义控件及自定义属性的写法，你也将对onMesure有更深的认识    
2.关于bitmap的简单处理，及canvas区域裁剪  
3.本文会实现两个自定义控件:`FitImageView(图片自适应)`和`BiggerView(放大镜)`，前者为后者作为铺垫。  
4.最后会介绍如何从guihub生成自己的依赖库，这样一个完整的自定义控件库便ok了。  
5.本项目源码见文尾`捷文规范`第一条


##### 实现效果一览：

>1.放大镜效果1：

![放大镜效果1.gif](https://upload-images.jianshu.io/upload_images/9414344-748958617232b4f3.gif?imageMogr2/auto-orient/strip)

>2.放大镜效果2：(使用了clipOutPath需要API26)

![放大镜效果2.gif](https://upload-images.jianshu.io/upload_images/9414344-ff733f2bd5499cbb.gif?imageMogr2/auto-orient/strip)


##### 3.该控件已做成类库(欢迎star)，使用：


```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
	dependencies {
	        implementation 'com.github.toly1994328:BiggerView:v1.01'
	}
```


---
#### 一、宽高等比例自适应的控件：FitImageView  
>一开始想做放大镜效果，没多想就继承ImageView了，后来越做越困难，bitmap的裁剪模式会影响视图中显示图片的大小。  
而View自己的的大小不变，会导致图片显示宽高捕捉困难，和图片左上角捕捉困难。  
这就会导致绘制放大图片时的定位适配困难，那么多裁剪模式，想想都崩溃。  
于是我想到，自己定义图像显示的view算了，需求是宽高按比例适应，并且View的尺寸即图片的尺寸，  
将蓝色作为背景，结果如下,你应该明白是什么意思了吧，就是既想要图片不变形，又想不要超出的背景区域：

![宽大于高.png](https://upload-images.jianshu.io/upload_images/9414344-5308d12f6f1bf129.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![高大于宽.png](https://upload-images.jianshu.io/upload_images/9414344-ad744b38415eb67e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



---
##### 1.自定义属性：


```
    <!--图片放大镜-->
    <declare-styleable name="FitImageView">
        <!--图片资源-->
        <attr name="z_fit_src" format="reference"/>
    </declare-styleable>
```

##### 2.自定义控件初始代码

```
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

    protected float scaleRateW2fit = 1;//宽度缩放适应比率
    protected float scaleRateH2fit = 1;//高度缩放适应比率
    protected int mImageW, mImageH;//图片显示的宽高

    public FitImageView(Context context) {
        this(context, null);
    }

    public FitImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO draw
    }
```

##### 3.测量及摆放：(这是核心处理)

```
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
```

##### 4.创建缩放后的bitmap及绘制
>创建的时机选择在onLayout里，因为要先测量后才能知道缩放比

```
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
```

---


#### 一、自定义控件：BiggerView

##### 1.自定义属性：attrs.xml

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!--图片放大镜-->
    <declare-styleable name="BiggerView">
        <!--半径-->
        <attr name="z_bv_radius" format="dimension"/>
        <!--边线宽-->
        <attr name="z_bv_outline_width" format="dimension"/>
        <!--进度色-->
        <attr name="z_bv_outline_color" format="color"/>
        <!--放大倍率-->
        <attr name="z_bv_rate" format="float"/>
    </declare-styleable>
</resources>
```

##### 2.初始化自定义控件

```
public class BiggerView extends FitImageView {
    private int mBvRadius = dp(30);//半径
    private int mBvOutlineWidth = 2;//边线宽

    private float rate = 4;//默认放大的倍数
    private int mBvOutlineColor = 0xffCCDCE4;//边线颜色

    private Paint mPaint;//主画笔
    private Bitmap mBiggerBitmap;//放大的图片
    private Path mPath;//剪切路径

    public BiggerView(Context context) {
        this(context, null);
    }

    public BiggerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BiggerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BiggerView);
        mBvRadius = (int) a.getDimension(R.styleable.BiggerView_z_bv_radius, mBvRadius);
        mBvOutlineWidth = (int) a.getDimension(R.styleable.BiggerView_z_bv_outline_width, mBvOutlineWidth);
        mBvOutlineColor = a.getColor(R.styleable.BiggerView_z_bv_outline_color, mBvOutlineColor);
        rate = (int) a.getFloat(R.styleable.BiggerView_z_bv_rate, rate);
        a.recycle();
        init();
    }

    private void init() {
        //初始化主画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mBvOutlineColor);
        mPaint.setStrokeWidth(mBvOutlineWidth * 2);
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        }
    }
}
```

#### 二、初级阶段
>点击的时候生成一个圆球，并随着手指移动跟随移动，松开手时消失，如图：  
这个小球就是将来展示局部放大效果的地方

![初阶效果.gif](https://upload-images.jianshu.io/upload_images/9414344-a7ab3d9439b9ea86.gif?imageMogr2/auto-orient/strip)

##### 1.添加成员变量：

```
private int mBvRadius = dp(30);//半径
private Paint mPaint;//主画笔

private float mCurX;//当前触点X
private float mCurY;//当前触点Y
private boolean isDown;//是否触摸
```

##### 2.触点的处理

```
@Override
public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            isDown = true;
            mCurX = event.getX();
            mCurY = event.getY();
            break;
        case MotionEvent.ACTION_UP:
            isDown = false;
    }
    invalidate();//记得刷新
    return true;
}
```
##### 3.绘制

```
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (isDown) {
        canvas.drawCircle(mCurX, mCurY, mBvRadius, mPaint);
    }
}
```

---


#### 三、中级阶段：(放大图片的处理)

![放大镜效果1.gif](https://upload-images.jianshu.io/upload_images/9414344-748958617232b4f3.gif?imageMogr2/auto-orient/strip)

![放大图平移到触点.png](https://upload-images.jianshu.io/upload_images/9414344-4b3f87e536599a38.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

##### 1.在onLayout时创建一个rate倍大小的Bitmap

```
@Override
protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    mBiggerBitmap = createBigBitmap(rate, mFitBitmap);
}
```

##### 2.绘制比放大后的图
>这里通过定位，将图片移至指定位置

```
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDown) {
            canvas.drawBitmap(mBiggerBitmap, -mCurX * (rate - 1), -mCurY * (rate - 1), mPaint);
        }
    }
```



>这样效果1就完成了


---
##### 3.效果2的实现：
>使用了clipOutPath的API,不须26及以上  
一开始触点是在圆的中心，这里往上调了一下(理由很简单，手指太大,把要看的部位遮住了...)  
但这有个问题，就是最上面的部分再往上就无法显示了，使用做了如下的优化：

![优化.gif](https://upload-images.jianshu.io/upload_images/9414344-0e7e2dba8422056e.gif?imageMogr2/auto-orient/strip)


```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    mShowY = -mCurY * (rate - 1) - 2 * mBvRadius;
    canvas.drawBitmap(mBiggerBitmap,
            -mCurX * (rate - 1), mShowY, mPaint);
    float rY = mCurY > 2 * mBvRadius ? mCurY - 2 * mBvRadius : mCurY +  mBvRadius;
    mPath.addCircle(mCurX, rY, mBvRadius, Path.Direction.CCW);
    canvas.clipOutPath(mPath);
    super.onDraw(canvas);
    canvas.drawCircle(mCurX, rY, mBvRadius, mPaint);
}
```

---
#### 四、高级阶段：优化点：
##### 1.使用枚举切换放大镜类型：

```
enum Style {
    NO_CLIP,//无裁剪，直接放大
    CLIP_CIRCLE,//圆形裁剪
}

@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (isDown) {
        switch (mStyle) {
            case NO_CLIP://无裁剪，直接放大
                float showY = -mCurY * (rate - 1);
                canvas.drawBitmap(mBiggerBitmap, -mCurX * (rate - 1), showY, mPaint);
                break;
            case CLIP_CIRCLE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mPath.reset();
                    showY = -mCurY * (rate - 1) - 2 * mBvRadius;
                    canvas.drawBitmap(mBiggerBitmap, -mCurX * (rate - 1), showY, mPaint);
                    float rY = mCurY > 2 * mBvRadius ? mCurY - 2 * mBvRadius : mCurY + mBvRadius;
                    mPath.addCircle(mCurX, rY, mBvRadius, Path.Direction.CCW);
                    canvas.clipOutPath(mPath);
                    super.onDraw(canvas);
                    canvas.drawCircle(mCurX, rY, mBvRadius, mPaint);
                } else {
                    mStyle = Style.NO_CLIP;//如果版本过低,无裁剪，直接放大
                    invalidate();
                }
                //可拓展更多模式....
        }
    }
}
```

##### 2.落点在图片边界区域处理：

![矩形区域校验.png](https://upload-images.jianshu.io/upload_images/9414344-b5d1c7cb2729689a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


```
@Override
public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_MOVE:
            mCurX = event.getX();
            mCurY = event.getY();
            //校验矩形区域
            isDown = judgeRectArea(mImageW / 2, mImageH / 2, mCurX, mCurY, mImageW, mImageH);
            break;
        case MotionEvent.ACTION_UP:
            isDown = false;
    }
    invalidate();//记得刷新
    return true;
}

/**
 * 判断落点是否在矩形区域
 */
public static boolean judgeRectArea(float srcX, float srcY, float dstX, float dstY, float w, float h) {
    return Math.abs(dstX - srcX) < w / 2 && Math.abs(dstY - srcY) < h / 2;
}
```
#### 五、上传github并成库
##### 0.变成库!!，变成库!!，变成库!!

![变成库.png](https://upload-images.jianshu.io/upload_images/9414344-58cb802128a63935.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---


##### 1.上传github

![上传github.png](https://upload-images.jianshu.io/upload_images/9414344-0b1c6f12ee035dcb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

##### 2.发布：

![1.png](https://upload-images.jianshu.io/upload_images/9414344-e54abdaaacdfcddd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![2.png](https://upload-images.jianshu.io/upload_images/9414344-d058c5cfcd40a767.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---
##### 3.查看：https://jitpack.io/

![see1.png](https://upload-images.jianshu.io/upload_images/9414344-e8b282419b05d06d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


##### 4.测试使用：

![使用.png](https://upload-images.jianshu.io/upload_images/9414344-aaff6c5794ddbce4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>ok，本篇完结

---

#### 后记：捷文规范
##### 1.本文成长记录及勘误表
[项目源码](https://github.com/toly1994328/BiggerView) | 日期|备注
---|---|---
[V0.1--github](https://github.com/toly1994328/BiggerView)|2018-11-17|[Android自定义控件之局部图片放大镜--BiggerView](https://www.jianshu.com/p/78394525181b)


##### 2.更多关于我

笔名 | QQ|微信|爱好
---|---|---|---|
张风捷特烈 | 1981462002|zdl1994328|语言
 [我的github](https://github.com/toly1994328)|[我的简书](https://www.jianshu.com/u/e4e52c116681)|[我的掘金](https://juejin.im/user/5b42c0656fb9a04fe727eb37)|[个人网站](http://www.toly1994.com)

##### 3.声明
>1----本文由张风捷特烈原创,转载请注明  
2----欢迎广大编程爱好者共同交流  
3----个人能力有限，如有不正之处欢迎大家批评指证，必定虚心改正   
4----看到这里，我在此感谢你的喜欢与支持

---

![icon_wx_200.png](https://upload-images.jianshu.io/upload_images/9414344-8a0c95a090041a0d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)