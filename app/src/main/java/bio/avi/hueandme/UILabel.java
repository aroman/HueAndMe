package bio.avi.hueandme;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by avi on 11/27/16.
 */

public class UILabel extends View {

    private String mText = "";
    private float mTextSize = 100;
    private Paint mBasePaint;
    private Paint mFillPaint;
    private Paint mOutlinePaint;

    public UILabel(Context context) {
        super(context);
        init(context);
    }

    public UILabel(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.UILabel);

        mTextSize = attributes.getFloat(R.styleable.UILabel_labelSize, 100);
        mText = attributes.getString(R.styleable.UILabel_labelText);

        init(context);
    }

    private void init(Context context) {
        mBasePaint = new Paint();
        mBasePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mBasePaint.setTextAlign(Paint.Align.LEFT);
        mBasePaint.setColor(Color.WHITE);
        mBasePaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/azo-sans-uber.ttf"));
        calculateStyles();
    }

    private void calculateStyles() {
        if (mBasePaint == null) {
            throw new IllegalStateException("base paint is null, did you forget to call init()?");
        }

        mBasePaint.setTextSize(mTextSize);

        mFillPaint = new Paint(mBasePaint);
        mFillPaint.setColor(Color.WHITE);

        mOutlinePaint = new Paint(mBasePaint);
        mOutlinePaint.setColor(Color.BLACK);
        mOutlinePaint.setStyle(Paint.Style.STROKE);

        mOutlinePaint.setStrokeWidth(mBasePaint.getTextSize() / 20);
    }

    public void setText(String text) {
        mText = text;
        calculateStyles();
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        calculateStyles();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                Math.round(mBasePaint.measureText(mText)),
                Math.round(mBasePaint.getTextSize())
        );
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(mText, 0, mBasePaint.getTextSize(), mFillPaint);
        canvas.drawText(mText, 0, mBasePaint.getTextSize(), mOutlinePaint);
    }

}
