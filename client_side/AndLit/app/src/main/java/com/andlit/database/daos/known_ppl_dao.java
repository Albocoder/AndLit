package com.andlit.database.daos;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andlit.database.entities.KnownPPL;

import java.util.List;


@Dao
public interface known_ppl_dao {
    // INSERTION
    @Insert
    Long insertEntry(KnownPPL person);

    // SELECTION
    @Query("select * from known_ppl where id != :i order by name ")
    List<KnownPPL> getAllRecordsExceptID(int i);

    @Query("select * from known_ppl order by name")
    List<KnownPPL> getAllRecords();

    @Query("select `id` from known_ppl")
    List<Integer> getAllIDs();

    @Query("select `global_id` from known_ppl")
    List<Integer> getAllGlobalIDs();

    @Query("select distinct name from known_ppl")
    List<String> getAllDistinctNames();

    @Query("select distinct sname from known_ppl")
    List<String> getAllDistinctSNames();

    @Query("select * from known_ppl where name = :n")
    List<KnownPPL> getAllPeopleWithName(String n);

    @Query("select * from known_ppl where sname = :sn")
    List<KnownPPL> getAllPeopleWithSName(String sn);

    @Query("select * from known_ppl where dob >= :s and dob <= :e")
    List<KnownPPL> getAllPeopleBornInDates(long s, long e);

    @Query("select * from known_ppl where age = :a")
    List<KnownPPL> getAllPeopleOfAge(int a);

    @Query("select * from known_ppl where id = :i")
    KnownPPL getPersonWithID(int i);

    @Query("select global_id from known_ppl where id = :i")
    Integer getGlobalIDForID(int i);

    @Query("select address from known_ppl where id = :i")
    String getAddressForID(int i);

    @Query("select * from known_ppl where id= :i")
    KnownPPL getEntryWithID(int i);

    @Query("select * from known_ppl where name = :n and sname = :s")
    KnownPPL getPeopleWithNameAndSName(String n,String s);

    // DELETION
    @Query("delete from known_ppl")
    void purgeData();

    @Query("delete from known_ppl where age = :a")
    void deletePeopleOfAge(int a);

    @Query("delete from known_ppl where name = :n and sname = :sn")
    void deletePeopleOfNameAndSName(String n, String sn);

    @Delete
    void deletePerson(KnownPPL k);

    @Delete
    void deletePeople(KnownPPL... ks);

    // UPDATE
    @Update
    void updatePerson(KnownPPL toUpdate);
}
