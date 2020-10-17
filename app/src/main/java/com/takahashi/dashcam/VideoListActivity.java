package com.takahashi.dashcam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.takahashi.dashcam.adapters.ListAdapter;
import com.takahashi.dashcam.utils.RecyclerItemClickListener;
import com.takahashi.dashcam.utils.db.DatabaseClient;
import com.takahashi.dashcam.utils.models.VideoMetaDataModel;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    List<VideoMetaDataModel> videoList = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        TextView fileName = view.findViewById(R.id.txtName);
                        String file = fileName.getText().toString();
                        String path = Uri.parse(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))).toString();

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(path + "/" + file), "video/*");
                        startActivity(intent);
                    }

                    @Override public void onLongItemClick(View view, int position) {}
                })
        );

        getTasks();
    }

    private void getTasks() {
        @SuppressLint("StaticFieldLeak")
        class GetTasks extends AsyncTask<Void, Void, List<VideoMetaDataModel>> {

            @Override
            protected List<VideoMetaDataModel> doInBackground(Void... voids) {
                List<VideoMetaDataModel> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .videoDataAccessObject()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<VideoMetaDataModel> tasks) {
                super.onPostExecute(tasks);
                mAdapter = new ListAdapter(tasks);
                recyclerView.setAdapter(mAdapter);
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }
}