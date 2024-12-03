package app.atomofiron.searchboxapp.screens.finder.history.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HistoryDao {
 
   @Query("SELECT * FROM ItemHistory order by pinned desc, id desc")
   List<ItemHistory> getAll();
 
   @Query("SELECT * FROM ItemHistory WHERE id = :id")
   ItemHistory getById(long id);

   @Query("SELECT COUNT(*) FROM ItemHistory")
   int count();

   @Insert
   long insert(ItemHistory employee);
 
   @Update
   void update(ItemHistory employee);
 
   @Delete
   void delete(ItemHistory employee);
 
}