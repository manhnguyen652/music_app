package com.example.music_app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.HorizontalSongAdapter;
import com.example.music_app.AppDatabase;
import com.example.music_app.R;
import com.example.music_app.entity.Song;
import com.example.music_app.utils.SongUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerHotSongs, recyclerLatestSongs, recyclerFavoriteSongs;
    private ProgressBar progressBar;
    private Button btnOnline, btnOffline;

    private List<Song> allSongs = new ArrayList<>();
    private List<Song> hotSongs = new ArrayList<>();
    private List<Song> newSongs = new ArrayList<>();
    private List<Song> favoriteSongs = new ArrayList<>();

    private String currentSource = "";
    private boolean isFirstLoad = true;

    private Context context;

    private HorizontalSongAdapter hotAdapter;
    private HorizontalSongAdapter latestAdapter;
    private HorizontalSongAdapter favoriteAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = requireContext();

        recyclerHotSongs = view.findViewById(R.id.recyclerHotSongs);
        recyclerLatestSongs = view.findViewById(R.id.recyclerLatestSongs);
        recyclerFavoriteSongs = view.findViewById(R.id.recyclerFavoriteSongs);
        progressBar = view.findViewById(R.id.progressBar);
        btnOnline = view.findViewById(R.id.btnOnline);
        btnOffline = view.findViewById(R.id.btnOffline);
        FloatingActionButton fabEmotionDj = view.findViewById(R.id.fabEmotionDj);

        // Init RecyclerViews once
        recyclerHotSongs.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerHotSongs.setHasFixedSize(true);
        recyclerHotSongs.setItemViewCacheSize(20);
        hotAdapter = new HorizontalSongAdapter(hotSongs, context);
        recyclerHotSongs.setAdapter(hotAdapter);

        recyclerLatestSongs.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerLatestSongs.setHasFixedSize(true);
        recyclerLatestSongs.setItemViewCacheSize(20);
        latestAdapter = new HorizontalSongAdapter(newSongs, context);
        recyclerLatestSongs.setAdapter(latestAdapter);

        recyclerFavoriteSongs.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerFavoriteSongs.setHasFixedSize(true);
        recyclerFavoriteSongs.setItemViewCacheSize(20);
        favoriteAdapter = new HorizontalSongAdapter(favoriteSongs, context);
        recyclerFavoriteSongs.setAdapter(favoriteAdapter);

        btnOffline.setOnClickListener(v -> {
            if (!currentSource.equals("offline")) {
                currentSource = "offline";
                saveCurrentSource("offline");
                Snackbar.make(view, "Đang tải nhạc từ thiết bị", Snackbar.LENGTH_SHORT).show();
                loadSongsFromDevice();
            }
        });

        btnOnline.setOnClickListener(v -> {
            if (!isOnline()) {
                Snackbar.make(view, "Không có kết nối Internet", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (!currentSource.equals("online")) {
                currentSource = "online";
                saveCurrentSource("online");
                Snackbar.make(view, "Đang tải nhạc online...", Snackbar.LENGTH_SHORT).show();
                loadSongsFromApi();
            }
        });

        fabEmotionDj.setOnClickListener(v -> {
            EmotionDjDialogFragment dialog = new EmotionDjDialogFragment();
            dialog.setOnEmotionSelectedListener(emotion -> {
                progressBar.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    List<Song> mixed = SongUtils.getSongsForEmotion(emotion);
                    if (mixed != null && !mixed.isEmpty()) {
                        AppDatabase db = AppDatabase.getInstance(context);
                        db.songDao().deleteAll();
                        db.songDao().insertAll(mixed);
                        allSongs = mixed;
                    }
                    if (getActivity() == null || !isAdded()) return;
                    getActivity().runOnUiThread(() -> {
                        if (mixed == null || mixed.isEmpty()) {
                            Snackbar.make(view, "Không tìm thấy bài phù hợp", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(view, "AI DJ đã mix playlist theo cảm xúc!", Snackbar.LENGTH_SHORT).show();
                            updateSongList(allSongs);
                        }
                        progressBar.setVisibility(View.GONE);
                    });
                }).start();
            });
            dialog.show(getParentFragmentManager(), "EmotionDjDialogFragment");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoad) {
            isFirstLoad = false;
            currentSource = getSavedSource();
            loadCachedOrDefault();
        } else {
            updateSongList(allSongs);
        }
    }

    private void loadCachedOrDefault() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Song> cachedSongs = db.songDao().getAllSongs();
            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                if (cachedSongs.isEmpty()) {
                    currentSource = "offline";
                    saveCurrentSource("offline");
                    loadSongsFromDevice();
                } else {
                    allSongs = cachedSongs;
                    updateSongList(allSongs);
                }
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private void saveCurrentSource(String source) {
        SharedPreferences prefs = context.getSharedPreferences("SongPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("currentSource", source).apply();
    }

    private String getSavedSource() {
        SharedPreferences prefs = context.getSharedPreferences("SongPrefs", Context.MODE_PRIVATE);
        return prefs.getString("currentSource", "offline");
    }

    private void loadSongsFromDevice() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<Song> songs = SongUtils.getAllSongsFromDevice(context);
            AppDatabase db = AppDatabase.getInstance(context);
            db.songDao().deleteAll();
            db.songDao().insertAll(songs);

            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                allSongs = songs;
                updateSongList(allSongs);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private void loadSongsFromApi() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<Song> songs = SongUtils.getAllSongsFromJamendo();
            AppDatabase db = AppDatabase.getInstance(context);
            db.songDao().deleteAll();
            db.songDao().insertAll(songs);

            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                allSongs = songs;
                updateSongList(allSongs);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private void updateSongList(List<Song> songs) {
        if (songs == null) return;
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<Song> top = SongUtils.getTopPlayedSongs(context);
            List<Song> latest = SongUtils.getLatestSongs(songs);
            List<Song> fav = SongUtils.getFavoriteList(context);

            if (getActivity() == null || !isAdded()) return;
            getActivity().runOnUiThread(() -> {
                hotSongs.clear();
                hotSongs.addAll(top);
                newSongs.clear();
                newSongs.addAll(latest);
                favoriteSongs.clear();
                favoriteSongs.addAll(fav);

                if (hotAdapter != null) hotAdapter.notifyDataSetChanged();
                if (latestAdapter != null) latestAdapter.notifyDataSetChanged();
                if (favoriteAdapter != null) favoriteAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
