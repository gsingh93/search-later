package com.gulshansingh.googlelater;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "queries")
public class Query {

    @DatabaseField(generatedId = true, columnName = "_id")
    public int id;

    @DatabaseField
    public String text;

    public Query() {
        // ORMLite needs a no-argument constructor
    }

    public Query(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
