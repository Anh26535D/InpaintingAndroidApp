package com.dvabk.inpaintingapp;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import java.io.IOException;
import java.util.ArrayList;


public class FingerPaintImageView extends AppCompatImageView {

    private static final float TOUCH_TOLERANCE = 4;
    private float maskX, maskY;
    private Path maskPath;
    private final Paint maskPaint;
    private ArrayList<Stroke> paths = new ArrayList<>();
    private final int currentColor;
    private final int strokeWidth;
    private Bitmap maskBitmap;
    private Canvas maskCanvas;
    private final Paint maskBitmapPaint = new Paint(Paint.DITHER_FLAG);
    Bitmap imgBitmap;

    public FingerPaintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        maskPaint = new Paint();
        maskPaint.setAntiAlias(true);
        maskPaint.setDither(true);
        maskPaint.setColor(Color.GREEN);
        maskPaint.setStyle(Paint.Style.STROKE);
        maskPaint.setStrokeJoin(Paint.Join.ROUND);
        maskPaint.setStrokeCap(Paint.Cap.ROUND);
        maskPaint.setAlpha(0xff);

        currentColor = Color.GREEN;
        strokeWidth = 20;
    }

    public Bitmap getMaskBitmap() {
        return maskBitmap;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
    }

    private Bitmap resizeBitmapWithoutDistort(Bitmap bitmap, int maxWidth, int maxHeight) {
        int cur_w = bitmap.getWidth();
        int cur_h = bitmap.getHeight();
        float ratio = (float) cur_w / cur_h;
        int new_w = (int) (ratio * maxHeight);
        int new_h = (int) (maxWidth / ratio);
        if (new_h <= maxHeight) {
            return Bitmap.createScaledBitmap(bitmap, maxWidth, new_h, false);
        } else {
            return Bitmap.createScaledBitmap(bitmap, new_w, maxHeight, false);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maskBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        maskCanvas = new Canvas(maskBitmap);
    }

    public void updateImageBitmap(Bitmap bitmap) {
        this.imgBitmap = bitmap;
        this.paths = new ArrayList<Stroke>();
        maskCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.setImageBitmap(this.imgBitmap);
    }

    public void updateImageBitmap(ContentResolver contentResolver, Uri uri, int screenWidth, int screenHeight) throws IOException {
        Bitmap imgBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri);
        int maxWidth = screenWidth - 40;
        int maxHeight = (int) (screenHeight / 2);
        imgBitmap = resizeBitmapWithoutDistort(imgBitmap, maxWidth, maxHeight);
        this.imgBitmap = imgBitmap;
        this.paths = new ArrayList<Stroke>();
        this.setImageBitmap(imgBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (imgBitmap != null) {
            canvas.drawBitmap(imgBitmap, getImageMatrix(), maskBitmapPaint);
        }
        if (maskBitmap == null) {
            maskBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            maskCanvas = new Canvas(maskBitmap);
        }
        int backgroundColor = Color.TRANSPARENT;
        maskCanvas.drawColor(backgroundColor);

        for (Stroke fp : paths) {
            maskPaint.setColor(fp.color);
            maskPaint.setStrokeWidth(fp.strokeWidth);
            maskCanvas.drawPath(fp.path, maskPaint);
        }
        canvas.drawBitmap(maskBitmap, 0, 0, maskBitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y) {
        maskPath = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, maskPath);
        paths.add(fp);
        maskPath.reset();
        maskPath.moveTo(x, y);
        maskX = x;
        maskY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - maskX);
        float dy = Math.abs(y - maskY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            maskPath.quadTo(maskX, maskY, (x + maskX) / 2, (y + maskY) / 2);
            maskX = x;
            maskY = y;
        }
    }

    private void touchUp() {
        maskPath.lineTo(maskX, maskY);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}
