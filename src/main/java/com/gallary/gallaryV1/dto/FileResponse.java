package com.gallary.gallaryV1.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
	private int id;
    private String fileName;
    private String filePath;
    private String publicId;
    private String resourceType;
    private String description;
    private List<String> tags;
	

}
