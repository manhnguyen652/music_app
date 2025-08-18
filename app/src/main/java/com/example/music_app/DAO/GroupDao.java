package com.example.music_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.music_app.entity.Group;
import com.example.music_app.entity.GroupMember;

import java.util.List;

@Dao
public interface GroupDao {
    @Insert
    long insertGroup(Group group);

    @Insert
    void insertMember(GroupMember member);

    @Query("SELECT * FROM groups WHERE owner_username = :username")
    List<Group> getGroupsOwnedBy(String username);

    @Query("SELECT g.* FROM groups g JOIN group_members m ON g.id = m.group_id WHERE m.username = :username")
    List<Group> getGroupsJoinedBy(String username);

    @Query("SELECT * FROM groups WHERE invite_code = :inviteCode LIMIT 1")
    Group getGroupByInviteCode(String inviteCode);

    @Query("SELECT COUNT(*) FROM group_members WHERE group_id = :groupId AND username = :username")
    int isMember(int groupId, String username);

    @Query("SELECT u.username FROM group_members m JOIN users u ON u.username = m.username WHERE m.group_id = :groupId")
    List<String> listMemberUsernames(int groupId);
}

