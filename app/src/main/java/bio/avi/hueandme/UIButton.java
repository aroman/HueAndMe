package bio.avi.hueandme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static android.R.id.list;

/**
 * Created by avi on 11/26/16.
 */

public class UIButton extends View {

    interface OnTapListener {
        void onTap();
    }

    private OnTapListener mListener;

    private Paint mTextPaint;
    private Paint mInnerRectPaint;
    private Paint mOuterRectPaint;

    private RectF mOuterRect;
    private RectF mInnerRect;

    private Rect mTextBounds;

    private float mTextSize;
    private String mText;

    private float mStrokeWidth;
    private float mPadding;

    private Boolean mIsHover = false;

    public UIButton(Context context) {
        super(context);
        calculateStyles();
    }

    public UIButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.UIButton);

        mTextSize = attributes.getFloat(R.styleable.UIButton_textSize, 100);
        mText = attributes.getString(R.styleable.UIButton_text);

        calculateStyles();
    }

    public void setText(String text) {
        mText = text;
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    public void setOnTapListener(OnTapListener listener) {
        mListener = listener;
    }

    private void onTap() {
        if (mListener != null) {
            mListener.onTap();
        }
    }

    private void calculateStyles() {
        if (mText == null) return;
        if (mTextSize == 0) return;

        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/azo-sans-uber.ttf"));

        // Text
        mTextPaint = new Paint(paint);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(mIsHover ? Color.BLACK : Color.WHITE);
        mTextBounds = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);

        mStrokeWidth = mTextSize / 6;
        mPadding = mStrokeWidth * 2.5f;

        // Outer rect
        mOuterRectPaint = new Paint(paint);
        mOuterRectPaint.setColor(Color.WHITE);
        mOuterRectPaint.setStrokeWidth(25f);
        mOuterRect = new RectF(
                0,
                0,
                mTextBounds.width() + 2 * mPadding + 2 * mStrokeWidth,
                mTextBounds.height() + 2 * mPadding + 2 * mStrokeWidth
        );

        // Inner rect
        mInnerRectPaint = new Paint(paint);
        mInnerRectPaint.setColor(mIsHover ? Color.WHITE : Color.BLACK);
        mInnerRectPaint.setStrokeWidth(25f);
        mInnerRect = new RectF(
                mStrokeWidth,
                mStrokeWidth,
                mTextBounds.width() + 2 * mPadding + mStrokeWidth,
                mTextBounds.height() + 2 * mPadding + mStrokeWidth
        );
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOuterRect == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        setMeasuredDimension(Math.round(mOuterRect.width()), Math.round(mOuterRect.height()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        Boolean prevIsHover = mIsHover;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsHover = true;
                break;
            case MotionEvent.ACTION_UP:
                mIsHover = false;
                onTap();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }

        if (prevIsHover != mIsHover) {
            calculateStyles();

            // tell the View to redraw the Canvas
            invalidate();
            return true;
        }

        // tell the View that we handled the event
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float outerRadius = mStrokeWidth * 1.5f;
        float innerRadius = mStrokeWidth;

        canvas.drawRoundRect(mOuterRect, outerRadius, outerRadius, mOuterRectPaint);
        canvas.drawRoundRect(mInnerRect, innerRadius, innerRadius, mInnerRectPaint);
        canvas.drawText(
                mText,
                mPadding + mStrokeWidth,
                mPadding + mStrokeWidth + mTextBounds.height(),
                mTextPaint
        );
    }

}
