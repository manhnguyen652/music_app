package com.example.music_app.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music_app.AppDatabase;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Group;
import com.example.music_app.entity.GroupMember;

import java.util.List;
import java.util.UUID;

public class GroupFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_group, container, false);
        LinearLayout listContainer = root.findViewById(R.id.group_list_container);
        EditText inputName = root.findViewById(R.id.input_group_name);
        Button btnCreate = root.findViewById(R.id.btn_create_group);
        EditText inputCode = root.findViewById(R.id.input_invite_code);
        Button btnJoin = root.findViewById(R.id.btn_join_group);

        SessionManager sessionManager = new SessionManager(requireContext());
        String username = sessionManager.getUsername();

        refreshList(listContainer, username);

        btnCreate.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) return;
            String invite = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Group newGroup = new Group(name, username, invite);
            long id = db.groupDao().insertGroup(newGroup);
            db.groupDao().insertMember(new GroupMember((int) id, username));
            sessionManager.setActiveGroupId((int) id);
            inputName.setText("");
            refreshList(listContainer, username);
        });

        btnJoin.setOnClickListener(v -> {
            String code = inputCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) return;
            AppDatabase db = AppDatabase.getInstance(requireContext());
            Group found = db.groupDao().getGroupByInviteCode(code);
            if (found != null) {
                if (db.groupDao().isMember(found.id, username) == 0) {
                    db.groupDao().insertMember(new GroupMember(found.id, username));
                }
                sessionManager.setActiveGroupId(found.id);
                inputCode.setText("");
                refreshList(listContainer, username);
            }
        });

        return root;
    }

    private void refreshList(LinearLayout container, String username) {
        container.removeAllViews();
        AppDatabase db = AppDatabase.getInstance(requireContext());
        List<Group> owned = db.groupDao().getGroupsOwnedBy(username);
        List<Group> joined = db.groupDao().getGroupsJoinedBy(username);

        for (Group g : owned) {
            container.addView(makeGroupItemView(g, true));
        }
        for (Group g : joined) {
            boolean alreadyListed = false;
            for (Group og : owned) {
                if (og.id == g.id) { alreadyListed = true; break; }
            }
            if (!alreadyListed) container.addView(makeGroupItemView(g, false));
        }
    }

    private View makeGroupItemView(Group group, boolean isOwner) {
        View item = LayoutInflater.from(requireContext()).inflate(R.layout.item_group, null, false);
        TextView name = item.findViewById(R.id.group_name);
        TextView code = item.findViewById(R.id.group_code);
        Button activate = item.findViewById(R.id.btn_activate_group);
        Button open = item.findViewById(R.id.btn_open_group);

        name.setText(group.name + (isOwner ? " (Owner)" : ""));
        code.setText(getString(R.string.label_invite_code, group.inviteCode));

        activate.setOnClickListener(v -> {
            SessionManager sm = new SessionManager(requireContext());
            sm.setActiveGroupId(group.id);
        });
        open.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.music_app.GroupDetailActivity.class);
            intent.putExtra("group_id", group.id);
            startActivity(intent);
        });
        return item;
    }
}

