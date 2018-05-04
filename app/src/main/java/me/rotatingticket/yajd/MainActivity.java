package me.rotatingticket.yajd;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.List;

import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.view.CandidateView;
import me.rotatingticket.yajd.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private CardView candidatesCardView;

    private static class CandidatesAdapter extends BaseAdapter {

        Context context;
        private List<? extends WordEntry> candidates;

        CandidatesAdapter(Context context, List<? extends WordEntry> candidates) {
            this.context = context;
            this.candidates = candidates;
        }

        public void setCandidates(List<? extends WordEntry> candidates) {
            this.candidates = candidates;
        }

        @Override
        public int getCount() {
            return candidates.size();
        }

        @Override
        public WordEntry getItem(int position) {
            return candidates.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.search_candidate, parent, false);
            }
            ((CandidateView)convertView).setWordEntry(getItem(position));
            return convertView;
        }
    }

    private MainActivityViewModel viewModel;
    private CandidatesAdapter candidatesAdapter;

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

    private void setUpCandidatesView(ListView candidatesView) {
        viewModel.getCandidates().observe(this, candidates -> {
            candidatesCardView.setVisibility(
                  candidates == null || candidates.size() == 0 ? View.INVISIBLE : View.VISIBLE
            );
            if (candidatesAdapter == null) {
                candidatesAdapter = new CandidatesAdapter(this, candidates);
                candidatesView.setAdapter(candidatesAdapter);
            } else {
                candidatesAdapter.setCandidates(candidates);
            }
        });
    }

    private void setUpSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.query(newText);
                return true;
            }
        });
    }
}
