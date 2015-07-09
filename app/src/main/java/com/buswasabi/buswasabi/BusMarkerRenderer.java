package com.buswasabi.buswasabi;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Hashtable;

/**
 * Created by root on 7/8/15.
 */
public class BusMarkerRenderer {
    Context context;
    Hashtable<String, Bitmap> cache;

    public BusMarkerRenderer(Context context) {
        this.context = context;
        cache = new Hashtable<String, Bitmap>();
    }

    public Bitmap getBitmap(String text) {
        Bitmap result = cache.get(text);
        if(result != null) {
            return result;
        }
        result = drawTextToBitmap(text);
        cache.put(text, result);
        return result;
    }


    private Bitmap drawTextToBitmap(String gText) {
        Resources resources = context.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                Bitmap.createBitmap(120, 40, Bitmap.Config.ARGB_8888);

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(255, 255, 255));
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;


        paint.setColor(Color.rgb(0, 0, 0));
        canvas.drawRect(0, 0, 120, 40, paint);
        paint.setColor(Color.rgb(255, 255, 255));
        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }
}
