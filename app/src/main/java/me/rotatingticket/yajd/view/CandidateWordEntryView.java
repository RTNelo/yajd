package me.rotatingticket.yajd.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import me.rotatingticket.yajd.R;
import me.rotatingticket.yajd.dict.core.WordEntry;

/**
 * The view of candidate word entry for custom dict search suggestion.
 */
public class CandidateWordEntryView extends LinearLayout implements WordEntryView {
    private TextView wordView;
    private TextView pronunciationView;
    private TextView summaryView;

    public CandidateWordEntryView(Context context) {
        this(context, null);
    }

    public CandidateWordEntryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CandidateWordEntryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CandidateWordEntryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * Inflate the view by layout xml search_candidate_view.
     */
    private void init() {
        inflate(getContext(), R.layout.search_candidate_view, this);
        wordView = findViewById(R.id.word);
        pronunciationView = findViewById(R.id.pronunciation);
        summaryView = findViewById(R.id.summary);
    }

    /**
     * Set the view content by a WordEntry.
     * @param wordEntry the WordEntry to set the content.
     */
    private void fillByWordEntry(WordEntry wordEntry) {
        wordView.setText(wordEntry.getWord());
        String pronunciation = StringUtils.join(wordEntry.getRomajis(), ", ");
        pronunciationView.setText(pronunciation);
        summaryView.setText(wordEntry.getSummary());
    }

    /**
     * Set the current WordEntry and refresh the view content.
     * @param wordEntry WordEntry to set.
     */
    public void setWordEntry(WordEntry wordEntry) {
        fillByWordEntry(wordEntry);
    }
}
