-- Создаем таблицу questions без автоинкремента
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL
);

-- Создаем таблицу для тегов
CREATE TABLE question_tags (
    question_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (question_id, tag),
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
);