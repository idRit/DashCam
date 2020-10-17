package com.takahashi.dashcam.utils.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity
public class VideoMetaDataModel {

    public VideoMetaDataModel(double distance, @NotNull String fileName, String totalTime) {
        this.distance = distance;
        this.fileName = fileName;
        this.totalTime = totalTime;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @ColumnInfo(name = "distance")
    public double distance;

    @PrimaryKey()
    @NonNull
    public String fileName;

    @ColumnInfo(name = "totalTime")
    public String totalTime;

    @ColumnInfo(name = "filePath")
    public String filePath;

}
