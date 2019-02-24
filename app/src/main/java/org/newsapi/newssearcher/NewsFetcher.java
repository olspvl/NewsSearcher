package org.newsapi.newssearcher;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class NewsFetcher {

    private static final String TAG = "NewsFetcher";

    private static final String API_KEY = "cb08a924e5d548d1a43b7465b3e516b4";
    private static final String DEFAULT_REQUEST = "https://newsapi.org/v2/top-headlines?country=ua&pageSize=50&apiKey=";
    private static final String SPECIAL_REQUEST = "https://newsapi.org/v2/everything?q=%s&pageSize=50&sortBy=publishedAt&apiKey=";

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<NewsItem> fetchRecentNews() {
        return downloadGalleryItems(DEFAULT_REQUEST + API_KEY);
    }

    public List<NewsItem> searchNews(String query) {
        return downloadGalleryItems(String.format(SPECIAL_REQUEST, query) + API_KEY);
    }

    private List<NewsItem> downloadGalleryItems(String url) {
        List<NewsItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    private void parseItems(List<NewsItem> items, JSONObject jsonBody) {
        try {
            JSONArray jsonArray = jsonBody.getJSONArray("articles");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.optJSONObject(i);
                JSONObject jsource = object.getJSONObject("source");
                String resource = jsource.getString("name");
                if(resource.contains(".ru")) {
                    continue;
                }
                String title = object.getString("title");
                String description = object.getString("description");
                String url = object.getString("url");
                items.add(new NewsItem(title,
                        description,
                        resource,
                        object.getString("urlToImage"),
                        url));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Items parsing failed!");
        }
    }

}
