import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

function AuthorsPage() {
  const [authors, setAuthors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    setLoading(true);
    api.get('/authors', { params: { page, limit: 12, search: search || undefined } })
      .then(res => {
        setAuthors(res.data.content || []);
        setTotalPages(res.data.totalPages || 1);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error:', err);
        setLoading(false);
      });
  }, [page, search]);

  return (
    <div>
      <h1> Автори</h1>

      <div className="filters-panel">
        <input
          type="text"
          className="search-input"
          placeholder="Пошук автора..."
          value={search}
          onChange={e => { setSearch(e.target.value); setPage(1); }}
        />
      </div>

      {loading ? (
        <p>Завантаження...</p>
      ) : (
        <>
          <div className="books-grid">
            {authors.map(author => (
              <Link key={author.id} to={`/authors/${author.id}`} style={{ textDecoration: 'none' }}>
                <div className="book-card">
                  <h3>{author.firstName} {author.lastName}</h3>
                  {author.nationality && <p className="book-meta"><strong>Національність:</strong> {author.nationality}</p>}
                  {author.birthDate && <p className="book-meta"><strong>Дата народження:</strong> {author.birthDate}</p>}
                  {author.biography && <p className="book-subtitle">{author.biography.slice(0, 120)}...</p>}
                </div>
              </Link>
            ))}
          </div>

          {authors.length === 0 && <p>Авторів не знайдено</p>}

          <div className="pagination">
            <button className="page-btn" disabled={page === 1} onClick={() => setPage(p => p - 1)}>Назад</button>
            <span>Сторінка {page} з {totalPages}</span>
            <button className="page-btn" disabled={page >= totalPages} onClick={() => setPage(p => p + 1)}>Вперед</button>
          </div>
        </>
      )}
    </div>
  );
}

export default AuthorsPage;
