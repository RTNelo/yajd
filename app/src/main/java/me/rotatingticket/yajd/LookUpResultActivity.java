package me.rotatingticket.yajd;

import android.app.SearchManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import me.rotatingticket.yajd.util.WordEntryAdapter;
import me.rotatingticket.yajd.view.LookUpResultWordEntryView;
import me.rotatingticket.yajd.viewmodel.LookUpResultActivityViewModel;


public class LookUpResultActivity extends AppCompatActivity {

    /**
     * The key of in the data uri query parameter of word to show when launched by ACTION_VIEW
     */
    public static final String VIEW_ACTION_DATA_KEY = "word";

    private LookUpResultActivityViewModel viewModel;
    private WordEntryAdapter wordEntryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_up_result);

        viewModel = ViewModelProviders.of(this).get(LookUpResultActivityViewModel.class);
        ListView resultsView = findViewById(R.id.results);
        // setup result observer even the result value will not change.
        viewModel.getLookUpResults().observe(this, results -> {
            if (wordEntryAdapter == null) {
                // create the adapter first time the candidates changed (from null).
                wordEntryAdapter = new WordEntryAdapter(
                      this,
                      LookUpResultWordEntryView.class,
                      R.layout.item_look_up_result,
                      results);
                resultsView.setAdapter(wordEntryAdapter);
            } else {
                wordEntryAdapter.setList(results);
            }
        });

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            handleSearch(query);
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            assert data != null;
            handleView(data.getQueryParameter(VIEW_ACTION_DATA_KEY));
        }
    }

    private void handleSearch(String query) {
        viewModel.handleQuery(query);
    }

    private void handleView(String word) {
        viewModel.handleView(word);
    }
}
