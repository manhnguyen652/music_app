package com.example.music_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_app.DAO.GroupDao;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.GroupSong;
import com.example.music_app.entity.Song;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends AppCompatActivity {

    private int groupId;
    private LinearLayout listContainer;
    private Button btnPlayAll;
    private Button btnAddSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        groupId = getIntent().getIntExtra("group_id", -1);
        if (groupId <= 0) { finish(); return; }

        listContainer = findViewById(R.id.group_song_list);
        btnPlayAll = findViewById(R.id.btn_play_all);
        btnAddSong = findViewById(R.id.btn_add_song);
        TextView title = findViewById(R.id.tv_group_title);
        com.example.music_app.entity.Group g = AppDatabase.getInstance(this).groupDao().getGroupById(groupId);
        if (g != null) title.setText("Nhóm: " + g.name + "  (" + g.inviteCode + ")");

        refreshSongs();

        btnPlayAll.setOnClickListener(v -> playFromGroup(0));
        btnAddSong.setOnClickListener(this::onAddSongClicked);
    }

    private void refreshSongs() {
        listContainer.removeAllViews();
        GroupDao dao = AppDatabase.getInstance(this).groupDao();
        List<Song> songs = dao.getSongsInGroup(groupId);
        for (int i = 0; i < songs.size(); i++) {
            int index = i;
            Song s = songs.get(i);
            View item = getLayoutInflater().inflate(R.layout.item_group_song, listContainer, false);
            TextView title = item.findViewById(R.id.tv_song_title);
            TextView artist = item.findViewById(R.id.tv_song_artist);
            ImageView btnDelete = item.findViewById(R.id.btn_delete);
            Button btnPlay = item.findViewById(R.id.btn_play);

            title.setText(s.getTitle());
            artist.setText(s.getArtist());

            btnPlay.setOnClickListener(v -> playFromGroup(index));
            btnDelete.setOnClickListener(v -> {
                dao.deleteGroupSong(groupId, s.getPath());
                refreshSongs();
            });

            listContainer.addView(item);
        }
    }

    private void onAddSongClicked(View v) {
        GroupDao gdao = AppDatabase.getInstance(this).groupDao();
        List<Song> allSongs = AppDatabase.getInstance(this).songDao().getAllSongs();
        if (allSongs == null) allSongs = new ArrayList<>();
        CharSequence[] titles = new CharSequence[allSongs.size()];
        for (int i = 0; i < allSongs.size(); i++) {
            Song s = allSongs.get(i);
            titles[i] = (s.getTitle() != null ? s.getTitle() : s.getPath());
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Chọn bài hát để thêm")
                .setItems(titles, (dialog, which) -> {
                    Song chosen = allSongs.get(which);
                    if (gdao.countSongInGroup(groupId, chosen.getPath()) == 0) {
                        gdao.insertGroupSong(new GroupSong(groupId, chosen.getPath()));
                        refreshSongs();
                        Toast.makeText(this, "Đã thêm vào danh sách nhóm", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bài hát đã tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void playFromGroup(int index) {
        GroupDao dao = AppDatabase.getInstance(this).groupDao();
        List<Song> songs = dao.getSongsInGroup(groupId);
        if (songs.isEmpty()) {
            Toast.makeText(this, "Danh sách trống", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("song_list", new Gson().toJson(songs));
        intent.putExtra("current_index", index);
        intent.putExtra("song_path", songs.get(index).getPath());
        intent.putExtra("song_artist", songs.get(index).getArtist());
        startActivity(intent);
    }
}

