package com.baganov.application_interview_questions.controller;

import com.baganov.application_interview_questions.model.Question;
import com.baganov.application_interview_questions.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin
public class QuestionController {

    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public List<Question> getAllQuestions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Set<String> tags,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);

        if (search != null && !search.isEmpty()) {
            return questionRepository.findByQuestionContainingOrAnswerContaining(search, search, sort);
        }

        if (tags != null && !tags.isEmpty()) {
            return questionRepository.findByTagsIn(tags, sort);
        }

        return questionRepository.findAll(sort);
    }

    @GetMapping("/{id}")
    public Question getQuestionById(@PathVariable Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вопрос не найден"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long id,
            @RequestBody Question questionDetails) {

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вопрос не найден"));

        question.setAnswer(questionDetails.getAnswer());
        question.setTags(questionDetails.getTags());

        Question updatedQuestion = questionRepository.save(question);
        return ResponseEntity.ok(updatedQuestion);
    }

    @GetMapping("/tags")
    public Set<String> getAllTags() {
        return questionRepository.findAllTags();
    }

    @GetMapping("/tags/stats")
    public Map<String, Long> getTagsStatistics() {
        List<Object[]> stats = questionRepository.getTagsStatistics();
        Map<String, Long> result = new HashMap<>();
        for (Object[] stat : stats) {
            result.put((String) stat[0], (Long) stat[1]);
        }
        return result;
    }
}