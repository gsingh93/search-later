package com.gulshansingh.googlelater;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends OrmLiteBaseActionBarActivity<DatabaseHelper> {

    private Dao<Query, Integer> mQueryDao;
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mQueryDao = getHelper().getDao();
            Cursor cursor = getQueryCursor();
            startManagingCursor(cursor);
            mAdapter = new SimpleCursorAdapter(this,
                    android.R.layout.simple_list_item_checked,
                    cursor,
                    new String[] { DatabaseHelper.QUERY_TEXT_COLUMN },
                    new int[] { android.R.id.text1 });
            final ListView listView = (ListView) findViewById(R.id.query_list);
            listView.setAdapter(mAdapter);

            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                    mode.setTitle("Delete " + listView.getCheckedItemCount() + " items?");
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_delete:
                            SparseBooleanArray arr = listView.getCheckedItemPositions();
                            List<Integer> ids = new ArrayList<Integer>();
                            try {
                                Cursor c = getQueryCursor();
                                int idIndex = c.getColumnIndex("_id");
                                for (int i = 0; i < listView.getCount(); i++) {
                                    if (arr.get(i)) {
                                        c.moveToPosition(i);
                                        ids.add(c.getInt(idIndex));
                                    }
                                }
                                mQueryDao.deleteIds(ids);
                                mAdapter.swapCursor(getQueryCursor());
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            mode.finish();
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    // Inflate the menu for the CAB
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.long_press_menu, menu);
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    //listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Cursor getQueryCursor() throws SQLException {
        QueryBuilder qb = mQueryDao.queryBuilder().selectColumns("text");
        CloseableIterator<Query> iterator = mQueryDao.iterator(qb.prepare());
        AndroidDatabaseResults results = (AndroidDatabaseResults) iterator.getRawResults();
        return results.getRawCursor();
    }

    private void addQuery(String query) {
        if (!query.equals("")) {
            try {
                mQueryDao.create(new Query(query.trim()));
                mAdapter.swapCursor(getQueryCursor());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem item = menu.findItem(R.id.action_new);
        final View v = item.getActionView();
        final EditText editText = (EditText) v.findViewById(R.id.action_view_edit_text);

        MenuItemCompat.setOnActionExpandListener(item, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                editText.post(new Runnable() {
                    @Override
                    public void run() {
                        editText.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true;
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addQuery(editText.getText().toString());
                    editText.clearFocus();
                    item.collapseActionView();
                    return true;
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_new:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
