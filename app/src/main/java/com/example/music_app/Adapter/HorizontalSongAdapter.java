package com.example.music_app.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.entity.Song;
import com.google.gson.Gson;

import java.util.List;

public class HorizontalSongAdapter extends RecyclerView.Adapter<HorizontalSongAdapter.ViewHolder> {
    private final List<Song> songList;
    private final Context context;

    public HorizontalSongAdapter(List<Song> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_horizontal_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());

        String imageUrl = song.getImgUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_cover)
                    .error(R.drawable.default_cover)
                    .into(holder.imgCover);
        } else {
            Glide.with(context)
                    .load(R.drawable.default_cover)
                    .into(holder.imgCover);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("song_title", song.getTitle());
            intent.putExtra("song_artist", song.getArtist());
            intent.putExtra("song_path", song.getPath());
            intent.putExtra("song_duration", song.getDuration());
            intent.putExtra("song_list", new Gson().toJson(songList));
            intent.putExtra("current_index", position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}

