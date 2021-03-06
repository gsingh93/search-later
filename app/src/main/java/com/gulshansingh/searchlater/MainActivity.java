package com.gulshansingh.searchlater;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
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
import android.widget.AdapterView;
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
        getSupportActionBar().setIcon(R.drawable.ic_action_bar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            mQueryDao = getHelper().getDao();
            Cursor cursor = getQueryCursor();
            startManagingCursor(cursor);
            mAdapter = new SimpleCursorAdapter(this,
                    R.layout.list_view_row,
                    cursor,
                    new String[] { DatabaseHelper.QUERY_TEXT_COLUMN },
                    new int[] { android.R.id.text1 });
            final ListView listView = (ListView) findViewById(R.id.query_list);
            listView.setAdapter(mAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    intent.putExtra(SearchManager.QUERY, textView.getText());
                    startActivity(intent);
                }
            });

            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                      long id, boolean checked) {
                    int count = listView.getCheckedItemCount();
                    String ending = count == 1 ? " item?" : " items?";
                    mode.setTitle("Delete " + count + ending);
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_delete:
                            SparseBooleanArray arr = listView.getCheckedItemPositions();
                            List<Integer> ids = new ArrayList<Integer>();
                            try {
                                Cursor c = getQueryCursor();
                                int idIndex = c.getColumnIndex(DatabaseHelper.QUERY_ID_COLUMN);
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
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.long_press_menu, menu);
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
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

    private void cancelAlarm() {
        Log.i(getClass().getName(), "Cancelling alarm");

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private Cursor getQueryCursor() throws SQLException {
        QueryBuilder qb = mQueryDao.queryBuilder().selectColumns(DatabaseHelper.QUERY_TEXT_COLUMN);
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
    public void onStop() {
        super.onStop();
        ReminderService.startAlarm(this);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("num_queries",
                mAdapter.getCount()).commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        cancelAlarm();
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
                editText.setText("");
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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_new:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
