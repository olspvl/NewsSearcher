package org.newsapi.newssearcher;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class NewsItemPageActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, Uri newsPage) {
        Intent i = new Intent(context, NewsItemPageActivity.class);
        i.setData(newsPage);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return NewsItemPageFragment.newInstance(getIntent().getData());
    }

}
