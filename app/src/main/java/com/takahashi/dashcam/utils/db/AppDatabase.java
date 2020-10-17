package com.takahashi.dashcam.utils.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.takahashi.dashcam.utils.daos.VideoDataAccessObject;
import com.takahashi.dashcam.utils.models.VideoMetaDataModel;

@Database(entities = {VideoMetaDataModel.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract VideoDataAccessObject videoDataAccessObject();
}
