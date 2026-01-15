package com.gallary.gallaryV1.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;

@Component
public class ImageProcessor {

    public byte[] resizeForAI(byte[] originalBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(originalBytes))
                .size(1024, 1024)
                .outputFormat("jpg")
                .toOutputStream(out);
        return out.toByteArray();
    }
}
