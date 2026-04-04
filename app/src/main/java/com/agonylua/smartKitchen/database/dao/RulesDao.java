package com.agonylua.smartKitchen.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.agonylua.smartKitchen.database.entity.Rules;

import java.util.List;

@Dao
public interface RulesDao {
    @Query("SELECT * FROM automation_rules WHERE user_id = :userId")
    LiveData<List<Rules>> getRulesListByUserId(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Rules> rules);

    @Query("DELETE FROM automation_rules")
    void clearAll();

    // 【新增】根据 ruleId 删除单条规则，用于乐观 UI 更新
    @Query("DELETE FROM automation_rules WHERE rule_id = :ruleId")
    void deleteByRuleId(String ruleId);

    @Update
    void update(Rules rules);
}