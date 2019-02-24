package org.newsapi.newssearcher;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NewsActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return NewsFragment.newInstance();
    }
}
