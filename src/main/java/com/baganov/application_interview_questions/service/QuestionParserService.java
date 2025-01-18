package com.baganov.application_interview_questions.service;

import com.baganov.application_interview_questions.model.Question;
import com.baganov.application_interview_questions.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class QuestionParserService {

    private final QuestionRepository questionRepository;
    private static final Pattern QUESTION_PATTERN = Pattern.compile(
            "##\\s*(\\d+)\\.\\s*`([^`]+)`([\\s\\S]+?)(?=\\s*##|\\s*$)",
            Pattern.DOTALL);

    @Autowired
    public QuestionParserService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Transactional
    public int parseAndSaveQuestions(MultipartFile file) throws Exception {
        String content = readFile(file);
        return parseAndSaveQuestions(content);
    }

    @Transactional
    public int parseAndSaveQuestions(String content) {
        try {
            log.info("Начинаем парсинг контента длиной: {} символов", content.length());

            List<Question> questions = parseQuestions(content);
            log.info("Распарсено {} вопросов", questions.size());

            questionRepository.deleteAll();
            List<Question> savedQuestions = questionRepository.saveAll(questions);
            log.info("Сохранено {} вопросов", savedQuestions.size());

            return savedQuestions.size();
        } catch (Exception e) {
            log.error("Ошибка при парсинге и сохранении вопросов", e);
            throw new RuntimeException("Failed to parse and save questions", e);
        }
    }

    private String readFile(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    private List<Question> parseQuestions(String content) {
        List<Question> questions = new ArrayList<>();
        Matcher matcher = QUESTION_PATTERN.matcher(content);

        while (matcher.find()) {
            try {
                String idStr = matcher.group(1);
                String questionText = matcher.group(2);
                String answer = matcher.group(3);

                log.debug("Найдено совпадение:");
                log.debug("ID: {}", idStr);
                log.debug("Вопрос: {}", questionText);
                log.debug("Начало ответа: {}", answer.substring(0, Math.min(100, answer.length())));

                Long questionId = Long.parseLong(idStr);

                Question question = new Question();
                question.setId(questionId);
                question.setQuestion(questionText.trim());
                question.setViewedCount(0);
                question.getTags().add("не просмотрен");

                answer = cleanAndFormatAnswer(answer);
                question.setAnswer(answer);

                questions.add(question);
                log.info("Успешно обработан вопрос #{}: {}", questionId, questionText);

            } catch (Exception e) {
                log.error("Ошибка при парсинге вопроса: {}", e.getMessage(), e);
            }
        }

        log.info("Всего найдено вопросов: {}", questions.size());
        return questions;
    }

    private String cleanAndFormatAnswer(String answer) {
        return answer
                // Удаляем HTML-блоки
                .replaceAll("(?s)<DIV[^>]*>.*?</DIV>\\s*$", "")
                // Удаляем пустые строки
                .replaceAll("(?m)^\\s*$\\n", "")
                // Форматируем маркированные списки
                .replaceAll("(?m)^\\+\\s", "* ")
                // Сохраняем форматирование кода
                .replaceAll("`([^`]+)`", "`$1`")
                // Сохраняем жирный текст
                .replaceAll("\\*\\*([^*]+?)\\*\\*", "**$1**")
                // Сохраняем курсив
                .replaceAll("\\*([^*]+?)\\*", "*$1*")
                // Удаляем лишние переносы строк
                .replaceAll("\n{3,}", "\n\n")
                // Удаляем ссылки на изображения
                .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                .trim();
    }
}