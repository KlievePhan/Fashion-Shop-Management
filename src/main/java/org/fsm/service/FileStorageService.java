package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.avatar-dir}")
    private String avatarDir;

    private static final String[] ALLOWED = {".jpg", ".jpeg", ".png", ".svg"};

    public String storeAvatar(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String orig = file.getOriginalFilename();
        String ext = orig != null && orig.contains(".")
                ? orig.substring(orig.lastIndexOf('.')).toLowerCase()
                : "";

        if (java.util.Arrays.stream(ALLOWED).noneMatch(ext::equals)) {
            throw new IllegalArgumentException("Only .jpg, .png, .svg allowed");
        }

        Path dir = Paths.get(avatarDir);
        Files.createDirectories(dir);

        String name = userId + "_" + UUID.randomUUID() + ext;
        Path target = dir.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return "avatars/" + name;
    }

    public void deleteAvatar(String url) throws IOException {
        if (url == null) return;
        Path p = Paths.get(avatarDir, url.replaceFirst("^avatars/", ""));
        Files.deleteIfExists(p);
    }
}