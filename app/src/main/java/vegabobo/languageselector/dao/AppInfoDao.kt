package vegabobo.languageselector.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppInfoDao {
    @Query("SELECT * FROM appinfoentity WHERE pkg = :pkg")
    fun findByPkg(pkg: String): AppInfoEntity?

    @Insert
    fun insert(aie: AppInfoEntity)

    @Query("UPDATE appinfoentity SET last_selected = NULL")
    fun cleanLastSelectedAll()

    @Query("UPDATE appinfoentity SET last_selected = :lastSelected WHERE pkg = :pkg")
    fun setLastSelected(pkg: String, lastSelected: Long)

    @Query("SELECT * FROM appinfoentity WHERE last_selected IS NOT NULL ORDER BY last_selected DESC")
    fun getHistory(): List<AppInfoEntity>
}