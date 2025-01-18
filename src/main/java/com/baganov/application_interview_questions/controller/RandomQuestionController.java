package com.baganov.application_interview_questions.controller;

import com.baganov.application_interview_questions.model.Question;
import com.baganov.application_interview_questions.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/random")
@CrossOrigin
@Slf4j
public class RandomQuestionController {

    private final QuestionRepository questionRepository;

    @Autowired
    public RandomQuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping("/question")
    public ResponseEntity<Question> getRandomQuestion() {
        // Получаем вопрос с наименьшим количеством просмотров
        Question question = questionRepository.findRandomQuestion()
                .orElseThrow(() -> new RuntimeException("No questions available"));

        // Обновляем статистику просмотров
        question.setViewedCount(question.getViewedCount() + 1);
        question.setLastViewed(LocalDateTime.now());
        question.getTags().remove("не просмотрен");
        question.getTags().add("просмотрено");

        questionRepository.save(question);
        log.info("Показан вопрос ID: {}, просмотров: {}", question.getId(), question.getViewedCount());

        return ResponseEntity.ok(question);
    }

    @PostMapping("/question")
    public ResponseEntity<Question> addQuestion(@RequestBody Question newQuestion) {
        // Генерируем следующий ID
        Long nextId = questionRepository.findMaxId().orElse(0L) + 1;
        newQuestion.setId(nextId);
        newQuestion.setViewedCount(0);

        Question savedQuestion = questionRepository.save(newQuestion);
        log.info("Добавлен новый вопрос ID: {}", savedQuestion.getId());

        return ResponseEntity.ok(savedQuestion);
    }
}