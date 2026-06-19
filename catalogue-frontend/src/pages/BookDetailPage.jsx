import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';

function BookDetailPage() {
  const { bookId } = useParams();
  const [book, setBook] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  
  const [showForm, setShowForm] = useState(false);
  const [rating, setRating] = useState(5);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

    const loadData = () => {
    const ts = Date.now();
    Promise.all([
        api.get(`/books/${bookId}?_t=${ts}`),
        api.get(`/books/${bookId}/reviews?_t=${ts}`)
    ])
        .then(([bookRes, reviewsRes]) => {
        setBook(bookRes.data);
        setReviews(reviewsRes.data || []);
        setLoading(false);
        })
        .catch(err => {
        console.error('Error:', err);
        setLoading(false);
        });
    };

  useEffect(() => {
    loadData();
  }, [bookId]);

  const handleBorrow = () => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Увійдіть щоб взяти книгу');
      return;
    }

    api.post('/borrowings', 
      { bookId, expectedDurationDays: 14 },
      { headers: { Authorization: `Bearer ${token}` } }
    )
      .then(() => {
        setMessage('✅ Книгу успішно взято!');
        loadData();
      })
      .catch(err => {
        setMessage('❌ ' + (err.response?.data?.message || 'Помилка'));
      });
  };

  const handleSubmitReview = (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Увійдіть щоб залишити відгук');
      return;
    }

    api.post(`/books/${bookId}/reviews`, 
      { rating, title, content },
      { headers: { Authorization: `Bearer ${token}` } }
    )
      .then(() => {
        setShowForm(false);
        setRating(5);
        setTitle('');
        setContent('');
        loadData();
      })
      .catch(err => {
        alert('Помилка: ' + (err.response?.data?.message || 'Не вдалося додати відгук'));
      });
  };

  if (loading) return <p>Завантаження...</p>;
  if (!book) return <p>Книгу не знайдено</p>;

  return (
    <div>
      <Link to="/" style={{ color: '#3498db', textDecoration: 'none' }}>
        ← Назад до каталогу
      </Link>
      
      <div className="book-card" style={{ marginTop: '20px' }}>
        <h1>{book.title}</h1>
        {book.subtitle && <p className="book-subtitle">{book.subtitle}</p>}
        
        <p className="book-meta"><strong>ISBN:</strong> {book.isbn}</p>
        <p className="book-meta"><strong>Формат:</strong> {book.format}</p>
        <p className="book-meta"><strong>Статус:</strong> {book.status === 'AVAILABLE' ? 'Доступна' : 'Зайнята'}</p>
        <p className="book-meta"><strong>Рейтинг:</strong> ⭐ {book.averageRating || 'Немає оцінок'}</p>
        <p className="book-meta"><strong>Копій:</strong> {book.availableCopies} / {book.totalCopies}</p>
        {book.description && <p style={{ marginTop: '15px', color: '#555' }}>{book.description}</p>}
        
        {book.authors?.length > 0 && (
          <p className="book-meta">
            <strong>Автори:</strong>{' '}
            {book.authors.map((a, i) => (
              <span key={a.id}>
                <Link to={`/authors/${a.id}`} style={{ color: '#3498db' }}>{a.firstName} {a.lastName}</Link>
                {i < book.authors.length - 1 ? ', ' : ''}
              </span>
            ))}
          </p>
        )}
        
        {book.categories?.length > 0 && (
          <p className="book-meta">
            <strong>Категорії:</strong>{' '}
            {book.categories.map((c, i) => (
              <span key={c.id}>
                <Link to={`/categories/${c.id}/books`} style={{ color: '#3498db' }}>{c.name}</Link>
                {i < book.categories.length - 1 ? ', ' : ''}
              </span>
            ))}
          </p>
        )}

        {book.status === 'AVAILABLE' && (
          <button className="page-btn" onClick={handleBorrow} style={{ marginTop: '15px' }}>
            Взяти книгу
          </button>
        )}
        {message && <p style={{ marginTop: '10px', fontWeight: 'bold' }}>{message}</p>}
      </div>

      {/* Відгуки */}
      <div style={{ marginTop: '40px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Відгуки</h2>
        <button className="page-btn" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Скасувати' : '+ Додати відгук'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmitReview} className="book-card" style={{ marginBottom: '20px' }}>
          <div style={{ marginBottom: '15px' }}>
            <label>Рейтинг: </label>
            <select value={rating} onChange={e => setRating(Number(e.target.value))} className="filter-select">
              <option value="5">⭐ 5</option>
              <option value="4">⭐ 4</option>
              <option value="3">⭐ 3</option>
              <option value="2">⭐ 2</option>
              <option value="1">⭐ 1</option>
            </select>
          </div>
          <input
            type="text"
            placeholder="Заголовок (необов'язково)"
            value={title}
            onChange={e => setTitle(e.target.value)}
            className="search-input"
            style={{ marginBottom: '10px' }}
          />
          <textarea
            placeholder="Ваш відгук..."
            value={content}
            onChange={e => setContent(e.target.value)}
            required
            rows="4"
            className="search-input"
            style={{ marginBottom: '10px' }}
          />
          <button type="submit" className="page-btn">Надіслати</button>
        </form>
      )}

      {reviews.length === 0 ? (
        <p>Ще немає відгуків</p>
      ) : (
        reviews.map(review => (
          <div key={review.id} className="book-card" style={{ marginBottom: '15px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <strong>⭐ {review.rating}/5</strong>
              <span style={{ color: '#888', fontSize: '14px' }}>
                {new Date(review.createdAt).toLocaleDateString('uk-UA')}
              </span>
            </div>
            {review.title && <h3 style={{ margin: '8px 0' }}>{review.title}</h3>}
            <p style={{ color: '#555' }}>{review.content}</p>
          </div>
        ))
      )}
    </div>
  );
}

export default BookDetailPage;
