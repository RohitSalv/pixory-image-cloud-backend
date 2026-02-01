package com.gallary.gallaryV1.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.gallary.gallaryV1.dto.FileResponse;
import com.gallary.gallaryV1.model.FileDetails;
import com.gallary.gallaryV1.model.User;
import com.gallary.gallaryV1.repository.FileRepository;
import com.gallary.gallaryV1.repository.UserRepository;
import com.gallary.gallaryV1.security.AuthUtil;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private Cloudinary cloudinary;
    @Mock
    private Uploader uploader;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private ImageAnalysisService imageAnalysisService;
    @Mock
    private AuthUtil authUtil;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FileService fileService;

    @Test
    void uploadFile_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());
        User mockUser = new User();
        mockUser.setId(1);

        when(authUtil.getCurrentUserId()).thenReturn(1);
        when(userRepository.getReferenceById(1)).thenReturn(mockUser);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(), any(Map.class)))
                .thenReturn(Map.of(
                        "secure_url", "http://res.cloudinary.com/test.jpg",
                        "public_id", "test_id",
                        "resource_type", "image"));

        when(fileRepository.save(any(FileDetails.class))).thenAnswer(invocation -> {
            FileDetails fd = invocation.getArgument(0);
            fd.setId(100); // simulate save ID
            return fd;
        });

        // Act
        FileResponse response = fileService.uploadFile(file);

        // Assert
        assertNotNull(response);
        assertEquals("test.jpg", response.getFileName());
        assertEquals("http://res.cloudinary.com/test.jpg", response.getFilePath());

        // Verify interactions - Most important: Async service was called
        verify(imageAnalysisService, times(1)).processImageForAI(eq(100), any(byte[].class));
        verify(fileRepository, times(1)).save(any(FileDetails.class));
    }
}
