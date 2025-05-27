package com.flamedavid.eurovision.utils;

import com.flamedavid.eurovision.exceptions.AppException;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class EmailTemplateUtil {

    public static String loadTemplate(String path, Map<String, String> values) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String content = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

            // Replace placeholders with values
            for (Map.Entry<String, String> entry : values.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return content;
        } catch (Exception e) {
            throw new AppException("Failed to load email template", e);
        }
    }
}