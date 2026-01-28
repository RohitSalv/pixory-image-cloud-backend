package com.gallary.gallaryV1.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.gallary.gallaryV1.dto.FileResponse;
import com.gallary.gallaryV1.dto.GeminiResult;
import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.model.User;
import com.gallary.gallaryV1.repository.FileRepository;
import com.gallary.gallaryV1.repository.UserRepository;
import com.gallary.gallaryV1.security.AuthUtil;

import io.jsonwebtoken.io.IOException;

@Service
public class FileService {

    private final Cloudinary cloudinary;
    private final FileRepository fileRepository;
    private final GeminiService geminiService;
    private final ImageProcessor imageProcessor;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;

    public FileService(
            Cloudinary cloudinary,
            FileRepository fileRepository,
            GeminiService geminiService,
            ImageProcessor imageProcessor,
            AuthUtil authUtil,
            UserRepository userRepository) {

        this.cloudinary = cloudinary;
        this.fileRepository = fileRepository;
        this.geminiService = geminiService;
        this.imageProcessor = imageProcessor;
        this.authUtil = authUtil;
        this.userRepository = userRepository;
    }

    @Transactional
    public FileResponse uploadFile(MultipartFile file) throws java.io.IOException {
        int userId = authUtil.getCurrentUserId();
        User user = userRepository.getReferenceById(userId);

        validateFile(file);

        try {
            byte[] originalBytes = file.getBytes();

            Map uploadResult = cloudinary.uploader()
                    .upload(originalBytes, Map.of("folder", "gallery"));

            // Initial FileDetails with pending AI analysis
            FileDetails details = buildFileDetails(file, uploadResult,
                    new GeminiResult("AI analysis pending...", List.of("pending")));
            details.setUser(user);

            FileDetails saved = fileRepository.save(details);

            // Trigger AI analysis in the background
            processImageForAI(saved.getId(), originalBytes);

            return mapToDto(saved);

        } catch (IOException e) {
            throw new RuntimeException("File upload failed");
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getMyFiles() {
        int userId = authUtil.getCurrentUserId();

        return fileRepository.findByUser_Id(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> searchFiles(String searchTerm, Pageable pageable) {
        int userId = authUtil.getCurrentUserId();
        return fileRepository.searchByDescriptionOrTags(userId, searchTerm, pageable)
                .map(this::mapToDto);
    }

    private FileResponse mapToDto(FileDetails file) {
        FileResponse dto = new FileResponse();
        dto.setId(file.getId());
        dto.setFileName(file.getFileName());
        dto.setFilePath(file.getFilePath());
        dto.setPublicId(file.getPublicId());
        dto.setResourceType(file.getResourceType());
        dto.setDescription(file.getDescription());
        dto.setTags(file.getTags());
        return dto;
    }

    public FileDetails getFileDetailsById(int id, User user) {
        return fileRepository.findByIdAndUser_Id(id, user.getId())
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public void deleteFile(int id, User user) {

        FileDetails file = getFileDetailsById(id, user);

        try {
            cloudinary.uploader().destroy(
                    file.getPublicId(),
                    Map.of("resource_type", file.getResourceType()));
        } catch (Exception ignored) {
        }

        fileRepository.delete(file);
    }

    @Async
    @Transactional
    public void processImageForAI(int fileId, byte[] imageBytes) {
        fileRepository.findById(fileId).ifPresent(fileDetails -> {
            try {
                GeminiResult aiResult = analyzeWithFallback(imageBytes);

                fileDetails.setDescription(aiResult.getDescription());
                fileDetails.setTags(new ArrayList<>(aiResult.getTags()));
                fileRepository.save(fileDetails);
            } catch (Exception e) {
                System.err.println("Asynchronous AI analysis failed for fileId: " + fileId + " - " + e.getMessage());
                fileDetails.setDescription("AI analysis failed: " + e.getMessage());
                fileDetails.setTags(new ArrayList<>(List.of("analysis-failed")));
                fileRepository.save(fileDetails);
            }
        });
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
        details.setTags(new ArrayList<>(aiResult.getTags()));
        details.setUploadTime(LocalDateTime.now());
        return details;
    }
}
