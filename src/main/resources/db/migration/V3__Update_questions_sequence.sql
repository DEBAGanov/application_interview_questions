-- Удаляем существующую последовательность, если она есть
DROP SEQUENCE IF EXISTS questions_id_seq CASCADE;

-- Создаем новую последовательность, начиная с максимального значения ID
CREATE SEQUENCE questions_id_seq START WITH 1000;

-- Изменяем колонку id, чтобы она использовала новую последовательность
ALTER TABLE questions ALTER COLUMN id SET DEFAULT nextval('questions_id_seq'); 