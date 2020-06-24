package com.yalantis.ucrop.util;

import android.content.Context;

/**
 * author:tdn
 * time:2020/6/22
 * description:
 */
public class ViewUtils {
    //px 转成 dp
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);  // int 向下取整，加0.5 是实现四舍五入
    }

    //dp 转成 px
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
