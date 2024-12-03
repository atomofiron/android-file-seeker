package app.atomofiron.searchboxapp.screens.finder.history.dao;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ItemHistory {

   @PrimaryKey(autoGenerate = true)
   public long id;

   public String title;

   public boolean pinned;

   @Override
   public boolean equals(@Nullable Object obj) {
      if (obj == null) return false;
      if (!(obj instanceof ItemHistory)) return false;
      ItemHistory o = (ItemHistory) obj;
      return o.id == id && o.title.equals(title);
   }
}