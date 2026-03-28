package com.selfinterior.api.visualqa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalMockVisualQuestionStorage implements VisualQuestionStorage {
  private final Path rootPath;

  public LocalMockVisualQuestionStorage(
      @Value("${app.storage.visual-question-root:tmp/visual-questions}") String rootPath) {
    this.rootPath = Paths.get(rootPath);
  }

  @Override
  public StoredVisualQuestionFile store(UUID questionId, MultipartFile file) throws IOException {
    String originalFileName =
        file.getOriginalFilename() == null ? "upload.bin" : file.getOriginalFilename();
    Path questionPath = rootPath.resolve(questionId.toString());
    Files.createDirectories(questionPath);
    Path targetPath = questionPath.resolve(originalFileName);
    file.transferTo(targetPath);

    return new StoredVisualQuestionFile(
        originalFileName, file.getContentType(), targetPath.toString().replace('\\', '/'));
  }
}
