package com.gallary.gallaryV1.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.repository.FileRepository;

@Service
public class FileService {

    private final String UPLOAD_DIR = "D:/uploads/";

    @Autowired
    private final FileRepository fileRepository ;
    
    public FileService(FileRepository fileRepository) {
    	this.fileRepository = fileRepository;
    }

    public FileDetails uploadFile(MultipartFile file) throws IOException {

        // create folder if not exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // save file to laptop
        String filePath = UPLOAD_DIR + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        // save details to DB
        FileDetails details = new FileDetails();
        details.setFileName(file.getOriginalFilename());
        details.setFilePath(filePath);
        details.setUploadTime(LocalDateTime.now());
        
        return fileRepository.save(details);
    }
    
}
