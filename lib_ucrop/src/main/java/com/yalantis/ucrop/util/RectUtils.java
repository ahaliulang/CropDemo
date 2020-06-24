package com.yalantis.ucrop.util;

import android.graphics.Matrix;
import android.graphics.RectF;

public class RectUtils {

    /**
     * Gets a float array of the 2D coordinates representing a rectangles
     * corners.
     * The order of the corners in the float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param r the rectangle to get the corners of
     * @return the float array of corners (8 floats)
     */
    public static float[] getCornersFromRect(RectF r) {
        return new float[]{
                r.left, r.top,
                r.right, r.top,
                r.right, r.bottom,
                r.left, r.bottom
        };
    }

    /**
     * Gets a float array of two lengths representing a rectangles width and height
     * The order of the corners in the input float array is:
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     *
     * @param corners the float array of corners (8 floats)
     * @return the float array of width and height (2 floats)
     */
    public static float[] getRectSidesFromCorners(float[] corners) {
        return new float[]{(float) Math.sqrt(Math.pow(corners[0] - corners[2], 2) + Math.pow(corners[1] - corners[3], 2)),
                (float) Math.sqrt(Math.pow(corners[2] - corners[4], 2) + Math.pow(corners[3] - corners[5], 2))};
    }

    public static float[] getCenterFromRect(RectF r) {
        return new float[]{r.centerX(), r.centerY()};
    }

    /**
     * Takes an array of 2D coordinates representing corners and returns the
     * smallest rectangle containing those coordinates.
     *
     * @param array array of 2D coordinates
     * @return smallest rectangle containing coordinates
     */
    public static RectF trapToRect(float[] array) {
        RectF r = new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 1; i < array.length; i += 2) {
            float x = Math.round(array[i - 1] * 10) / 10.f;
            float y = Math.round(array[i] * 10) / 10.f;
            r.left = (x < r.left) ? x : r.left;
            r.top = (y < r.top) ? y : r.top;
            r.right = (x > r.right) ? x : r.right;
            r.bottom = (y > r.bottom) ? y : r.bottom;
        }
        r.sort();
        return r;
    }


    /**
     * 获取矩形缩放的比例
     *
     * @param src 原始矩形
     * @param dst 目标矩形
     * @return 缩放的比例
     */
    public static float getScaleFactor(RectF src, RectF dst) {
        if (src == null || dst == null) return 1f;
        try {
            float dstDiagonalLen = (float) Math.sqrt(Math.pow(dst.right - dst.left, 2) + Math.pow(dst.bottom - dst.top, 2));
            float srcDiagonalLen = (float) Math.sqrt(Math.pow(src.right - src.left, 2) + Math.pow(src.bottom - src.top, 2));
            return dstDiagonalLen / srcDiagonalLen;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1f;
    }

    /**
     * 获取两个矩形移动的距离
     * @param src 原始矩形
     * @param dst 目标矩形
     * @return
     */
    public static float[] getTranslateDelta(RectF src, RectF dst) {
        float[] result = new float[2];
        if (src == null || dst == null) return result;
        result[0] = dst.centerX() - src.centerX();
        result[1] = dst.centerY() - src.centerY();
        return result;
    }


    /**
     * Gets a float array of the 2D coordinates representing a rectangles
     * corners.
     * The order of the corners in the float array is:
     * 0---1--->2
     * |        |
     * 7        3
     * |        |
     * 6<---5---4
     *
     * @param r the rectangle to get the corners of
     * @return the float array of corners (16 floats)
     */
    public static float[] getCornersFromRectForEight(RectF r) {
        return new float[]{
                r.left, r.top,
                r.centerX(), r.top,
                r.right, r.top,
                r.right, r.centerY(),
                r.right, r.bottom,
                r.centerX(), r.bottom,
                r.left, r.bottom,
                r.left, r.centerY()
        };
    }

}