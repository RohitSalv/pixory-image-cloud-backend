package com.gallary.gallaryV1.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.TimeZoneColumn;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDetails {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private int id;
    private String fileName;
    private String filePath;
    private String description;
    private String[] tags;
    private int userId;

    private LocalDateTime uploadTime;


}
