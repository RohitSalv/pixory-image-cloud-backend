package com.gallary.gallaryV1.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.service.FileService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Operation(summary = "Health check")
    @GetMapping("/hello")
    public String helloUser() {
        return "Hello, User!";
    }

    @PostMapping("/upload")
    public ResponseEntity<FileDetails> uploadFile(
            @RequestParam MultipartFile file) {

        return ResponseEntity.ok(fileService.uploadFile(file));
    }

    @GetMapping("/all")
    public List<FileDetails> getAllFileDetails() {
        return fileService.getAllFiles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDetails> getFileDetails(@PathVariable int id) {
        return ResponseEntity.ok(fileService.getFileDetailsById(id));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Void> downloadFile(@PathVariable int id) {
        String redirectUrl = fileService.getFileUrl(id);
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable int id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
