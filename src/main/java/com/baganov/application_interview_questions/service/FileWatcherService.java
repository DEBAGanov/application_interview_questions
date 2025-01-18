package com.baganov.application_interview_questions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import com.baganov.application_interview_questions.repository.QuestionRepository;

@Slf4j
@Service
public class FileWatcherService {
    private final QuestionParserService parserService;
    private final QuestionRepository questionRepository;
    private final Path filesDirectory;

    @Autowired
    public FileWatcherService(QuestionParserService parserService, QuestionRepository questionRepository) {
        this.parserService = parserService;
        this.questionRepository = questionRepository;
        this.filesDirectory = Paths.get("src/main/resources/files");
        initDirectory();
    }

    @PostConstruct
    public void init() {
        if (isDatabaseEmpty()) {
            log.info("База данных пуста. Начинаем парсинг файлов...");
            parseExistingFiles();
        } else {
            log.info("База данных уже содержит вопросы. Пропускаем парсинг файлов.");
        }
    }

    private boolean isDatabaseEmpty() {
        return questionRepository.count() == 0;
    }

    private void initDirectory() {
        try {
            if (!Files.exists(filesDirectory)) {
                Files.createDirectories(filesDirectory);
                log.info("Created directory: {}", filesDirectory);
            }
        } catch (IOException e) {
            log.error("Failed to create directory: {}", filesDirectory, e);
        }
    }

    private void parseExistingFiles() {
        try {
            Files.list(filesDirectory)
                    .filter(path -> path.toString().endsWith(".md"))
                    .forEach(this::parseFile);
        } catch (IOException e) {
            log.error("Error reading files from directory", e);
        }
    }

    private void parseFile(Path file) {
        try {
            String content = Files.readString(file);
            parserService.parseAndSaveQuestions(content);
            log.info("Successfully parsed file: {}", file.getFileName());
        } catch (Exception e) {
            log.error("Error parsing file: {}", file.getFileName(), e);
        }
    }
}