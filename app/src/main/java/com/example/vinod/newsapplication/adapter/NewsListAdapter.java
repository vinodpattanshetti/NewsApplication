package com.example.vinod.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vinod.newsapplication.utils.DownloadImageTask;
import com.example.vinod.newsapplication.R;
import com.example.vinod.newsapplication.model.Article;
import com.example.vinod.newsapplication.view.MainActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.NewsViewHolder> {

    private List<Article> articles;
    private IAdapterToActivityCommunicator mIAdapterToActivityCommunicator;

    public NewsListAdapter(List<Article> articles, MainActivity activity) {
        this.articles = articles;
        mIAdapterToActivityCommunicator = activity;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_layout, parent, false);

        return new NewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        final Article article = articles.get(position);
        holder.tvHeadlineNews.setText(article.getTitle());
        holder.tvHeadlineChannel.setText(article.getSource().getName());
        holder.tvNewsDate.setText(article.getPublishedAt());
        new DownloadImageTask(holder.mImageView)
                .execute(article.getUrlToImage());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIAdapterToActivityCommunicator.sendNewsArticleData(article.getUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeadlineNews;
        TextView tvHeadlineChannel;
        TextView tvNewsDate;
        ImageView mImageView;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeadlineNews = itemView.findViewById(R.id.tv_headline_news);
            tvHeadlineChannel = itemView.findViewById(R.id.tv_headline_channel);
            tvNewsDate = itemView.findViewById(R.id.tv_news_date);
            mImageView = itemView.findViewById(R.id.iv_news_image);
        }
    }

    public interface IAdapterToActivityCommunicator {
        void sendNewsArticleData(String urlData);
    }
}
