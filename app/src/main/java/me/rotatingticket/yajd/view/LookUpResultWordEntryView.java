package me.rotatingticket.yajd.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import me.rotatingticket.yajd.R;
import me.rotatingticket.yajd.dict.core.WordEntry;

public class LookUpResultWordEntryView extends FrameLayout implements WordEntryView {
    private TextView wordView;
    private TextView pronunciationView;
    private TextView descriptionView;

    public LookUpResultWordEntryView(@NonNull Context context) {
        this(context, null);
    }

    public LookUpResultWordEntryView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LookUpResultWordEntryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LookUpResultWordEntryView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_look_up_result, this);
        wordView = findViewById(R.id.word);
        pronunciationView = findViewById(R.id.pronunciation);
        descriptionView = findViewById(R.id.description);
    }

    private void fillByWordEntry(WordEntry wordEntry) {
        wordView.setText(wordEntry.getWord());
        String pronunciation = wordEntry.getRomajisInOneline();
        pronunciationView.setText(pronunciation);
        descriptionView.setText(wordEntry.getDescription());
    }

    @Override
    public void setWordEntry(WordEntry wordEntry) {
        fillByWordEntry(wordEntry);
    }

}
