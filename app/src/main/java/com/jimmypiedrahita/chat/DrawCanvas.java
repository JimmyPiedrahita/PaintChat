package com.jimmypiedrahita.chat;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
public class DrawCanvas extends View {
    float posX = 0, posY = 0;
    int R = 0,G = 0,B = 0, sizePen = 25;
    Path path;
    Paint paint;
    List<Path> paths;
    List<Paint> paints;
    private boolean isEraserMode = false;
    public int colorEraser;
    public DrawCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paths = new ArrayList<>();
        paints = new ArrayList<>();
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int i = 0;
        for (Path line: paths){
            canvas.drawPath(line, paints.get(i++));
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        posX = event.getX();
        posY = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                paint = new Paint();
                if(isEraserMode){
                    paint.setARGB(255,Color.red(colorEraser),Color.green(colorEraser),Color.blue(colorEraser));
                }else{
                    paint.setARGB(255,R,G,B);
                }
                paint.setStrokeWidth(sizePen);
                paint.setStyle(Paint.Style.STROKE);
                paints.add(paint);
                path = new Path();
                path.moveTo(posX, posY);
                paths.add(path);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int pointSave = event.getHistorySize();
                for (int i = 0; i < pointSave; i++){
                    path.lineTo(event.getHistoricalX(i), event.getHistoricalY(i));
                }
        }
        invalidate();
        return true;
    }
    public Uri captureDraw(Context context){
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 750, 500, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), scaledBitmap, "image", null);
        return Uri.parse(path);
    }
    public void changeColorPen(int red, int green, int blue){
        this.R = red;
        this.G = green;
        this.B = blue;
    }
    public void clearCanvas(){
        paths.clear();
        paints.clear();
        post(this::invalidate);
    }
    public void changeSizePen(int size){
        this.sizePen = size;
    }
    public void setEraserMode(boolean eraserMode) {
        isEraserMode = eraserMode;
    }
    public boolean isEraserMode() {
        return isEraserMode;
    }
}