package org.newsapi.newssearcher;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private RecyclerView mNewsRecyclerView;
    private List<NewsItem> mNewsItems = new ArrayList<>();
    private ThumbnailDownloader<NewsHolder> mThumbnailDownloader;

    private String mCurrentQuery;

    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mCurrentQuery = null;
        updateItems();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<NewsHolder>() {
                    @Override
                    public void onThumbnailDownloaded(NewsHolder target, Bitmap thumbnail) {
                        target.bindBitmap(thumbnail);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news, container, false);
        mNewsRecyclerView = v.findViewById(R.id.news_recycler_view);
        mNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mNewsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mNewsRecyclerView.addItemDecoration(dividerItemDecoration);

        setupAdapter();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_news, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mCurrentQuery = searchView.getQuery().toString().replaceAll("\\s+", "+");
                updateItems();
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentQuery != null) {
                    searchView.setQuery(mCurrentQuery, false);
                }
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if(mCurrentQuery != null) {
                    mCurrentQuery = null;
                    updateItems();
                }
                searchView.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }
        });
    }

    private void updateItems() {
        new FetchNewsTask(mCurrentQuery).execute();
    }

    private void setupAdapter() {
        if(isAdded()) {
            mNewsRecyclerView.setAdapter(new NewsAdapter(mNewsItems));
        }
    }

    private class NewsHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private NewsItem mNewsItem;
        private TextView mTitle;
        private TextView mDescription;
        private TextView mResource;
        private ImageView mImageView;

        public NewsHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.news_title);
            mDescription = itemView.findViewById(R.id.news_description);
            mResource = itemView.findViewById(R.id.news_resource);
            mImageView = itemView.findViewById(R.id.news_image);
            itemView.setOnClickListener(this);
        }

        public void bindBitmap(Bitmap bitmap) {
            if(mNewsItem.getBitmap() == null) {
                mNewsItem.setBitmap(bitmap);
                mImageView.setImageBitmap(bitmap);
            } else {
                setCurrentBitmap();
            }
        }

        public void setCurrentBitmap() {
            mImageView.setImageBitmap(mNewsItem.getBitmap());
        }

        public boolean isImageAttached() {
            return mNewsItem.getBitmap() != null;
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }


        public void bindGalleryItem(NewsItem newsItem) {
            mNewsItem = newsItem;
            mTitle.setText(mNewsItem.getTitle());
            mDescription.setText(mNewsItem.getDescription().equals("null") ? "" : mNewsItem.getDescription());
            mResource.setText(mNewsItem.getResource());
        }

        @Override
        public void onClick(View v) {
            Intent i = NewsItemPageActivity.newIntent(getContext(), Uri.parse(mNewsItem.getUrl()));
            startActivity(i);
        }
    }

    private class NewsAdapter extends RecyclerView.Adapter<NewsHolder> {

        private List<NewsItem> mNewsItems;

        public NewsAdapter(List<NewsItem> newsItems) {
            mNewsItems = newsItems;
        }

        @NonNull
        @Override
        public NewsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.news_item, parent, false);
            return new NewsHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NewsHolder holder, int position) {
            NewsItem newsItem = mNewsItems.get(position);
            holder.bindGalleryItem(newsItem);
            Drawable placeholder = getResources().getDrawable(R.drawable.loading);
            if(holder.isImageAttached()) {
                holder.setCurrentBitmap();
            } else {
                holder.bindDrawable(placeholder);
                mThumbnailDownloader.queueThumbnail(holder, newsItem.getImageUrl());
            }
        }

        @Override
        public int getItemCount() {
            return mNewsItems.size();
        }
    }


    private class FetchNewsTask extends AsyncTask<Void, Void, List<NewsItem>> {
        private String mQuery;

        public FetchNewsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<NewsItem> doInBackground(Void... params) {
            if(mQuery == null) {
                return new NewsFetcher().fetchRecentNews();
            } else {
                return new NewsFetcher().searchNews(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<NewsItem> newsItems) {
            mNewsItems = newsItems;
            setupAdapter();
        }
    }

}
