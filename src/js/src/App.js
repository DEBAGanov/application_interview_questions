import React, { useState, useEffect } from 'react';
import { Layout, Input, Select, List, Card, Tag, Button, Modal, message, Upload } from 'antd';
import { EditOutlined, UploadOutlined, TagsOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import './App.css';

const { Header, Content } = Layout;
const { Search } = Input;

const App = () => {
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [selectedTags, setSelectedTags] = useState([]);
  const [sortOrder, setSortOrder] = useState('asc');
  const [tags, setTags] = useState([]);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [editedAnswer, setEditedAnswer] = useState('');
  const [tagModalVisible, setTagModalVisible] = useState(false);
  const [editingTags, setEditingTags] = useState([]);
  const [newTag, setNewTag] = useState('');
  const [randomQuestion, setRandomQuestion] = useState(null);
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [newQuestion, setNewQuestion] = useState({ question: '', answer: '', tags: [] });
  const [tagsStats, setTagsStats] = useState({});

  useEffect(() => {
    fetchQuestions();
    fetchTags();
    fetchTagsStats();
  }, [searchText, selectedTags, sortOrder]);

  const fetchQuestions = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchText) params.append('search', searchText);
      if (selectedTags.length) selectedTags.forEach(tag => params.append('tags', tag));
      params.append('sortDir', sortOrder);

      const response = await fetch(`/api/questions?${params}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setQuestions(data);
    } catch (error) {
      console.error('Error fetching questions:', error);
      message.error('Ошибка при загрузке вопросов: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchTags = async () => {
    try {
      const response = await fetch('/api/questions/tags');
      const data = await response.json();
      setTags(data);
    } catch (error) {
      message.error('Ошибка при загрузке тегов');
    }
  };

  const fetchTagsStats = async () => {
    try {
      const response = await fetch('/api/questions/tags/stats');
      const data = await response.json();
      setTagsStats(data);
    } catch (error) {
      message.error('Ошибка при загрузке статистики тегов');
    }
  };

  const handleEdit = (question) => {
    setEditingQuestion(question);
    setEditedAnswer(question.answer);
    setEditModalVisible(true);
  };

  const handleSave = async () => {
    if (!editingQuestion) return;

    try {
      const response = await fetch(`/api/questions/${editingQuestion.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...editingQuestion,
          answer: editedAnswer,
        }),
      });

      if (response.ok) {
        message.success('Ответ успешно обновлен');
        fetchQuestions();
        setEditModalVisible(false);
      } else {
        message.error('Ошибка при обновлении ответа');
      }
    } catch (error) {
      message.error('Ошибка при сохранении изменений');
    }
  };

  const handleEditTags = (question) => {
    setEditingQuestion(question);
    setEditingTags(question.tags || []);
    setTagModalVisible(true);
  };

  const handleSaveTags = async () => {
    if (!editingQuestion) return;

    try {
      const response = await fetch(`/api/questions/${editingQuestion.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...editingQuestion,
          tags: editingTags,
        }),
      });

      if (response.ok) {
        message.success('Теги успешно обновлены');
        fetchQuestions();
        setTagModalVisible(false);
      } else {
        message.error('Ошибка при обновлении тегов');
      }
    } catch (error) {
      message.error('Ошибка при сохранении тегов');
    }
  };

  const handleAddTag = () => {
    if (newTag && !editingTags.includes(newTag)) {
      setEditingTags([...editingTags, newTag]);
      setNewTag('');
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setEditingTags(editingTags.filter(tag => tag !== tagToRemove));
  };

  const fetchRandomQuestion = async () => {
    try {
      const response = await fetch('/api/random/question');
      if (!response.ok) throw new Error('Failed to fetch random question');
      const data = await response.json();
      setRandomQuestion(data);
    } catch (error) {
      message.error('Ошибка при получении случайного вопроса');
    }
  };

  const addNewQuestion = async (values) => {
    try {
      const response = await fetch('/api/random/question', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(values),
      });
      if (!response.ok) throw new Error('Failed to add question');
      message.success('Вопрос успешно добавлен');
      fetchQuestions();
    } catch (error) {
      message.error('Ошибка при добавлении вопроса');
    }
  };

  const handleAddQuestion = async () => {
    try {
      await addNewQuestion(newQuestion);
      setAddModalVisible(false);
      setNewQuestion({ question: '', answer: '', tags: [] });
    } catch (error) {
      message.error('Ошибка при добавлении вопроса');
    }
  };

  return (
    <Layout>
      <Header style={{ background: '#fff', padding: '0 20px' }}>
        <h1>Вопросы для собеседования</h1>
      </Header>
      <Content style={{ padding: '20px' }}>
        <Card
          title="Статистика тегов"
          style={{ marginBottom: '20px' }}
          size="small"
        >
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
            {Object.entries(tagsStats).map(([tag, count]) => (
              <Tag key={tag} color={getTagColor(tag)}>
                {tag}: {count}
              </Tag>
            ))}
          </div>
        </Card>

        <div style={{ marginBottom: '20px' }}>
          <Button onClick={fetchRandomQuestion} type="primary" style={{ marginRight: '16px' }}>
            Случайный вопрос
          </Button>
          <Button onClick={() => setAddModalVisible(true)}>
            Добавить вопрос
          </Button>
        </div>

        {randomQuestion && (
          <Card
            style={{ marginBottom: '20px' }}
            title={randomQuestion.question}
            extra={`Просмотрено: ${randomQuestion.viewedCount} раз`}
          >
            <ReactMarkdown>{randomQuestion.answer}</ReactMarkdown>
            <div style={{ marginTop: '10px' }}>
              {randomQuestion.tags.map(tag => (
                <Tag key={tag} color={getTagColor(tag)}>{tag}</Tag>
              ))}
            </div>
          </Card>
        )}

        <div style={{ marginBottom: '20px' }}>
          <Search
            placeholder="Поиск по вопросам и ответам"
            onSearch={value => setSearchText(value)}
            style={{ width: 300, marginRight: '20px' }}
            allowClear
          />
          <Select
            mode="multiple"
            placeholder="Выберите теги"
            style={{ width: 300, marginRight: '20px' }}
            onChange={value => setSelectedTags(value)}
            options={tags.map(tag => ({ label: tag, value: tag }))}
            allowClear
          />
          <Select
            defaultValue="asc"
            style={{ width: 120 }}
            onChange={value => setSortOrder(value)}
            options={[
              { label: 'По возрастанию', value: 'asc' },
              { label: 'По убыванию', value: 'desc' },
            ]}
          />
        </div>

        <List
          loading={loading}
          dataSource={questions}
          locale={{ emptyText: 'Нет данных' }}
          renderItem={(item) => (
            <Card
              key={item.id}
              style={{ marginBottom: '16px' }}
              title={`${item.id}. ${item.question}`}
              extra={
                <div>
                  <Button
                    icon={<TagsOutlined />}
                    onClick={() => handleEditTags(item)}
                    style={{ marginRight: '8px' }}
                  >
                    Теги
                  </Button>
                  <Button
                    icon={<EditOutlined />}
                    onClick={() => handleEdit(item)}
                  >
                    Редактировать
                  </Button>
                </div>
              }
            >
              <div className="markdown-content">
                <ReactMarkdown>{item.answer}</ReactMarkdown>
              </div>
              <div style={{ marginTop: '10px' }}>
                {item.tags && item.tags.map(tag => (
                  <Tag key={tag} color={getTagColor(tag)}>{tag}</Tag>
                ))}
              </div>
            </Card>
          )}
        />

        <Modal
          title="Редактирование ответа"
          open={editModalVisible}
          onOk={handleSave}
          onCancel={() => setEditModalVisible(false)}
          width={800}
        >
          <Input.TextArea
            value={editedAnswer}
            onChange={e => setEditedAnswer(e.target.value)}
            rows={10}
          />
        </Modal>

        <Modal
          title="Управление тегами"
          open={tagModalVisible}
          onOk={handleSaveTags}
          onCancel={() => setTagModalVisible(false)}
        >
          <div style={{ marginBottom: '16px' }}>
            <Input
              placeholder="Новый тег"
              value={newTag}
              onChange={e => setNewTag(e.target.value)}
              onPressEnter={handleAddTag}
              style={{ width: 'calc(100% - 90px)', marginRight: '8px' }}
            />
            <Button type="primary" onClick={handleAddTag}>
              Добавить
            </Button>
          </div>
          <div>
            {editingTags.map(tag => (
              <Tag
                key={tag}
                closable
                onClose={() => handleRemoveTag(tag)}
                style={{ margin: '4px' }}
              >
                {tag}
              </Tag>
            ))}
          </div>
        </Modal>

        <Modal
          title="Добавить новый вопрос"
          open={addModalVisible}
          onOk={handleAddQuestion}
          onCancel={() => setAddModalVisible(false)}
          width={800}
        >
          <div style={{ marginBottom: '16px' }}>
            <Input
              placeholder="Вопрос"
              value={newQuestion.question}
              onChange={e => setNewQuestion({ ...newQuestion, question: e.target.value })}
            />
          </div>
          <div style={{ marginBottom: '16px' }}>
            <Input.TextArea
              placeholder="Ответ (поддерживается Markdown)"
              value={newQuestion.answer}
              onChange={e => setNewQuestion({ ...newQuestion, answer: e.target.value })}
              rows={10}
            />
          </div>
          <div>
            <Select
              mode="tags"
              style={{ width: '100%' }}
              placeholder="Добавьте теги"
              onChange={tags => setNewQuestion({ ...newQuestion, tags })}
              value={newQuestion.tags}
            />
          </div>
        </Modal>
      </Content>
    </Layout>
  );
};

const getTagColor = (tag) => {
  switch (tag) {
    case 'не просмотрен':
      return 'orange';
    case 'просмотрено':
      return 'green';
    default:
      return 'blue';
  }
};

export default App;