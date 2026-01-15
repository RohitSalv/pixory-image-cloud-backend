package com.gallary.gallaryV1.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.gallary.gallaryV1.dto.GeminiResult;
import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.repository.FileRepository;

@Service
public class FileService {

    private final Cloudinary cloudinary;
    private final FileRepository fileRepository;
    private final GeminiService geminiService;
    private final ImageProcessor imageProcessor;

    public FileService(
            Cloudinary cloudinary,
            FileRepository fileRepository,
            GeminiService geminiService,
            ImageProcessor imageProcessor) {

        this.cloudinary = cloudinary;
        this.fileRepository = fileRepository;
        this.geminiService = geminiService;
        this.imageProcessor = imageProcessor;
    }

    public FileDetails uploadFile(MultipartFile file) {

        validateFile(file);

        try {
            byte[] originalBytes = file.getBytes();

            Map uploadResult = cloudinary.uploader()
                    .upload(originalBytes, Map.of("folder", "gallery"));

            GeminiResult aiResult = analyzeWithFallback(originalBytes);

            FileDetails details = buildFileDetails(file, uploadResult, aiResult);
            return fileRepository.save(details);

        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }
    }

    public List<FileDetails> getAllFiles() {
        return fileRepository.findAll();
    }

    public FileDetails getFileDetailsById(int id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public String getFileUrl(int id) {
        return getFileDetailsById(id).getFilePath();
    }

    public void deleteFile(int id) {
        FileDetails file = getFileDetailsById(id);

        try {
            cloudinary.uploader().destroy(
                    file.getPublicId(),
                    Map.of("resource_type", file.getResourceType())
            );
        } catch (Exception ignored) {
            // Cloudinary cleanup failure shouldn't block DB delete
        }

        fileRepository.delete(file);
    }

    /* ---------- helpers ---------- */

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
    }

    private GeminiResult analyzeWithFallback(byte[] originalBytes) {
        try {
            byte[] optimized = imageProcessor.resizeForAI(originalBytes);
            return geminiService.analyzeImage(optimized);
        } catch (Exception e) {
            return new GeminiResult("Description pending", List.of("uploaded"));
        }
    }

    private FileDetails buildFileDetails(
            MultipartFile file,
            Map uploadResult,
            GeminiResult aiResult) {

        FileDetails details = new FileDetails();
        details.setFileName(file.getOriginalFilename());
        details.setFilePath(uploadResult.get("secure_url").toString());
        details.setPublicId(uploadResult.get("public_id").toString());
        details.setResourceType(uploadResult.get("resource_type").toString());
        details.setDescription(aiResult.getDescription());
        details.setTags(aiResult.getTags());
        details.setUploadTime(LocalDateTime.now());
        return details;
    }
}
