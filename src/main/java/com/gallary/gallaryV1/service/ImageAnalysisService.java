package com.gallary.gallaryV1.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gallary.gallaryV1.dto.GeminiResult;
import com.gallary.gallaryV1.repository.FileRepository;

@Service
public class ImageAnalysisService {

    private final FileRepository fileRepository;
    private final GeminiService geminiService;
    private final ImageProcessor imageProcessor;

    public ImageAnalysisService(FileRepository fileRepository, GeminiService geminiService,
            ImageProcessor imageProcessor) {
        this.fileRepository = fileRepository;
        this.geminiService = geminiService;
        this.imageProcessor = imageProcessor;
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

    private GeminiResult analyzeWithFallback(byte[] originalBytes) {
        try {
            byte[] optimized = imageProcessor.resizeForAI(originalBytes);
            return geminiService.analyzeImage(optimized);
        } catch (Exception e) {
            return new GeminiResult("Description pending", List.of("uploaded"));
        }
    }
}
