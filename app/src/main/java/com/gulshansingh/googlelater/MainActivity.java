package com.gulshansingh.googlelater;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;

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
                    android.R.layout.simple_list_item_1,
                    cursor,
                    new String[] { DatabaseHelper.QUERY_TEXT_COLUMN },
                    new int[] { android.R.id.text1 });
            ListView listView = (ListView) findViewById(R.id.query_list);
            listView.setAdapter(mAdapter);
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
