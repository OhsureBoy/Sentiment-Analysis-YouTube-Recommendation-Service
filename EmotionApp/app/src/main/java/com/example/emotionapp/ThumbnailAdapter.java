package com.example.emotionapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ThumbnailAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Thumbnail> thumbnails;

    public ThumbnailAdapter(Context context) {
        this.context = context;
        this.thumbnails = new ArrayList<>();
    }

    public void addThumbnail(Thumbnail thumbnail) {
        thumbnails.add(thumbnail);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return thumbnails.size();
    }

    @Override
    public Object getItem(int position) {
        return thumbnails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public void clear() {
        thumbnails.clear();
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.thumbnail_item, parent, false);
        }

        Thumbnail thumbnail = thumbnails.get(position);

        ImageView thumbnailImageView = convertView.findViewById(R.id.thumbnailImageView);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);

        Picasso.get().load(thumbnail.getThumbnailUrl()).into(thumbnailImageView);
        titleTextView.setText(thumbnail.getTitle()); // Title 설정

        // 썸네일 클릭 시 해당 영상 재생 액티비티로 이동
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("videoUrl", thumbnail.getVideoUrl());
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}
