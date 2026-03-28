package com.selfinterior.api.visualqa;

import java.io.IOException;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface VisualQuestionStorage {
  StoredVisualQuestionFile store(UUID questionId, MultipartFile file) throws IOException;

  record StoredVisualQuestionFile(String fileName, String contentType, String storagePath) {}
}
