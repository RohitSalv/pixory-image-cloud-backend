package com.gallary.gallaryV1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.service.FileService;

@RestController
@RequestMapping("/user")
public class userController {
	
	@Autowired
	private final FileService fileService;
	
	public userController(FileService fileService) {
		this.fileService = fileService;
	}
	

    @GetMapping("/hello")
    public String helloUser() {
        return "Hello, User!";
    }

    @PostMapping("/upload")
    public ResponseEntity<FileDetails> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            FileDetails savedFile = fileService.uploadFile(file);
            return ResponseEntity.ok(savedFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    

}
