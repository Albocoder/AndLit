package albocoder.github.com.facedetector.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import albocoder.github.com.facedetector.database.entities.training_face;

@Dao
public interface training_face_dao {
    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertTrainingFaces(training_face... faces);

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    void insertTrainingFace(training_face face);

    @Query("select * from training_faces")
    List<training_face> getAllRecords();

    @Query("select * from training_faces where label= :l")
    List<training_face> getInstancesOfLabel(int l);

    @Query("select path from training_faces where label= :l")
    List<String> getPathsOfLabel(int l);

    @Query("select distinct `label` from training_faces")
    List<Integer> getAllPossibleRecognitions();

    @Query("select count(distinct `label`) from training_faces")
    Integer getNumberOfPossibleRecognitions();

    @Query("select count(*) from training_faces where label= :l")
    List<String> getNumberOfTrainingInstancesForLabel(int l);

    @Query("select count(*) from training_faces")
    Integer getNumberOfTrainingInstances();

    @Delete
    void deleteEntry(training_face toDelete);

    @Update(onConflict = OnConflictStrategy.ROLLBACK)
    void updateRowData(training_face toChange);
}