package com.gallary.gallaryV1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gallary.gallaryV1.model.FileDetails;

@Repository
public interface FileRepository extends JpaRepository<FileDetails, Integer> {

}
