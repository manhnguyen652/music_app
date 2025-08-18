package com.example.music_app.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_members")
public class GroupMember {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "group_id")
    public int groupId;

    @ColumnInfo(name = "username")
    public String username;

    public GroupMember(int groupId, String username) {
        this.groupId = groupId;
        this.username = username;
    }
}

