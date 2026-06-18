import React, { useState, useEffect } from 'react';
import api from '../services/api';
import BookCard from '../components/books/BookCard';

function HomePage() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  const [page, setPage] = useState(1); 
  const [totalPages, setTotalPages] = useState(1);

  const [search, setSearch] = useState('');
  const [format, setFormat] = useState('');
  const [status, setStatus] = useState('');
  const [language, setLanguage] = useState('');
  const [author, setAuthor] = useState('');
  const [category, setCategory] = useState('');
  const [minRating, setMinRating] = useState('');
  const [publishedAfter, setPublishedAfter] = useState('');
  const [publishedBefore, setPublishedBefore] = useState('');
  
  const [sortBy, setSortBy] = useState('title:asc'); 

  useEffect(() => {
    setLoading(true);
    
    const params = {
      page: page,
      limit: 9,
      sort: sortBy
    };

    if (search) params.search = search;
    if (format) params.format = format;
    if (status) params.status = status;
    if (language) params.language = language;
    if (author) params.author = author;
    if (category) params.category = category;
    if (minRating) params.minRating = minRating;
    if (publishedAfter) params.publishedAfter = publishedAfter;
    if (publishedBefore) params.publishedBefore = publishedBefore;

    api.get('/books', { params })
      .then(response => {
        const dataNode = response.data;
        
        setBooks(dataNode.content || []);
        setTotalPages(dataNode.totalPages || 1);
        setLoading(false);
      })
      .catch(error => {
        console.error('Помилка завантаження книг:', error);
        setLoading(false);
      });
  }, [page, search, format, status, language, author, category, minRating, publishedAfter, publishedBefore, sortBy]);

  return (
    <div>
      <h1> Каталог книг</h1>

      {/* Панель пошуку та фільтрації */}
      <div className="filters-panel">
        <input
          type="text"
          className="search-input"
          placeholder="Пошук за назвою, підзаголовком, описом або ISBN..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(1); }}
        />

        <div className="filters-row">
          {/* Фільтр 1: Формат */}
          <select className="filter-select" value={format} onChange={(e) => { setFormat(e.target.value); setPage(1); }}>
            <option value="">Усі формати</option>
            <option value="HARDCOVER">Тверда обкладинка</option>
            <option value="PAPERBACK">М'яка обкладинка</option>
            <option value="EBOOK">Електронна книга</option>
            <option value="AUDIOBOOK">Аудіокнига</option>
          </select>

          {/* Фільтр 2: Статус */}
          <select className="filter-select" value={status} onChange={(e) => { setStatus(e.target.value); setPage(1); }}>
            <option value="">Усі статуси</option>
            <option value="AVAILABLE"> Доступна</option>
            <option value="BORROWED"> Зайнята</option>
            <option value="RESERVED"> Зарезервована</option>
          </select>

          {/* Фільтр 3: Мова */}
          <select className="filter-select" value={language} onChange={(e) => { setLanguage(e.target.value); setPage(1); }}>
            <option value="">Усі мови</option>
            <option value="uk">Українська (UK)</option>
            <option value="en">Англійська (EN)</option>
          </select>

          {/* Фільтр 4: Мінімальний рейтинг */}
          <select className="filter-select" value={minRating} onChange={(e) => { setMinRating(e.target.value); setPage(1); }}>
            <option value="">Рейтинг від...</option>
            <option value="5">⭐ 5.0</option>
            <option value="4">⭐ 4.0 й вище</option>
            <option value="3">⭐ 3.0 й вище</option>
          </select>

          <input 
            type="text" 
            className="filter-select" 
            placeholder="Ім'я автора..." 
            value={author} 
            onChange={(e) => { setAuthor(e.target.value); setPage(1); }} 
          />

          <input 
            type="text" 
            className="filter-select" 
            placeholder="Категорія..." 
            value={category} 
            onChange={(e) => { setCategory(e.target.value); setPage(1); }} 
          />

          <select className="filter-select" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            <option value="title:asc">Назва (А-Я)</option>
            <option value="title:desc">Назва (Я-А)</option>
            <option value="averageRating:desc">Спочатку найрейтинговіші</option>
            <option value="publicationDate:desc">Новинки</option>
          </select>
        </div>
      </div>

      {/* Список книг */}
      {loading ? (
        <p>Завантаження даних...</p>
      ) : (
        <>
          <div className="books-grid">
            {books.map(book => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>

          {books.length === 0 && <p>Книг не знайдено за заданими фільтрами.</p>}

          <div className="pagination">
            <button className="page-btn" disabled={page === 1} onClick={() => setPage(p => p - 1)}>
              Назад
            </button>
            <span>Сторінка {page} з {totalPages}</span>
            <button className="page-btn" disabled={page >= totalPages} onClick={() => setPage(p => p + 1)}>
              Вперед
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default HomePage;
