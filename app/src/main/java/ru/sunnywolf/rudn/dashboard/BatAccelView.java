package ru.sunnywolf.rudn.dashboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by whiteraven on 9/10/17.
 */

public class BatAccelView extends View {
    private static final String TAG = BatAccelView.class.getSimpleName();

    private int mValueBattery;
    private int mValueAccel;
    private int mProgressSize;
    private int mStrokeWidth;
    private int mStrokeColorBack;
    private float mColorBatteryLow;
    private float mColorBatteryHigh;
    private float mColorBatterySaturation;
    private float[] mColorBatteryHSV = new float[3];
    private int mColorAccel;
    private int mColorAccent;
    private Paint mPaintBack = new Paint();
    private Paint mPaintBattery = new Paint();
    private Paint mPaintAccel = new Paint();
    private Paint mPaintText = new Paint();

    public BatAccelView(Context context) {
        super(context);
        setupPaints();
    }
    public BatAccelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttrs(context, attrs);
    }
    public BatAccelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(context, attrs);
    }
    public BatAccelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttrs(context, attrs);
    }

    void parseAttrs(Context context, AttributeSet attrs){
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BatAccelView,
                0,0);
        try {
            mColorAccent = getAccentColor(context);
            mProgressSize = a.getInt(R.styleable.BatAccelView_indicatorSize, 150);
            mStrokeWidth = a.getInt(R.styleable.BatAccelView_indicatorWidth, 10);
            mColorAccel = a.getInt(R.styleable.BatAccelView_colorAccel, mColorAccent);
            mColorBatteryLow = a.getFloat(R.styleable.BatAccelView_colorBatteryHSVLow, 0.0f);
            mColorBatteryHigh = a.getFloat(R.styleable.BatAccelView_colorBatteryHSVHigh, 100.0f);
            mColorBatterySaturation = a.getFloat(R.styleable.BatAccelView_colorBatteryHSVSaturation, 1.0f);
            mStrokeColorBack = a.getInt(R.styleable.BatAccelView_colorBack, Color.GRAY);
        }
        finally {
            a.recycle();
        }
        setupPaints();
    }

    void setupPaints(){
        mPaintAccel.setColor(mColorAccel);
        mPaintAccel.setStrokeWidth(mStrokeWidth);
        mPaintAccel.setStrokeCap(Paint.Cap.ROUND);
        mPaintAccel.setStyle(Paint.Style.STROKE);

        mColorBatteryHSV[1] = mColorBatterySaturation;
        mColorBatteryHSV[2] = 1.0f;
        mPaintBattery.setStrokeWidth(mStrokeWidth);
        mPaintBattery.setStrokeCap(Paint.Cap.ROUND);
        mPaintBattery.setStyle(Paint.Style.STROKE);

        mPaintBack.setColor(mStrokeColorBack);
        mPaintBack.setStrokeWidth(mStrokeWidth);
        mPaintBack.setStrokeCap(Paint.Cap.ROUND);
        mPaintBack.setStyle(Paint.Style.STROKE);

        mPaintText.setColor(Color.WHITE);
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaintText.setStrokeWidth(1);
        mPaintText.setTextSize(70);
    }

    private int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);
        drawBattery(canvas);
        drawAccel(canvas);
        drawBatteryText(canvas);
        drawAccelText(canvas);
    }

    private void drawBackground(Canvas c){
        c.drawArc(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                getWidth() - mStrokeWidth / 2,
                getHeight() - mStrokeWidth / 2,
                mProgressSize / 2 * -1,
                mProgressSize,
                false,
                mPaintBack);

        c.drawArc(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                getWidth() - mStrokeWidth / 2,
                getHeight() - mStrokeWidth / 2,
                180 - mProgressSize / 2,
                mProgressSize,
                false,
                mPaintBack);
    }
    private void drawBattery(Canvas c){
        int color;
        mColorBatteryHSV[0] =
                mColorBatteryLow + (mColorBatteryHigh - mColorBatteryLow) * mValueBattery / 100.0f;
        color = Color.HSVToColor(mColorBatteryHSV);
        mPaintBattery.setColor(color);
        c.drawArc(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                getWidth() - mStrokeWidth / 2,
                getHeight() - mStrokeWidth / 2,
                mProgressSize / 2 - mProgressSize * mValueBattery / 100,
                mProgressSize * mValueBattery / 100,
                false,
                mPaintBattery);
    }
    private void drawAccel(Canvas c){
        c.drawArc(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                getWidth() - mStrokeWidth / 2,
                getHeight() - mStrokeWidth / 2,
                180 - mProgressSize / 2,
                mProgressSize * mValueAccel / 100,
                false,
                mPaintAccel);
    }
    private void drawBatteryText(Canvas c){
        Path path = new Path();
        path.addArc(
                mStrokeWidth * 2,
                mStrokeWidth * 2,
                getWidth() - mStrokeWidth * 2,
                getHeight() - mStrokeWidth * 2,
                -30,
                90);
        c.drawTextOnPath("B a t t e r y", path, 0, 0, mPaintText);
    }
    private void drawAccelText(Canvas c){
        Path path = new Path();
        path.addArc(
                mStrokeWidth * 2,
                mStrokeWidth * 2,
                getWidth() - mStrokeWidth * 2,
                getHeight() - mStrokeWidth * 2,
                120,
                mProgressSize);
        c.drawTextOnPath("A c c e l e r a t i o n", path, 0, 0, mPaintText);
    }

    public void setBatteryValue(int value){
        mValueBattery = value > 100 ? 100 : value < 5 ? 5 : value;
        invalidate();
    }
    public void setAccelValue(int value){
        mValueAccel = value > 100 ? 100 : value < 0 ? 0 : value;
        invalidate();
    }
}
