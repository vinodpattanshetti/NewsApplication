package com.example.vinod.newsapplication.view;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import com.example.vinod.newsapplication.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.vinod.newsapplication.utils.Constants.NEWS_DATA_KEY;

public class NewsDetailActivity extends AppCompatActivity {

    private String newsDetailUrl = "";
    private WebView webview;
    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_detail_activity);
        webview = findViewById(R.id.webview);
        mImageView = findViewById(R.id.iv_back);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        newsDetailUrl = getIntent().getStringExtra(NEWS_DATA_KEY);
        if (newsDetailUrl.contains("http:")) {
            newsDetailUrl = newsDetailUrl.replace("http:", "https:");
        }
        initWebView();
    }

    private void initWebView() {
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.loadUrl(newsDetailUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webview.canGoBack()) {
            this.webview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
