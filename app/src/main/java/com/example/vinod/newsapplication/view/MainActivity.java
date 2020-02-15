package com.example.vinod.newsapplication.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.example.vinod.newsapplication.R;
import com.example.vinod.newsapplication.adapter.NewsListAdapter;
import com.example.vinod.newsapplication.database.DbHandler;
import com.example.vinod.newsapplication.model.News;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.vinod.newsapplication.utils.Constants.NEWS_DATA_KEY;

public class MainActivity extends AppCompatActivity implements NewsListAdapter.IAdapterToActivityCommunicator{

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private News news;
    String newsStringData;
    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_news_list);
        mProgressBar = findViewById(R.id.pb_progress);
        mProgressBar.setVisibility(View.VISIBLE);
        initViewsAndNetworkCall();
    }

    private void initViewsAndNetworkCall() {
        //news = readDataFromDataBase();
        if (null != news && null != news.getArticles()) {
            callNewsListAdapter();
        } else {
            initNetworkApiCall();
        }
    }

    private void initNetworkApiCall() {
        new HTTPReqTask().execute();
    }

    private void callNewsListAdapter() {
        NewsListAdapter adapter = new NewsListAdapter(news.getArticles(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void sendNewsArticleData(String urlData) {
        Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);
        intent.putExtra(NEWS_DATA_KEY, urlData);
        startActivity(intent);
    }

    private class HTTPReqTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("https://candidate-test-data-moengage.s3.amazonaws.com/Android/news-api-feed/staticResponse.json");
                urlConnection = (HttpURLConnection) url.openConnection();

                int code = urlConnection.getResponseCode();
                if (code != 200) {
                    throw new IOException("Invalid response from server: " + code);
                }

                BufferedReader rd = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                newsStringData = stringBuilder.toString();
                news = new Gson().fromJson(stringBuilder.toString(), News.class);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveDataToTheDataBase();
            callNewsListAdapter();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void saveDataToTheDataBase() {
        DbHandler dbHandler = new DbHandler(MainActivity.this);
        dbHandler.insertUserDetails(newsStringData);
    }

    private News readDataFromDataBase() {
        DbHandler dbHandler = new DbHandler(MainActivity.this);
        dbHandler.getReadableDatabase();
        return new GsonBuilder().setLenient().create().fromJson(dbHandler.getNewsData(), News.class);
    }

}
