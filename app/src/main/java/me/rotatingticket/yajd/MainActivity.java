package me.rotatingticket.yajd;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.util.WordEntryAdapter;
import me.rotatingticket.yajd.view.CandidateWordEntryView;
import me.rotatingticket.yajd.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private CardView candidatesCardView;

    private MainActivityViewModel viewModel;
    private WordEntryAdapter wordEntryAdapter;

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
    }

    /**
     * Prepare the candidate list view.
     * Set the observer of viewModel's getCandidates to update the ListView.
     * @param candidatesView the candidate list view.
     */
    private void setUpCandidatesView(ListView candidatesView) {
        candidatesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WordEntry wordEntry = wordEntryAdapter.getItem(position);
                launchLookUpResultActivity(wordEntry);
            }
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
}
