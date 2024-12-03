package app.atomofiron.searchboxapp.screens.finder.history.dao;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { ItemHistory.class }, version = 1, exportSchema = false)
public abstract class HistoryDatabase extends RoomDatabase {
   public abstract HistoryDao historyDao();
}