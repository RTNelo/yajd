package me.rotatingticket.yajd;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.List;

import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.util.WordEntryAdapter;
import me.rotatingticket.yajd.util.zinnia.Character;
import me.rotatingticket.yajd.view.CandidateWordEntryView;
import me.rotatingticket.yajd.view.CanvasView;
import me.rotatingticket.yajd.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private CardView candidatesCardView;
    private CanvasView canvasView;
    private RecyclerView handwritingCandidatesView;

    private EditText searchViewSrcEditText;
    private WordEntryAdapter wordEntryAdapter;

    private MainActivityViewModel viewModel;
    private Character handwritingCharacter;
    private HandwritingCandidatesAdapter handwritingCandidatesAdapter;

    private static class HandwritingCandidatesAdapter extends RecyclerView.Adapter {

        static class HandwritingCandidatesViewHolder extends RecyclerView.ViewHolder {
            TextView view;

            HandwritingCandidatesViewHolder(View itemView,
                                            View.OnClickListener onClickListener) {
                super(itemView);
                view = (TextView) itemView;
                view.setOnClickListener(onClickListener);
            }
        }

        private List<String> candidates;
        private LayoutInflater layoutInflater;
        private View.OnClickListener itemOnClickListener;

        public HandwritingCandidatesAdapter(List<String> candidates,
                                            Context context,
                                            View.OnClickListener onClickListener) {
            super();
            this.candidates = candidates;
            this.layoutInflater = LayoutInflater.from(context);
            this.itemOnClickListener = onClickListener;
        }

        public void setCandidates(List<String> candidates) {
            this.candidates = candidates;
            notifyDataSetChanged();
        }

        @Override
        public HandwritingCandidatesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView itemView = (TextView) layoutInflater.inflate(R.layout.item_handwriting_candidates, parent, false);
            return new HandwritingCandidatesViewHolder(itemView, itemOnClickListener);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((HandwritingCandidatesViewHolder)holder).view.setText(candidates.get(position));
        }

        @Override
        public int getItemCount() {
            if (candidates != null) {
                return candidates.size();
            } else {
                return 0;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        candidatesCardView = findViewById(R.id.candidates_card_view);

        ListView candidatesView = findViewById(R.id.candidates_view);
        setUpCandidatesView(candidatesView);

        SearchView searchView = findViewById(R.id.search_view);
        setUpSearchView(searchView);

        CardView handwritingView = findViewById(R.id.handwriting_view);
        setUpHandwriting(handwritingView);

        startClipboardTranslationService();
    }

    private void startClipboardTranslationService() {
        Intent intent = new Intent(this, ClipboardTranslationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    /**
     * Prepare the candidate list view.
     * Set the observer of viewModel's getCandidates to update the ListView.
     * @param candidatesView the candidate list view.
     */
    private void setUpCandidatesView(ListView candidatesView) {
        candidatesView.setOnItemClickListener((parent, view, position, id) -> {
            WordEntry wordEntry = wordEntryAdapter.getItem(position);
            launchLookUpResultActivity(wordEntry);
        });

        viewModel.getCandidates().observe(this, candidates -> {
            // only show the candidate list view card if have candidates.
            candidatesCardView.setVisibility(
                  candidates == null || candidates.size() == 0 ? View.INVISIBLE : View.VISIBLE
            );
            if (wordEntryAdapter == null) {
                // create the adapter first time the candidates changed.
                wordEntryAdapter = new WordEntryAdapter(
                      this,
                      CandidateWordEntryView.class,
                      R.layout.search_candidate,
                      candidates);
                candidatesView.setAdapter(wordEntryAdapter);
            } else {
                wordEntryAdapter.setList(candidates);
            }
        });
    }

    /**
     * Prepare the search view.
     * Trigger the query for search suggestion when the query string changed.
     * @param searchView the search view.
     */
    private void setUpSearchView(SearchView searchView) {
        Resources resources = getResources();

        // store the EditText in the search view
        int searchViewSrcId = resources.getIdentifier("@android:id/search_src_text", null, null);
        searchViewSrcEditText = searchView.findViewById(searchViewSrcId);

        int searchViewCloseId = resources.getIdentifier("@android:id/search_close_btn", null, null);
        ImageView closeButton = searchView.findViewById(searchViewCloseId);
        closeButton.setOnClickListener(v -> {
            if (searchViewSrcEditText.getText().length() == 0) {
                return;
            }
            searchViewSrcEditText.setText("");
            if (!viewModel.getHandWritingToggledRealValue()) {
                searchViewSrcEditText.requestFocus();
                enableSearchViewIme();
            }
        });

        // setup search config
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        assert searchManager != null;
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(new ComponentName(this, LookUpResultActivity.class));
        searchView.setSearchableInfo(searchableInfo);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // do not care the submit event
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.query(newText);
                return true;
            }
        });
    }

    /**
     * Close the IME popover.
     */
    private void closeSearchViewIme() {
        searchViewSrcEditText.clearFocus();
    }

    /**
     * Disable IME popover on the SearchView.
     * @param close true if you want to close the popover now.
     */
    private void disableSearchViewIme(boolean close) {
        searchViewSrcEditText.setShowSoftInputOnFocus(false);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(searchViewSrcEditText.getWindowToken(), 0);

        if (close) {
            closeSearchViewIme();
        }
    }

    /**
     * Enable and Open the IME popover.
     */
    private void enableSearchViewIme() {
        searchViewSrcEditText.setShowSoftInputOnFocus(true);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(searchViewSrcEditText, 0);
    }

    /**
     * Launch a LookUpResultActivity to display a WordEntry.
     * @param wordEntry The WordEntry to display.
     */
    private void launchLookUpResultActivity(WordEntry wordEntry) {
        Intent intent = new Intent(this, LookUpResultActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        Uri data = new Uri.Builder()
              .appendQueryParameter(LookUpResultActivity.VIEW_ACTION_DATA_KEY, wordEntry.getWord())
              .build();
        intent.setData(data);
        startActivity(intent);
    }

    /**
     * Toggle the handwriting panel.
     * @param view Clicked view.
     */
    public void toggleHandwriting(View view) {
        viewModel.toggleHandwriting();
    }

    /**
     * Setup the handwriting.
     * @param handwritingView
     */
    private void setUpHandwriting(View handwritingView) {
        canvasView = handwritingView.findViewById(R.id.canvas);

        // on panel toggled
        viewModel.getHandwritingToggled().observe(this, handwritingToggled -> {
            assert handwritingToggled != null;
            if (handwritingToggled) {
                // disable IME and display the handwriting panel
                disableSearchViewIme(true);
                launchHandwriting(handwritingView);
            } else {
                // enable IME and close the handwriting panel
                enableSearchViewIme();
                closeHandwriting(handwritingView);
            }
        });

        // setup handwriting candidates list
        handwritingCandidatesView = handwritingView.findViewById(R.id.handwriting_candidates_view);
        handwritingCandidatesAdapter = new HandwritingCandidatesAdapter(
              null,
              getApplicationContext(),
              v -> {
                  insertText(((TextView)v).getText().toString());
                  refreshHandwriting();
              });
        handwritingCandidatesView.setAdapter(handwritingCandidatesAdapter);

        // on handwriting candidates changed
        viewModel.getHandwritingCandidates().observe(this, handwritingResult -> {
            // invisible if there is no handwriting candidates
            handwritingCandidatesView.setVisibility(
                  handwritingResult == null || handwritingResult.size() == 0 ? View.GONE : View.VISIBLE
            );
            // update the display
            handwritingCandidatesAdapter.setCandidates(handwritingResult);
        });

        // process the stroke event on the canvas
        canvasView.setOnStrokeListener(new CanvasView.OnStrokeListener() {
            @Override
            public boolean onStrokeBegin(float x, float y) {
                // perpare the character if there is no one.
                if (handwritingCharacter == null) {
                    prepareCharacter();
                }
                handwritingCharacter.beginStroke((int)x, (int)y);
                return false;
            }

            @Override
            public boolean onStrokeMove(float x, float y) {
                handwritingCharacter.draw((int)x, (int)y);
                return false;
            }

            @Override
            public boolean onStrokeEnd(float x, float y) {
                handwritingCharacter.endStroke((int)x, (int)y);
                // recognize (at background)
                viewModel.recognizeCharacter(handwritingCharacter);
                return false;
            }
        });
    }

    /**
     * Insert target text to the SearchView after cursor.
     * @param text Target text.
     */
    public void insertText(String text) {
        searchViewSrcEditText.getText().insert(searchViewSrcEditText.getSelectionStart(), text);
    }

    /**
     * Launch the handwriting panel.
     * @param handWritingView The panel view.
     */
    public void launchHandwriting(View handWritingView) {
        handWritingView.setVisibility(View.VISIBLE);
    }

    /**
     * Finalize current character (If there is one). Then prepare a new character.
     */
    private void prepareCharacter() {
        if (handwritingCharacter != null) {
            handwritingCharacter.close();
        }
        handwritingCharacter = new Character();
        handwritingCharacter.setWidth(canvasView.getWidth());
        handwritingCharacter.setHeight(canvasView.getHeight());
    }

    /**
     * Refresh the handwriting for next input.
     */
    public void refreshHandwriting() {
        clearHandwriting();
        prepareCharacter();
    }

    /**
     * Clear the handwriting panel and corresponding data.
     */
    public void clearHandwriting() {
        canvasView.clear();
        clearHandwritingCandidates();
        if (handwritingCharacter != null) {
            handwritingCharacter.clear();
            handwritingCharacter = null;
        }
    }

    /**
     * Close the handwriting panel.
     * @param handwritingView The panel view.
     */
    public void closeHandwriting(View handwritingView) {
        handwritingView.setVisibility(View.GONE);
        clearHandwriting();
    }

    /**
     * Clear the handwriting candidates.
     */
    void clearHandwritingCandidates() {
        handwritingCandidatesAdapter.setCandidates(null);
        handwritingCandidatesView.setVisibility(View.GONE);
    }
}
