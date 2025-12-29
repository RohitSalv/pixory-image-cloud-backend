package com.gallary.gallaryV1.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class GeminiResult {
    private String description;
    private List<String> tags;
}
