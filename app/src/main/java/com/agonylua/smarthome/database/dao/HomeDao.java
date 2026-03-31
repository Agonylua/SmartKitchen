package com.agonylua.smarthome.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.agonylua.smarthome.database.entity.Home;

@Dao
public interface HomeDao {
    @Query("SELECT * FROM home WHERE homeId = :homeId")
    Home getHomeByHomeId(String homeId);

    @Query("SELECT * FROM home WHERE ownerId = :userId OR memberIds LIKE '%' || :userId || '%'")
    LiveData<Home> getHomeByUserId(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Home home);

    @Query("DELETE FROM home")
    void clearAll();

//    @Query("DELETE FROM home WHERE rule_id = :ruleId")
//    void deleteByRuleId(String ruleId);

    @Update
    void update(Home home);
}
