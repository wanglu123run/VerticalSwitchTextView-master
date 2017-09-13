package com.viclee.verticalswitchtextview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class VerticalSwitchTextView extends TextView implements View.OnClickListener {
    private static final int DEFAULT_SWITCH_DURATION = 500;
    private static final int DEFAULT_IDLE_DURATION = 2000;
    public static final int TEXT_ALIGN_CENTER = 0;
    public static final int TEXT_ALIGN_LEFT = 1;
    public static final int TEXT_ALIGN_RIGHT = 2;
    private Context mContext;

    private List<String> lists;//会循环显示的文本内容
    private List<String> ellipsisLists;
    private int contentSize;
    private String outStr;//当前滑出的文本内容
    private String inStr;//当前滑入的文本内容
    private float textBaseY;//文本显示的baseline
    private int currentIndex = 0;//当前显示到第几个文本
    private String ellipsis;
    private float ellipsisLen = 0;

    private int switchDuaration = DEFAULT_SWITCH_DURATION;//切换时间
    private int idleDuaration = DEFAULT_IDLE_DURATION;//间隔时间
    private int switchOrientation = 0;
    private int alignment = TEXT_ALIGN_CENTER;

    /**
     * 文本中轴线X坐标
     */
    private float inTextCenterX;
    private float outTextCenterX;
    private float currentAnimatedValue = 0.0f;
    private ValueAnimator animator;

    private TextUtils.TruncateAt mEllipsize;

    private int verticalOffset = 0;
    private int mWidth;
    private int mHeight;
    private int paddingLeft = 0;
    private int paddingBottom = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;

    private Paint mPaint;

    //回调接口，用来通知调用者控件当前的状态
    public VerticalSwitchTextViewCbInterface cbInterface;

    public VerticalSwitchTextView(Context context) {
        this(context, null);
    }

    public VerticalSwitchTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSwitchTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VerticalSwitchTextView);
        try {
            switchDuaration = array.getInt(R.styleable.VerticalSwitchTextView_switchDuaration, DEFAULT_SWITCH_DURATION);
            idleDuaration = array.getInt(R.styleable.VerticalSwitchTextView_idleDuaration, DEFAULT_IDLE_DURATION);
            switchOrientation = array.getInt(R.styleable.VerticalSwitchTextView_switchOrientation, 0);
            alignment = array.getInt(R.styleable.VerticalSwitchTextView_alignment, TEXT_ALIGN_CENTER);
        } finally {
            array.recycle();
        }
        init();
    }

    private boolean isHorizontalPlay=false;//是否可以横向播放

    private void init() {
        setOnClickListener(this);
        mPaint = getPaint();
        mPaint.setTextAlign(Paint.Align.CENTER);
        ellipsis = getContext().getString(R.string.ellipsis);
        ellipsisLen = mPaint.measureText(ellipsis);//根据样式，测量这三个点的宽度
        mEllipsize = getEllipsize();

        animator = ValueAnimator.ofFloat(0f, 1f).setDuration(switchDuaration);
        animator.setStartDelay(idleDuaration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAnimatedValue = (float) animation.getAnimatedValue();
                if (currentAnimatedValue < 1.0f) {
                    invalidate();//onDraw方法重新调用
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isHorizontalPlay=false;
                endGun();
//                Log.e("VerticalSwitchTextView", "-----------------onAnimationStart-------------------");
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                currentIndex = (++currentIndex) % contentSize;
                if (cbInterface != null) {
                    cbInterface.showNext(currentIndex);//显示下一个
                }
                outStr = lists.get(currentIndex);
                inStr = lists.get((currentIndex + 1) % contentSize);
                animator.setStartDelay(idleDuaration);
                animator.start();//重新开始动画
                isHorizontalPlay=true;
                startGun();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }



    /**
     * 设置循环显示的文本内容
     *
     * @param content 内容list
     */
    public void setTextContent(List<String> content) {
        lists = content;
        if (lists == null || lists.size() == 0) {
            return;
        }
        contentSize = lists.size();

        if (contentSize > 1) {
            animator.start();
        }
    }

    private void generateEllipsisText() {
        if (ellipsisLists != null) {//防止重复计算
            return;
        }
        ellipsisLists = new ArrayList<>();
        if (lists != null && lists.size() != 0) {
            for (String item : lists) {
                int avail = mWidth - paddingLeft - paddingRight;//有效的宽度
                float remaining = avail - ellipsisLen;
                if (avail <= 0) {
                    ellipsisLists.add("");
                } else {
                    float itemWidth = mPaint.measureText(item, 0, item.length());//获取文字的宽度
                    if (itemWidth < avail) {
                        ellipsisLists.add(item);
                    } else if (remaining <= 0) {
                        ellipsisLists.add(ellipsis);
                    } else {
                        //字的总宽度比控件宽
                        int len = item.length();
                        float[] widths = new float[len];
                        mPaint.getTextWidths(item, 0, item.length(), widths);
                        if (mEllipsize == TextUtils.TruncateAt.END) {
                            float blockWidth = 0f;
                            for (int i = 0; i < len; i++) {
                                blockWidth += widths[i];
                                if (blockWidth > remaining) {
                                    ellipsisLists.add(item.substring(0, i) + ellipsis);
                                    break;
                                }
                            }
                        } else if (mEllipsize == TextUtils.TruncateAt.START) {
                            float blockWidth = 0f;
                            for (int i = len - 1; i >= 0; i--) {
                                blockWidth += widths[i];
                                if (blockWidth > remaining) {
                                    ellipsisLists.add(ellipsis + item.substring(i, len - 1));
                                    break;
                                }
                            }
                        } else if (mEllipsize == TextUtils.TruncateAt.MIDDLE) {
                            float blockWidth = 0f;
                            for (int i = 0, j = len - 1; i < j; i++, j--) {
                                blockWidth += (widths[i] + widths[j]);
                                if (blockWidth > remaining) {
                                    if (blockWidth - widths[j] < remaining) {
                                        ellipsisLists.add(item.substring(0, i + 1) + ellipsis + item.substring(j, len - 1));
                                    } else {
                                        ellipsisLists.add(item.substring(0, i) + ellipsis + item.substring(j, len - 1));
                                    }
                                    break;
                                }
                            }
                        }else if (mEllipsize == TextUtils.TruncateAt.MARQUEE) {
                            //跑马灯
                            ellipsisLists.add(item);
                        }
                    }
                }
            }
        }
        lists = ellipsisLists;
    }

    /**
     * 主要用来调整TextView的高度
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);

        Rect bounds = new Rect();
        if (contentSize <= 0) {
            return;
        }
        String text = lists.get(0);
        mPaint.getTextBounds(text, 0, text.length(), bounds);
        int textHeight = bounds.height();

        paddingLeft = getPaddingLeft();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
        paddingTop = getPaddingTop();

        if (mEllipsize != null) {
            generateEllipsisText();
        }

        startGun();
        outStr = lists.get(0);
        if (contentSize > 1) {
            inStr = lists.get(1);
        } else {
            inStr = lists.get(0);
        }

        mHeight = textHeight + paddingBottom + paddingTop;

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        //计算文字高度
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        //计算文字的baseline
        textBaseY = mHeight - (mHeight - fontHeight) / 2 - fontMetrics.bottom;

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (contentSize <= 0) {
            return;
        }
        //计算绘制的文字中心位置
        switch (alignment) {
            case TEXT_ALIGN_CENTER:
                inTextCenterX = outTextCenterX = (mWidth - paddingLeft - paddingRight) / 2 + paddingLeft;
                break;
            case TEXT_ALIGN_LEFT:
                inTextCenterX = paddingLeft + mPaint.measureText(inStr) / 2;
                outTextCenterX = paddingLeft + mPaint.measureText(outStr) / 2;
                break;
            case TEXT_ALIGN_RIGHT:
                inTextCenterX = mWidth - paddingRight - mPaint.measureText(inStr) / 2;
                outTextCenterX = mWidth - paddingRight - mPaint.measureText(outStr) / 2;
                break;
        }
        //直接使用mHeight控制文本绘制，会因为text的baseline的问题不能居中显示
        verticalOffset = Math.round(2 * textBaseY * (0.5f - currentAnimatedValue));

        if(isHorizontalPlay){
            //左右 变换x轴
            canvas.drawText(currentStr, mCoordinateX,textBaseY, mPaint);
        }else{//上下
            if (switchOrientation == 0) {//向上滚动切换
                /**
                 * 注释的是原始版的位置控制，原始版在垂直滚动时，会先将水平滚动的字归位之后再垂直滚
                 */
               /* if (verticalOffset > 0) {
                    canvas.drawText(outStr, outTextCenterX, verticalOffset, mPaint);
                } else {
                    canvas.drawText(inStr, inTextCenterX, 2 * textBaseY + verticalOffset, mPaint);
                }*/

                if (verticalOffset > 0) {
                    canvas.drawText(outStr, mCoordinateX, verticalOffset, mPaint);
                } else {
                    canvas.drawText(inStr, inTextCenterX, 2 * textBaseY + verticalOffset, mPaint);
                }
            } else {
                /*if (verticalOffset > 0) {//向下滚动切换
                    canvas.drawText(outStr, outTextCenterX, 2 * textBaseY - verticalOffset, mPaint);
                } else {
                    canvas.drawText(inStr, inTextCenterX, -verticalOffset, mPaint);
                }*/
                if (verticalOffset > 0) {//向下滚动切换
                    canvas.drawText(outStr, mCoordinateX, 2 * textBaseY - verticalOffset, mPaint);
                } else {
                    canvas.drawText(inStr, inTextCenterX, -verticalOffset, mPaint);
                }
            }
        }

    }


    /** 是否停止滚动 */
    private boolean mStopMarquee;
    private float mCoordinateX = 1280;//当前滚动位置
    private float mTextWidth;//文本宽度
    private int mScrollWidth = 1280;//滚动区域宽度
    private int speed = 1;//滚动速度
    private String currentStr = "";//当前的文字

    private float startX;//最初开始x坐标
    private float resstartX;//重新开始的x坐标 从最右边开始进入的坐标


    /**
     * 初始化横向滚动参数
     */
    private void initHorizontal() {
        currentStr = lists.get(currentIndex);
        mScrollWidth = getMeasuredWidth();
        mTextWidth = getPaint().measureText(currentStr);
    }

    protected void startGun() {
        initHorizontal();
        switch (alignment) {
            case TEXT_ALIGN_CENTER:
                mCoordinateX = (mWidth - paddingLeft - paddingRight) / 2 + paddingLeft;
                break;
            case TEXT_ALIGN_LEFT:
                mCoordinateX = paddingLeft + mTextWidth / 2;
                break;
            case TEXT_ALIGN_RIGHT:
                mCoordinateX = mWidth - paddingRight - mTextWidth / 2;
                break;
        }
        //记录x初值
        startX=mCoordinateX;
        resstartX=startX+mWidth+paddingLeft-paddingRight;
        mStopMarquee = false;
        isHorizontalPlay=true;
        if (TextUtils.isEmpty(currentStr)||mTextWidth<=mScrollWidth) {//文本宽度小于控件宽度则不滚动
            return;
        }
        if (!TextUtils.isEmpty(currentStr))
            mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    protected void endGun() {
        initHorizontal();
        isHorizontalPlay=false;
        mStopMarquee = true;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mCoordinateX < (-startX)) {//文字滚动完了，从滚动区域的右边出来   -927
                        mCoordinateX = resstartX;
                        invalidate();
                        if (!mStopMarquee) {
                            sendEmptyMessageDelayed(0, 10);
                        }
                    } else {
                        mCoordinateX -= speed;
                        invalidate();
                        if (!mStopMarquee) {
                            sendEmptyMessageDelayed(0, 10);
                        }
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public void onClick(View v) {
        if (contentSize > currentIndex) {
            if (cbInterface != null) {
                cbInterface.onItemClick(currentIndex);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStopMarquee = true;
        if (mHandler.hasMessages(0))
            mHandler.removeMessages(0);
        mContext = null;
        if (animator != null) {
            animator.cancel();
        }
    }

    //回调接口，用来通知调用者控件当前的状态,index表示开始显示哪一个文本内容
    public interface VerticalSwitchTextViewCbInterface {
        void showNext(int index);

        void onItemClick(int index);
    }

    public void setCbInterface(VerticalSwitchTextViewCbInterface cb) {
        cbInterface = cb;
    }
}
