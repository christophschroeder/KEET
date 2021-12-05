package de.schroedertechnologies.keet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

public final class KiView extends View {
    private static final String TAG = KiView.class.getSimpleName();
    private Bitmap background;
    private Paint backgroundPaint;
    private Paint facePaint;
    private RectF faceRect;
    private Bitmap faceTexture;
    private float handAcceleration = 0.0f;
    private boolean handInitialized = false;
    private Paint handPaint;
    private Path handPath;
    private float handPosition = 0.0f;
    private Paint handScrewPaint;
    private float handTarget = 0.0f;
    private float handVelocity = 4.0f;
    private Handler handler;
    private long lastHandMoveTime = 0;
    private Bitmap logo;
    private Matrix logoMatrix;
    private Paint logoPaint;
    private float logoScale;
    private Paint rimCirclePaint;
    private Paint rimPaint;
    private RectF rimRect;
    private Paint rimShadowPaint;
    private Paint scalePaint;
    private RectF scaleRect;
    private Paint titlePaint;
    private Paint titlePaint2;
    private Path titlePath;
    private Path titlePath2;

    public KiView(Context context) {
        super(context);
        init();
    }

    public KiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KiView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable("superState"));
        this.handInitialized = bundle.getBoolean("handInitialized");
        this.handPosition = bundle.getFloat("handPosition");
        this.handTarget = bundle.getFloat("handTarget");
        this.handVelocity = bundle.getFloat("handVelocity");
        this.handAcceleration = bundle.getFloat("handAcceleration");
        this.lastHandMoveTime = bundle.getLong("lastHandMoveTime");
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Bundle state = new Bundle();
        state.putParcelable("superState", superState);
        state.putBoolean("handInitialized", this.handInitialized);
        state.putFloat("handPosition", this.handPosition);
        state.putFloat("handTarget", this.handTarget);
        state.putFloat("handVelocity", this.handVelocity);
        state.putFloat("handAcceleration", this.handAcceleration);
        state.putLong("lastHandMoveTime", this.lastHandMoveTime);
        return state;
    }

    private void init() {
        this.handler = new Handler();
        initDrawingTools();
    }

    private String getTitle() {
        return "Ki v." + getResources().getString(R.string.app_version);
    }

    private void initDrawingTools() {
        this.rimRect = new RectF(0.1f, 0.1f, 0.9f, 0.9f);
        this.rimPaint = new Paint();
        //this.rimPaint.setFlags(1);
        this.rimPaint.setShader(new LinearGradient(0.4f, 0.0f, 0.6f, 1.0f, Color.rgb(240, 245, 240), Color.rgb(48, 49, 48), Shader.TileMode.CLAMP));
        this.rimCirclePaint = new Paint();
        this.rimCirclePaint.setAntiAlias(true);
        this.rimCirclePaint.setStyle(Paint.Style.STROKE);
        this.rimCirclePaint.setColor(Color.argb(79, 51, 54, 51));
        this.rimCirclePaint.setStrokeWidth(0.005f);
        this.faceRect = new RectF();
        this.faceRect.set(this.rimRect.left + 0.02f, this.rimRect.top + 0.02f, this.rimRect.right - 0.02f, this.rimRect.bottom - 0.02f);
        this.faceTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.plastic);
        BitmapShader paperShader = new BitmapShader(this.faceTexture, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);
        Matrix paperMatrix = new Matrix();
        this.facePaint = new Paint();
        this.facePaint.setFilterBitmap(true);
        paperMatrix.setScale(1.0f / ((float) this.faceTexture.getWidth()), 1.0f / ((float) this.faceTexture.getHeight()));
        paperShader.setLocalMatrix(paperMatrix);
        this.facePaint.setStyle(Paint.Style.FILL);
        this.facePaint.setShader(paperShader);
        this.rimShadowPaint = new Paint();
        this.rimShadowPaint.setShader(new RadialGradient(0.5f, 0.5f, this.faceRect.width() / 2.0f, new int[]{0, 1280, 1342178560}, new float[]{0.96f, 0.96f, 0.99f}, Shader.TileMode.MIRROR));
        this.rimShadowPaint.setStyle(Paint.Style.FILL);
        this.scalePaint = new Paint();
        this.scalePaint.setStyle(Paint.Style.STROKE);
        this.scalePaint.setColor(-1627370225);
        this.scalePaint.setStrokeWidth(0.005f);
        this.scalePaint.setAntiAlias(true);
        this.scalePaint.setTextSize(0.045f);
        this.scalePaint.setTypeface(Typeface.SANS_SERIF);
        this.scalePaint.setTextScaleX(0.8f);
        this.scalePaint.setTextAlign(Paint.Align.CENTER);
        this.scaleRect = new RectF();
        this.scaleRect.set(this.faceRect.left + 0.07f, this.faceRect.top + 0.07f, this.faceRect.right - 0.07f, this.faceRect.bottom - 0.07f);
        this.titlePaint = new Paint();
        this.titlePaint.setColor(-1349230327);
        this.titlePaint.setAntiAlias(true);
        this.titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        this.titlePaint.setTextAlign(Paint.Align.CENTER);
        this.titlePaint.setTextSize(0.05f);
        this.titlePaint.setTextScaleX(0.8f);
        this.titlePath = new Path();
        this.titlePath.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f, -180.0f);
        this.titlePaint2 = new Paint();
        this.titlePath2 = new Path();
        this.logoPaint = new Paint();
        this.logoPaint.setFilterBitmap(true);
        this.logo = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.logo);
        this.logoMatrix = new Matrix();
        this.logoScale = (1.0f / ((float) this.logo.getWidth())) * 0.3f;
        this.logoMatrix.setScale(this.logoScale, this.logoScale);
        this.handPaint = new Paint();
        this.handPaint.setAntiAlias(true);
        this.handPaint.setColor(-13029588);
        this.handPaint.setShadowLayer(0.01f, -0.005f, -0.005f, 2130706432);
        this.handPaint.setStyle(Paint.Style.FILL);
        this.handPath = new Path();
        this.handPath.moveTo(0.5f, 0.7f);
        this.handPath.lineTo(0.49f, 0.69299996f);
        this.handPath.lineTo(0.498f, 0.18f);
        this.handPath.lineTo(0.502f, 0.18f);
        this.handPath.lineTo(0.51f, 0.69299996f);
        this.handPath.lineTo(0.5f, 0.7f);
        this.handPath.addCircle(0.5f, 0.5f, 0.025f, Path.Direction.CW);
        this.handScrewPaint = new Paint();
        this.handScrewPaint.setAntiAlias(true);
        this.handScrewPaint.setColor(-11976900);
        this.handScrewPaint.setStyle(Paint.Style.FILL);
        this.backgroundPaint = new Paint();
        this.backgroundPaint.setFilterBitmap(true);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
        Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));
        int chosenDimension = Math.min(chooseDimension(MeasureSpec.getMode(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec)), chooseDimension(MeasureSpec.getMode(heightMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)));
        setMeasuredDimension(chosenDimension, chosenDimension);
    }

    private int chooseDimension(int mode, int size) {
        return (mode == Integer.MIN_VALUE || mode == 1073741824) ? size : getPreferredSize();
    }

    private int getPreferredSize() {
        return 300;
    }

    private void drawRim(Canvas canvas) {
        canvas.drawOval(this.rimRect, this.rimPaint);
        canvas.drawOval(this.rimRect, this.rimCirclePaint);
    }

    private void drawFace(Canvas canvas) {
        canvas.drawOval(this.faceRect, this.facePaint);
        canvas.drawOval(this.faceRect, this.rimCirclePaint);
        canvas.drawOval(this.faceRect, this.rimShadowPaint);
    }

    private void drawScale_3g(Canvas canvas) {
        canvas.drawOval(this.scaleRect, this.scalePaint);
        canvas.save();
        float y1 = this.scaleRect.top;
        float y2 = y1 - 0.02f;
        canvas.drawText(" 0", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText(" 1", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText(" 2", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText(" 3", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText(" 4", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText(" 5", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(120.0f, 0.5f, 0.5f);
        canvas.drawText(" 1", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText(" 2", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText(" 3", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText(" 4", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText(" 5", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.restore();
    }

    private void drawScale_vor3g(Canvas canvas) {
        canvas.drawOval(this.scaleRect, this.scalePaint);
        canvas.save();
        float y1 = this.scaleRect.top;
        float y2 = y1 - 0.02f;
        canvas.drawText("10", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText("11", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText("12", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText("13", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText("14", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(-20.0f, 0.5f, 0.5f);
        canvas.drawText("15", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(120.0f, 0.5f, 0.5f);
        canvas.drawText("11", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText("12", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText("13", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText("14", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.drawText("15", 0.5f, y2 - 0.015f, this.scalePaint);
        canvas.drawLine(0.5f, y1, 0.5f, y2, this.scalePaint);
        canvas.rotate(20.0f, 0.5f, 0.5f);
        canvas.restore();
    }

    private void drawTitle(Canvas canvas) {
        getTitle();
    }

    private void drawMaschef(Canvas canvas) {
    }

    private void drawHand(Canvas canvas) {
        canvas.save();
        canvas.rotate(this.handPosition, 0.5f, 0.5f);
        canvas.drawPath(this.handPath, this.handPaint);
        canvas.restore();
        canvas.drawCircle(0.5f, 0.5f, 0.01f, this.handScrewPaint);
    }

    private void drawBackground(Canvas canvas) {
        if (this.background == null) {
            Log.w(TAG, "Background not created");
        } else {
            canvas.drawBitmap(this.background, 0.0f, 0.0f, this.backgroundPaint);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        drawBackground(canvas);
        float scale = (float) getWidth();
        canvas.save();
        canvas.scale(scale, scale);
        drawHand(canvas);
        canvas.restore();
        if (handNeedsToMove()) {
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "Size changed to " + w + "x" + h);
        regenerateBackground();
    }

    private void regenerateBackground() {
        if (this.background != null) {
            this.background.recycle();
        }
        this.background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas backgroundCanvas = new Canvas(this.background);
        float scale = (float) getWidth();
        backgroundCanvas.scale(scale, scale);
        drawRim(backgroundCanvas);
        drawFace(backgroundCanvas);
        if (Build.VERSION.SDK_INT >= 24) {
            drawScale_3g(backgroundCanvas);
        } else {
            drawScale_vor3g(backgroundCanvas);
            drawTitle(backgroundCanvas);
        }
        Log.w(TAG, "MS: VERSION.SDK_INT..." + Build.VERSION.SDK_INT);
        Canvas backgroundCanvas2 = new Canvas(this.background);
        float scale2 = (float) (getWidth() / 2);
        backgroundCanvas2.scale(scale2, scale2);
        backgroundCanvas2.translate(0.5f, 0.85f);
        this.titlePaint2.setColor(-1349230327);
        this.titlePaint2.setAntiAlias(true);
        this.titlePaint2.setTypeface(Typeface.DEFAULT_BOLD);
        this.titlePaint2.setTextAlign(Paint.Align.CENTER);
        this.titlePaint2.setTextSize(0.03f);
        this.titlePaint2.setTextScaleX(0.8f);
        this.titlePath2.addArc(new RectF(0.24f, 0.24f, 0.76f, 0.76f), -180.0f, -180.0f);
        drawMaschef(backgroundCanvas);
    }

    private boolean handNeedsToMove() {
        return Math.abs(this.handPosition - this.handTarget) > 0.01f;
    }

    /* access modifiers changed from: package-private */
    public void setKIValue(float value) {
        this.handPosition = value;
        this.handInitialized = true;
        postInvalidate();
    }
}
