package com.mental.dokumon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * https://stackoverflow.com/questions/1793338/drawable-grayscale/4951839#4951839
 * http://chiuki.github.io/android-shaders-filters/#/16
 * https://gist.github.com/ZacSweers/aac5f2e684e125e64ae6b1ad0a2540aa
 * https://medium.com/@ZacSweers/detecting-blurriness-with-renderscript-26960caff404
 * https://medium.com/square-corner-blog/welcome-to-the-color-matrix-64d112e3f43d
 */
public class ImageTransformer {


    // COLOR FILTERS

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COLOR_FILTER_GRAYSCALE, COLOR_FILTER_BINARY})
    public @interface ColorFilter {}

    public static final int COLOR_FILTER_GRAYSCALE = 0;
    public static final int COLOR_FILTER_BINARY = 1;


    // CONVOLVE COEFFICIENTS

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({COEFFICIENT_BLUR, COEFFICIENT_EDGE})
    public @interface ConvolveCoefficient {}

    public static final int COEFFICIENT_BLUR = 0;
    public static final int COEFFICIENT_EDGE = 1;


    public static Bitmap applyColorFilter(Drawable drawable, @ColorFilter int colorFilterType) {

        Bitmap original = ((BitmapDrawable) drawable).getBitmap();
        return applyColorFilter(original, colorFilterType);
    }

    public static Bitmap applyColorFilter(Bitmap original, @ColorFilter int colorFilterType) {

        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(getColorMatrix(colorFilterType)));
        canvas.drawBitmap(original, 0, 0, paint);

        return bitmap;
    }

    /**
     * http://aishack.in/tutorials/image-convolution-examples/
     */
    public static Bitmap convolve3x3(Bitmap original, @ConvolveCoefficient int coefficientType, Context context) {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve3x3 convolution = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(getCoeffecient(coefficientType));
        convolution.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }

    public static Bitmap blur(Bitmap original, float radius, Context context) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(context);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setInput(allocIn);
        blur.setRadius(radius);
        blur.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;
    }


    // PRIVATE METHODS

    /**
     * http://chiuki.github.io/android-shaders-filters/#/14
     *
     * @param colorFilterType
     * @return
     */
    private static ColorMatrix getColorMatrix(@ColorFilter int colorFilterType) {

        if (colorFilterType == COLOR_FILTER_BINARY) {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);

            float m = 255f;
            float t = -255*128f;
            ColorMatrix threshold = new ColorMatrix(new float[] {
                    m, 0, 0, 1, t,
                    0, m, 0, 1, t,
                    0, 0, m, 1, t,
                    0, 0, 0, 1, 0
            });

            // Convert to grayscale, then scale and clamp
            colorMatrix.postConcat(threshold);
            return colorMatrix;

        }

        if(COLOR_FILTER_GRAYSCALE == colorFilterType) {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            return colorMatrix;
        }

        return null;
    }

    private static float[] getCoeffecient(@ConvolveCoefficient int coeffecientType){

        if (COEFFICIENT_BLUR == coeffecientType) {
            return new float[] {
                    0f, 1f, 0f,
                    1f, 4f, 1f,
                    0f, 1f, 0f
            };
        }

        if (COEFFICIENT_EDGE == coeffecientType) {
            return new float[] {
                    -1f, -1f, -1f,
                    -1f,  8f, -1f,
                    -1f, -1f, -1f
            };
        }

        // default is identify coeff.
        return new float[] {0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f};
    }
}
