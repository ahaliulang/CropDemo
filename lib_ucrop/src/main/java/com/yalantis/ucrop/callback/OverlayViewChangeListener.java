package com.yalantis.ucrop.callback;

import android.graphics.RectF;

/**
 * Created by Oleksii Shliama.
 */
public interface OverlayViewChangeListener {

    void onCropRectUpdated(RectF cropRect);

    void onCropRectChanged(RectF src,RectF dst,int duration);
}