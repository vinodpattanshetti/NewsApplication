package com.example.vinod.newsapplication.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.vinod.newsapplication.R;
import com.example.vinod.newsapplication.adapter.NewsListAdapter;
import com.example.vinod.newsapplication.database.DbHandler;
import com.example.vinod.newsapplication.model.Article;
import com.example.vinod.newsapplication.model.News;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.vinod.newsapplication.utils.Constants.NEWS_DATA_KEY;

public class MainActivity extends AppCompatActivity implements NewsListAdapter.IAdapterToActivityCommunicator {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView mSortTextView;
    private News news;
    private List<String> sortList = new ArrayList<>();
    private List<Article> mArticlesList = new ArrayList<>();
    private List<Article> mOriginalArticlesList = new ArrayList<>();
    List<Article> mFirstFilterArticle1 = new ArrayList<>();
    List<Article> mFirstFilterArticle2 = new ArrayList<>();
    List<Article> mFirstFilterArticle3 = new ArrayList<>();
    List<Article> mFirstFilterArticle4 = new ArrayList<>();
    List<Article> mFirstFilterArticle5 = new ArrayList<>();
    private NewsListAdapter adapter;
    String newsStringData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_news_list);
        mProgressBar = findViewById(R.id.pb_progress);
        mSortTextView = findViewById(R.id.tv_sort);
        mSortTextView.setInputType(InputType.TYPE_NULL);
        mProgressBar.setVisibility(View.VISIBLE);
        sortList.add("Old to New");
        sortList.add("New to Old");
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
        mOriginalArticlesList = news.getArticles();
        mArticlesList = news.getArticles();
        adapter = new NewsListAdapter(mArticlesList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(adapter);
        initSorting();
        //inflateFilterAlertDialog();
        initFilter();
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

    private void initSorting() {
        final ArrayAdapter<String> spinner_countries = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, sortList);

        mSortTextView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Select Sort")
                        .setAdapter(spinner_countries, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Collections.reverse(mArticlesList);
                                } else if (which == 1) {
                                    Collections.reverse(mArticlesList);
                                }
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });
    }

    private void initFilter() {
        //TechCrunch
        //The Wall Street Journal
        //CNN
        //CNBC
        //Marketwatch.com
        for (int i = 0; i < mArticlesList.size(); i++) {
            if ("TechCrunch".equals(mArticlesList.get(i).getSource().getName())) {
                mFirstFilterArticle1.add(mArticlesList.get(i));
            }
        }

        for (int i = 0; i < mArticlesList.size(); i++) {
            if ("The Wall Street Journal".equals(mArticlesList.get(i).getSource().getName())) {
                mFirstFilterArticle2.add(mArticlesList.get(i));
            }
        }

        for (int i = 0; i < mArticlesList.size(); i++) {
            if ("CNN".equals(mArticlesList.get(i).getSource().getName())) {
                mFirstFilterArticle3.add(mArticlesList.get(i));
            }
        }

        for (int i = 0; i < mArticlesList.size(); i++) {
            if ("CNBC".equals(mArticlesList.get(i).getSource().getName())) {
                mFirstFilterArticle4.add(mArticlesList.get(i));
            }
        }

        for (int i = 0; i < mArticlesList.size(); i++) {
            if ("Marketwatch.com".equals(mArticlesList.get(i).getSource().getName())) {
                mFirstFilterArticle5.add(mArticlesList.get(i));
            }
        }
    }

    private void inflateFilterAlertDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.filter_dialog, null);
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        AppCompatEditText editText = dialogView.findViewById(R.id.et_box);
        final String[] filterText = {null};
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 4) {
                    filterText[0] = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Button button = dialogView.findViewById(R.id.bt_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (filterText[0]) {
                    case "TechCrunch":
                        mArticlesList = mFirstFilterArticle1;
                        break;
                    case "The Wall Street Journal":
                        mArticlesList = mFirstFilterArticle2;
                        break;
                    case "CNN":
                        mArticlesList = mFirstFilterArticle3;
                        break;
                    case "CNBC":
                        mArticlesList = mFirstFilterArticle4;
                        break;
                    case "Marketwatch.com":
                        mArticlesList = mFirstFilterArticle5;
                        break;
                    default:
                        mArticlesList = mOriginalArticlesList;
                        break;
                }
                adapter.notifyDataSetChanged();
                alertDialog.dismiss();
            }
        });

    }
}
