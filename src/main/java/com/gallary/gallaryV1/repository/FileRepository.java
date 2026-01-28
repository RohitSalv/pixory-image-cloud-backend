package com.gallary.gallaryV1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gallary.gallaryV1.model.FileDetails;

@Repository
public interface FileRepository extends JpaRepository<FileDetails, Integer> {

    List<FileDetails> findByUser_Id(int userId);

    Optional<FileDetails> findByIdAndUser_Id(int id, int userId);

    @Query("SELECT fd FROM FileDetails fd WHERE fd.user.id = :userId AND " +
           "(LOWER(fd.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(CAST(fd.tags as string)) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<FileDetails> searchByDescriptionOrTags(
            @Param("userId") int userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}

