package com.baganov.application_interview_questions.repository;

import com.baganov.application_interview_questions.model.Question;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuestionContainingOrAnswerContaining(String question, String answer, Sort sort);

    List<Question> findByTagsIn(Set<String> tags, Sort sort);

    @Query("SELECT DISTINCT t FROM Question q JOIN q.tags t")
    Set<String> findAllTags();

    @Query(value = "SELECT * FROM questions " +
            "WHERE viewed_count = (SELECT MIN(viewed_count) FROM questions) " +
            "ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<Question> findRandomQuestion();

    @Query("SELECT MAX(q.id) FROM Question q")
    Optional<Long> findMaxId();

    @Query("SELECT t, COUNT(q) FROM Question q JOIN q.tags t GROUP BY t")
    List<Object[]> getTagsStatistics();
}