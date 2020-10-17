package com.takahashi.dashcam.utils.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.takahashi.dashcam.utils.models.VideoMetaDataModel;

import java.util.List;

@Dao
public interface VideoDataAccessObject {

    @Query("SELECT * FROM VideoMetaDataModel")
    List<VideoMetaDataModel> getAll();

    @Query("SELECT filePath FROM VideoMetaDataModel")
    List<String> getPath();

    @Insert
    void insert(VideoMetaDataModel videoMetaDataModel);

    @Delete
    void delete(VideoMetaDataModel videoMetaDataModel);
}
