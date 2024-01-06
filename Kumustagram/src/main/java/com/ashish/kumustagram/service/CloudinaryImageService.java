package com.ashish.kumustagram.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryImageService {

    public Map upload(byte[] data, String folder);

    public void delete(String public_id, String folder);
}
