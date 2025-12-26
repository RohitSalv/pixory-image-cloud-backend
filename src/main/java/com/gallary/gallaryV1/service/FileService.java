package com.gallary.gallaryV1.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.repository.FileRepository;

@Service
public class FileService {

    private final String UPLOAD_DIR = "D:/uploads/";
    
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private final FileRepository fileRepository ;
    
    public FileService(FileRepository fileRepository) {
    	this.fileRepository = fileRepository;
    }

    public FileDetails uploadFile(MultipartFile file) {

//        // create folder if not exists
//        File directory = new File(UPLOAD_DIR);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        // save file to laptop
//        String filePath = UPLOAD_DIR + file.getOriginalFilename();
//        file.transferTo(new File(filePath));
//
//        // save details to DB
//        FileDetails details = new FileDetails();
//        details.setFileName(file.getOriginalFilename());
//        details.setFilePath(filePath);
//        details.setUploadTime(LocalDateTime.now());
//        
//        return fileRepository.save(details);
    	
    	try {
			Map uploadResult = cloudinary.uploader().upload(
					file.getBytes(),
					Map.of(
							"folder","gallary",
							"resource_type","auto"
							)
			);
			
			FileDetails fileDetails = new FileDetails();
			fileDetails.setFileName(file.getOriginalFilename());
			fileDetails.setFilePath(uploadResult.get("secure_url").toString());
			fileDetails.setPublicId(uploadResult.get("public_id").toString());
			fileDetails.setResourceType(uploadResult.get("resource_type").toString());
			fileDetails.setUploadTime(LocalDateTime.now());
			
			return fileRepository.save(fileDetails);
			
		} catch (Exception e) {
			// TODO: handle exception
			throw new RuntimeException("Cloudinary upload failed",e);
		}
    	
    	
    }
    
    // Get All file data
    public List<FileDetails> getAllFiles(){
    	return fileRepository.findAll();
    }
    
    // Get one file Data
    public FileDetails getFileDetailsById( int id){
    	return fileRepository.findById(id)
    			.orElseThrow(() -> new RuntimeException("File not Found")) ;
    }
    
    
    // Download one file
    public Resource downloadFile(int id) throws MalformedURLException {
    	FileDetails details = fileRepository.findById(id)
    			.orElseThrow(()-> new RuntimeException("File not found"));
    	
    	Path path = Paths.get(details.getFilePath());
    	
    	Resource resource = new UrlResource(path.toUri());
    	
    	if(!resource.exists()) {
    		throw new RuntimeException("File not found on disk");
    	}
    	
    	return resource;
    }
    
    //Delete the file
    public void deleteFile(int id) throws IOException {
        FileDetails file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        cloudinary.uploader().destroy(
        		file.getPublicId(),
        		Map.of("resource_type",file.getResourceType())
        		);
        
        fileRepository.delete(file); // OR mark status=DELETED
    }

}
