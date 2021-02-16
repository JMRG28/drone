package com.proj.drone_routing;

import android.graphics.SurfaceTexture;
import android.view.TextureView;

import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

/**
 * That class is used to initialise and to update the video feed received from the aircraft main camera.
 */
public class VideoFeed implements TextureView.SurfaceTextureListener {
    private VideoFeeder.VideoDataListener videoDataListener = null;
    private DJICodecManager codecManager = null;
    VideoFeed(TextureView target){
        init(target);
    }

    /**
     * Initialise the video feed view
     * @param target TextureView on which the video feed should be displayed
     */
    private void init(TextureView target) {
        if (null != target) {
            target.setSurfaceTextureListener(this);

            // This callback is for

            videoDataListener = new VideoFeeder.VideoDataListener() {
                @Override
                public void onReceive(byte[] bytes, int size) {
                    if (null != codecManager) {
                        codecManager.sendDataToDecoder(bytes, size);
                    }
                }
            };
        }

        initSDKCallback();
    }

    private void initSDKCallback() {
        try {
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(videoDataListener);
        } catch (Exception ignored) {
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (codecManager == null) {
            codecManager = new DJICodecManager(getContext(), surface, width, height);
        }
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (codecManager != null) {
            codecManager.cleanSurface();
            codecManager = null;
        }
        return false;
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
