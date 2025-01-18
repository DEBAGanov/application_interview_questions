package com.baganov.application_interview_questions.controller;

import com.baganov.application_interview_questions.service.QuestionParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class ParserController {

    private final QuestionParserService parserService;

    @Autowired
    public ParserController(QuestionParserService parserService) {
        this.parserService = parserService;
    }

    @PostMapping("/api/parse")
    public ResponseEntity<String> parseFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Получен файл: {}, размер: {} байт", file.getOriginalFilename(), file.getSize());

            // Читаем первые 200 символов для отладки
            String preview = new String(file.getBytes(), StandardCharsets.UTF_8)
                    .substring(0, Math.min(200, (int) file.getSize()));
            log.debug("Начало файла: {}", preview);

            int count = parserService.parseAndSaveQuestions(file);

            String message = String.format("Файл успешно обработан. Добавлено %d вопросов", count);
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке файла: ", e);
            return ResponseEntity.badRequest()
                    .body("Ошибка при обработке файла: " + e.getMessage());
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity
                .badRequest()
                .body("Файл слишком большой. Максимальный размер: 10MB");
    }
}