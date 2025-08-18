package com.example.music_app.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "groups")
public class Group {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "owner_username")
    public String ownerUsername;

    @ColumnInfo(name = "invite_code")
    public String inviteCode;

    public Group(String name, String ownerUsername, String inviteCode) {
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.inviteCode = inviteCode;
    }
}

