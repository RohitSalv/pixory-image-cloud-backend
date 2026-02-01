package com.gallary.gallaryV1.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import com.gallary.gallaryV1.exception.ResourceNotFoundException;

import io.jsonwebtoken.io.IOException;

@Service
public class FileService {

    private final Cloudinary cloudinary;
    private final FileRepository fileRepository;
    private final ImageAnalysisService imageAnalysisService;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;

    public FileService(
            Cloudinary cloudinary,
            FileRepository fileRepository,
            ImageAnalysisService imageAnalysisService,
            AuthUtil authUtil,
            UserRepository userRepository) {

        this.cloudinary = cloudinary;
        this.fileRepository = fileRepository;
        this.imageAnalysisService = imageAnalysisService;
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
            imageAnalysisService.processImageForAI(saved.getId(), originalBytes);

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
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
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

    /* ---------- helpers ---------- */

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
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
