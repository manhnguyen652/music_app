package com.example.music_app.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_songs")
public class GroupSong {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "group_id")
    public int groupId;

    @ColumnInfo(name = "song_path")
    public String songPath;

    public GroupSong(int groupId, String songPath) {
        this.groupId = groupId;
        this.songPath = songPath;
    }
}

