package com.gallary.gallaryV1.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import java.io.ByteArrayOutputStream;


import com.cloudinary.Cloudinary;
import com.gallary.gallaryV1.dto.GeminiResult;
import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.repository.FileRepository;

@Service
public class FileService {

    private final String UPLOAD_DIR = "D:/uploads/";
    
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private final FileRepository fileRepository ;
    
    @Autowired
    private final GeminiService geminiService;
    
    public FileService(FileRepository fileRepository, GeminiService geminiService) {
    	this.fileRepository = fileRepository;
		this.geminiService = geminiService;
    }

    

        public FileDetails uploadFile(MultipartFile file) {
            try {
                byte[] originalBytes = file.getBytes();

                // 1. Upload Original to Cloudinary (Full Quality)
                Map uploadResult = cloudinary.uploader().upload(originalBytes, Map.of("folder", "gallery"));

                // 2. Prepare Optimized Version for Gemini (Reduced size to save quota)
                byte[] aiBytes = resizeImageForAI(originalBytes);
                
                GeminiResult aiResult;
                try {
                    aiResult = geminiService.analyzeImage(aiBytes);
                } catch (Exception e) {
                    // Fallback if AI is down or quota is still full
                    aiResult = new GeminiResult("Description pending", List.of("uploaded"));
                }

                // 3. Save to DB
                FileDetails fileDetails = new FileDetails();
                fileDetails.setFileName(file.getOriginalFilename());
                fileDetails.setFilePath(uploadResult.get("secure_url").toString());
                fileDetails.setPublicId(uploadResult.get("public_id").toString());
                fileDetails.setResourceType(uploadResult.get("resource_type").toString());
                fileDetails.setDescription(aiResult.getDescription());
                fileDetails.setTags(aiResult.getTags());
                fileDetails.setUploadTime(LocalDateTime.now());

                return fileRepository.save(fileDetails);

            } catch (Exception e) {
                throw new RuntimeException("Media processing failed", e);
            }
        }

        private byte[] resizeImageForAI(byte[] originalBytes) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Resize to max 1024px while maintaining aspect ratio
            Thumbnails.of(new java.io.ByteArrayInputStream(originalBytes))
                    .size(1024, 1024) 
                    .outputFormat("jpg")
                    .toOutputStream(outputStream);
            return outputStream.toByteArray();
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
    
    private String extractDescription(Map uploadResult) {
        try {
            Map info = (Map) uploadResult.get("info");
            if (info == null) return null;

            Map detection = (Map) info.get("detection");
            if (detection == null) return null;

            List<Map> captions = (List<Map>) detection.get("captioning");
            if (captions == null || captions.isEmpty()) return null;

            return captions.get(0).get("caption").toString();
        } catch (Exception e) {
            return null;
        }
    }


}
