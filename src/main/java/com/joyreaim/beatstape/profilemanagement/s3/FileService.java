package com.joyreaim.beatstape.profilemanagement.s3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class FileService {
    private String AVATAR_UPLOAD_BUCKET;
    private String AVATAR_EXTENSION = ".jpg";

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${profile.s3.avatar.bucket}")
    private String rawBucket;

    @PostConstruct
    private void initBuckets() {
        AVATAR_UPLOAD_BUCKET = "s3://" + rawBucket + "/";
    }

    public void writeResource(String id, byte[] rawBytes) throws IOException {
        Resource resource = this.resourceLoader.getResource(AVATAR_UPLOAD_BUCKET + id + AVATAR_EXTENSION);
        WritableResource writableResource = (WritableResource) resource;
        try (OutputStream outputStream = writableResource.getOutputStream()) {
            outputStream.write(rawBytes);
        }
    }
}
