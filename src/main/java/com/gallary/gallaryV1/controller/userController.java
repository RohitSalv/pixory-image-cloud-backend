package com.gallary.gallaryV1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/v1/files")
public class userController {

	@Autowired
	private final FileService fileService;

	public userController(FileService fileService) {
		this.fileService = fileService;
	}

	// Greetings and testing API start.
	@Operation(
			summary = "Greatings to user",
			description = "It is used to indicate successfull start of api."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Indicates successfull start."),
		@ApiResponse(responseCode = "400", description = "Failed to start backend")
	})
	
	@GetMapping("/hello")
	public String helloUser() {
		return "Hello, User!";
	}

	// Post Method to upload file and storing data in Database.
	@PostMapping("/upload")
	public ResponseEntity<FileDetails> uploadFile(@RequestParam("file") MultipartFile file) {

		try {
			FileDetails savedFile = fileService.uploadFile(file);
			return ResponseEntity.ok(savedFile);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}
	
	// Get Method for get details of all files.
	@GetMapping("/all")
	public List<FileDetails> getAllFileDetails(){
		return fileService.getAllFiles();
	}

	// Get One file details form the storage.
	@GetMapping("/{id}")
	public ResponseEntity<FileDetails> getFileDetails(@PathVariable int id) {

		return fileService.getFileDetailsById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	// Download the one file from the storage.
	@GetMapping("/download/{id}")
	public ResponseEntity<Resource> downlodFile(@PathVariable int id) {

		try {
			Resource resource = fileService.downloadFile(id);

			return ResponseEntity.ok()
					.header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);

		} catch (Exception e) {
			// TODO: handle exception
			return ResponseEntity.notFound().build();
		}
	}
	
	// To delete specific file
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Void> deleteFile(@PathVariable int id) {
	    fileService.deleteFile(id);
	    return ResponseEntity.noContent().build();
	}


}
