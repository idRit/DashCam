package com.takahashi.dashcam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.takahashi.dashcam.R;
import com.takahashi.dashcam.utils.models.VideoMetaDataModel;

import java.util.List;
import java.util.Locale;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    //    private String[] mDataset;
    List<VideoMetaDataModel> videoList;

    public ListAdapter(List<VideoMetaDataModel> myDataset) {
        videoList = myDataset;
    }

    @Override
    public ListAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_view, parent, false);

        ListViewHolder vh = new ListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        VideoMetaDataModel video = videoList.get(position);
        holder.setDetails(video);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName, txtTime, txtDistance;

        public ListViewHolder(View v) {
            super(v);
            txtDistance = v.findViewById(R.id.txtDistance);
            txtName = v.findViewById(R.id.txtName);
            txtTime = v.findViewById(R.id.txtTime);
        }

        void setDetails(VideoMetaDataModel videoMetaDataModel) {
            txtName.setText(videoMetaDataModel.fileName);
            txtDistance.setText(String.format(Locale.US, "%f KM", videoMetaDataModel.distance));
            txtTime.setText(videoMetaDataModel.totalTime);
        }
    }
}
