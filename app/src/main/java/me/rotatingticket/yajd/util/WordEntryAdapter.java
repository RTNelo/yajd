package me.rotatingticket.yajd.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import me.rotatingticket.yajd.dict.core.WordEntry;
import me.rotatingticket.yajd.view.WordEntryView;

/**
 * The adapter of the list view of word entry.
 * Use a WordEntryView as views of items,
 * then use corresponding WordEntry to fill the view content.
 */
public class WordEntryAdapter extends BaseAdapter {

    private Context context;
    private List<? extends WordEntry> list;
    private int itemViewLayoutId;
    private Class<? extends WordEntryView> viewKls;

    public WordEntryAdapter(Context context,
                     Class<? extends WordEntryView> viewKls,
                     int itemViewLayoutId,
                     List<? extends WordEntry> list) {
        this.context = context;
        this.list = list;
        this.itemViewLayoutId = itemViewLayoutId;
        this.viewKls = viewKls;
    }

    public void setList(List<? extends WordEntry> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public WordEntry getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(itemViewLayoutId, parent, false);
        }
        viewKls.cast(convertView).setWordEntry(getItem(position));
        return convertView;
    }
}
