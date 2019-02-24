package org.newsapi.newssearcher;


import android.graphics.Bitmap;

public class NewsItem {
    private String mTitle;
    private String mDescription;
    private String mResource;
    private String mImageUrl;
    private String mUrl;
    private Bitmap mBitmap;

    public NewsItem(String title, String description, String resource, String urlToImage, String url) {
        mTitle = title;
        mDescription = description;
        mResource = resource;
        mUrl = url;
        mImageUrl = urlToImage;
        mBitmap = null;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getResource() {
        return mResource;
    }

}
