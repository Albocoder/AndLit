package albocoder.github.com.facedetector.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import albocoder.github.com.facedetector.database.entities.Classifier;

@Dao
public interface classifier_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertClassifier(Classifier key);

    @Query("select * from classifier")
    Classifier getClassifier();

    @Query("delete from classifier")
    void deleteClassifier();

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateClassifier(Classifier toUpdate);
}
