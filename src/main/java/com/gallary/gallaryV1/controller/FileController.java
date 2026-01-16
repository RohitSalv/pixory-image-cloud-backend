package com.gallary.gallaryV1.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gallary.gallaryV1.dto.FileResponse;
import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.security.SecurityUtil;
import com.gallary.gallaryV1.service.FileService;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> upload(@RequestParam MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadFile(file));
    }

    
    @GetMapping("/me")
    public ResponseEntity<List<FileResponse>> getMyFiles() {
        return ResponseEntity.ok(fileService.getMyFiles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDetails> getFileDetails(@PathVariable int id) {
        var user = SecurityUtil.getCurrentUser().getUser();
        return ResponseEntity.ok(fileService.getFileDetailsById(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable int id) {
        var user = SecurityUtil.getCurrentUser().getUser();
        fileService.deleteFile(id, user);
        return ResponseEntity.noContent().build();
    }
}
