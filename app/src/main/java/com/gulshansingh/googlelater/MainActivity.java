package com.gulshansingh.googlelater;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class MainActivity extends OrmLiteBaseActionBarActivity<DatabaseHelper> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Dao<Query, Integer> commentDao = getHelper().getDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
