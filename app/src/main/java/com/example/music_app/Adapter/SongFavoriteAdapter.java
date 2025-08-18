package com.example.music_app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.AppDatabase;
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;
import com.example.music_app.entity.SongFavorite;
import com.google.gson.Gson;

import java.util.List;

public class SongFavoriteAdapter extends RecyclerView.Adapter<SongFavoriteAdapter.SongViewHolder> {

    private final List<Song> songFavorite;
    private final Context context;

    public SongFavoriteAdapter(List<Song> songFavorite, Context context) {
        this.songFavorite = songFavorite;
        this.context = context;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_play_list, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songFavorite.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(formatTime((int) song.getDuration()));

        if (song.getImgUrl() != null && !song.getImgUrl().isEmpty()) {
            Glide.with(context).load(song.getImgUrl()).into(holder.imgCover);
        } else {
            new Thread(() -> {
                byte[] art = getAlbumArt(song.getPath());
                if (art != null) {
                    ((Activity) context).runOnUiThread(() -> {
                        Glide.with(context)
                                .asBitmap()
                                .load(art)
                                .placeholder(R.drawable.default_cover)
                                .into(holder.imgCover);
                    });
                }
            }).start();
        }

        holder.btnDelete.setImageResource(R.drawable.ic_delete);

        holder.btnDelete.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(context);
            String currentUser = sessionManager.getUsername();

            AppDatabase.getInstance(context).songFavoriteDao()
                    .deleteByUsernameAndPath(currentUser, song.getPath());

            int removedIndex = holder.getAdapterPosition();
            songFavorite.remove(removedIndex);
            notifyItemRemoved(removedIndex);

            Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("song_title", song.getTitle());
            intent.putExtra("song_artist", song.getArtist());
            intent.putExtra("song_path", song.getPath());
            intent.putExtra("song_duration", song.getDuration());
            intent.putExtra("song_list", new Gson().toJson(songFavorite));
            intent.putExtra("current_index", position);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return songFavorite != null ? songFavorite.size() : 0;
    }

    private String formatTime(int durationSeconds) {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView imgCover, btnDelete;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_Title);
            tvArtist = itemView.findViewById(R.id.tv_Artist);
            tvDuration = itemView.findViewById(R.id.tv_Duration);
            imgCover = itemView.findViewById(R.id.imgCover);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private byte[] getAlbumArt(String path) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            return art;
        } catch (Exception e) {
            return null;
        }
    }
}
