package com.gallary.gallaryV1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gallary.gallaryV1.model.FileDetails;

@Repository
public interface FileRepository extends JpaRepository<FileDetails, Integer> {

    List<FileDetails> findByUser_Id(int userId);

    Optional<FileDetails> findByIdAndUser_Id(int id, int userId);
}

