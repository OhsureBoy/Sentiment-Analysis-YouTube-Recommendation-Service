package com.example.emotionapp;

public class Thumbnail {
    private String title;
    private String thumbnailUrl;
    private String videoUrl;

    private String description;

    public Thumbnail(String title, String thumbnailUrl, String videoUrl) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

}
