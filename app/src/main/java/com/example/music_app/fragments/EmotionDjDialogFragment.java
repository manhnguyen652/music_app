package com.example.music_app.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.music_app.R;

public class EmotionDjDialogFragment extends DialogFragment {

    public interface OnEmotionSelectedListener {
        void onEmotionSelected(String emotionText);
    }

    private OnEmotionSelectedListener listener;

    public void setOnEmotionSelectedListener(OnEmotionSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_emotion_dj, container, false);

        EditText etEmotion = view.findViewById(R.id.etEmotion);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnMix = view.findViewById(R.id.btnMix);

        TextView emojiHappy = view.findViewById(R.id.emoji_happy);
        TextView emojiSad = view.findViewById(R.id.emoji_sad);
        TextView emojiEnergetic = view.findViewById(R.id.emoji_energetic);
        TextView emojiRelax = view.findViewById(R.id.emoji_relax);
        TextView emojiFocus = view.findViewById(R.id.emoji_focus);
        TextView emojiRomance = view.findViewById(R.id.emoji_romance);

        View.OnClickListener emojiClick = v -> {
            TextView tv = (TextView) v;
            etEmotion.setText(tv.getText());
        };
        emojiHappy.setOnClickListener(emojiClick);
        emojiSad.setOnClickListener(emojiClick);
        emojiEnergetic.setOnClickListener(emojiClick);
        emojiRelax.setOnClickListener(emojiClick);
        emojiFocus.setOnClickListener(emojiClick);
        emojiRomance.setOnClickListener(emojiClick);

        btnCancel.setOnClickListener(v -> dismiss());
        btnMix.setOnClickListener(v -> {
            String text = etEmotion.getText() != null ? etEmotion.getText().toString().trim() : "";
            if (listener != null && !TextUtils.isEmpty(text)) {
                listener.onEmotionSelected(text);
                dismiss();
            }
        });

        return view;
    }
}

