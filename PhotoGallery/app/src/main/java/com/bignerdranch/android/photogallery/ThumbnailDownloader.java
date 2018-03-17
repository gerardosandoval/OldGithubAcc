package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by GSMgo on 6/26/16.
 * DEFAULT
 */
public class ThumbnailDownloader<T> extends HandlerThread{
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private final static int MESSAGE_PRELOAD = 1;

    private android.os.Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private LruCache<String, Bitmap> mLruCache;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
        mLruCache = new LruCache<String, Bitmap>(16384);
    }

    @Override
    protected void onLooperPrepared(){
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch(msg.what) {
                    case MESSAGE_DOWNLOAD:
                        //Image needed to be displayed on screen now.
                        //obj is a ViewHolder (a PhotoHolder to be specific)
                        //We need to download/cache the image (or pull from cache), and then send it to the responseHandler
                        T target = (T) msg.obj;
                        handleRequest(target);
                        break;
                    case MESSAGE_PRELOAD:
                        //A request to preload an image for future use was created.
                        //obj is a string (with a url to the image)
                        //we just need to download it and put it in cache (if it's not there already)
                        String url = (String) msg.obj;
                        downloadImage(url);
                        break;
                }
            }
        };
    }

    public void queueThumbnail(T target, String url){
        Log.i(TAG, "GOT a URL: "+ url);

        if (url == null){
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void preloadImage(String url) {
        mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
    }

    private Bitmap downloadImage(String url) {
        Bitmap bitmap;

        if (url == null) //whoops!
            return null;

        //If the image is already in cache, no need to download it, just return it.
        bitmap = mLruCache.get(url);
        if (bitmap != null)
            return bitmap;

        //download and cache the image. Then return it in case it's needed right away.
        try {
            byte[] bitmapBytes = new Flickrfetchr().getUrlBytes(url);
            bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            mLruCache.put(url, bitmap);
            Log.i(TAG, "Downloaded & cached image: " + url);
            return bitmap;
        } catch (IOException ex) {
            Log.e(TAG, "Error downloading image.", ex);
            return null;
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    public void clearCache() {
        mLruCache.evictAll();
    }

    public Bitmap getCachedImage(String url) {
        return mLruCache.get(url);
    }

    private void handleRequest(final T target){
        try {
            final String url = mRequestMap.get(target);

            if (url == null){
                return;
            }

            byte[] bitmapBytes = new Flickrfetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0 , bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        } catch (IOException ioe){
            Log.e(TAG,"Error downloading image", ioe);
        }
    }
}
