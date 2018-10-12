package com.example.tuantran.recording.listeners;

/**
 * Created by Tuan Tran on 3/24/2018.
 */

public interface OnDatabaseChangedListener {
    void onNewDatabaseEntryAdded();
    void onDatabaseEntryRename();

    //TODO
    void onDatabaseEntryRenamed();
}
