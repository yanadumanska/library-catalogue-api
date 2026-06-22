import React, { useState, useEffect } from 'react';
import api from '../services/api';

function AdminPage() {
  const token = localStorage.getItem('token');
  const [users, setUsers] = useState([]);
  const [books, setBooks] = useState([]);
  const [authors, setAuthors] = useState([]);
  const [tab, setTab] = useState('users');
  const [message, setMessage] = useState('');

  const [newBook, setNewBook] = useState({
    isbn: '', title: '', format: 'PAPERBACK', totalCopies: 1,
    availableCopies: 1, language: 'uk', description: '', authorIds: []
  });

  const [newAuthor, setNewAuthor] = useState({
    firstName: '', lastName: '', biography: '', nationality: ''
  });

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  useEffect(() => {
    if (tab === 'users') loadUsers();
    if (tab === 'books') loadBooks();
    if (tab === 'add-book') loadAuthors();
  }, [tab]);

  const loadUsers = () => {
    api.get('/users', { headers: { Authorization: `Bearer ${token}` } })
      .then(res => setUsers(res.data || []))
      .catch(err => console.error(err));
  };

  const loadBooks = () => {
    api.get('/books?limit=100', { headers: { Authorization: `Bearer ${token}` } })
      .then(res => setBooks(res.data.content || []))
      .catch(err => console.error(err));
  };

  const loadAuthors = () => {
    api.get('/authors?limit=100')
      .then(res => setAuthors(res.data.content || []))
      .catch(err => console.error(err));
  };

  const updateUserStatus = (userId, status, role) => {
    api.patch(`/users/${userId}/status`, { status, role },
      { headers: { Authorization: `Bearer ${token}` } })
      .then(() => {
        setMessage('Роль користувача оновлено');
        loadUsers();
      })
      .catch(err => setMessage('Помилка оновлення'));
  };

  const deleteBook = (bookId) => {
    if (!window.confirm('Видалити книгу?')) return;
    api.delete(`/books/${bookId}`, { headers: { Authorization: `Bearer ${token}` } })
      .then(() => {
        setMessage('Книгу видалено');
        loadBooks();
      })
      .catch(err => setMessage('Помилка видалення'));
  };

  const addBook = (e) => {
    e.preventDefault();
    const data = { ...newBook, isbn: '978' + Date.now().toString().slice(-10) };
    api.post('/books', data, { headers: { Authorization: `Bearer ${token}` } })
      .then(() => {
        setMessage('Книгу успішно додано');
        setNewBook({ isbn: '', title: '', format: 'PAPERBACK', totalCopies: 1, availableCopies: 1, language: 'uk', description: '', authorIds: [] });
        loadBooks();
      })
      .catch(err => setMessage((err.response?.data?.message || 'Помилка додавання')));
  };

  const addAuthor = (e) => {
    e.preventDefault();
    api.post('/authors', newAuthor, { headers: { Authorization: `Bearer ${token}` } })
      .then(() => {
        setMessage('Автора успішно додано');
        setNewAuthor({ firstName: '', lastName: '', biography: '', nationality: '' });
        loadAuthors();
      })
      .catch(err => setMessage((err.response?.data?.message || 'Помилка додавання')));
  };

  return (
    <div>
      <h1>Адмін-панель</h1>

      {message && (
        <div style={{
          position: 'fixed',
          top: '80px',
          right: '20px',
          background: message.includes('✅') ? '#27ae60' : '#e74c3c',
          color: 'white',
          padding: '15px 25px',
          borderRadius: '10px',
          zIndex: 1000,
          boxShadow: '0 4px 15px rgba(0,0,0,0.2)',
          fontWeight: 'bold'
        }}>
          {message}
        </div>
      )}

      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button className="page-btn" onClick={() => setTab('users')}>Користувачі</button>
        <button className="page-btn" onClick={() => setTab('books')}>Книги</button>
        <button className="page-btn" onClick={() => setTab('add-book')}>Додати книгу</button>
        <button className="page-btn" onClick={() => setTab('add-author')}>Додати автора</button>
      </div>

      {tab === 'users' && (
        <div className="books-grid">
          {users.map(u => (
            <div key={u.id} className="book-card">
              <h3>{u.fullName}</h3>
              <p className="book-meta"><strong>Email:</strong> {u.email}</p>
              <p className="book-meta"><strong>Роль:</strong> {u.role}</p>
              <select
                className="filter-select"
                value={u.role}
                onChange={e => updateUserStatus(u.id, null, e.target.value)}
                style={{ marginTop: '10px' }}
              >
                <option value="PATRON">PATRON</option>
                <option value="LIBRARIAN">LIBRARIAN</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          ))}
        </div>
      )}

      {tab === 'books' && (
        <div className="books-grid">
          {books.map(b => (
            <div key={b.id} className="book-card">
              <h3>{b.title}</h3>
              <p className="book-meta"><strong>ISBN:</strong> {b.isbn}</p>
              <p className="book-meta"><strong>Статус:</strong> {b.status}</p>
              <p className="book-meta"><strong>Копій:</strong> {b.availableCopies}/{b.totalCopies}</p>
              <button className="page-btn" onClick={() => deleteBook(b.id)} style={{ marginTop: '10px', background: '#e74c3c' }}>
                Видалити
              </button>
            </div>
          ))}
        </div>
      )}

      {tab === 'add-book' && (
        <form onSubmit={addBook} className="book-card" style={{ maxWidth: '500px' }}>
          <input className="search-input" placeholder="Назва книги" value={newBook.title}
            onChange={e => setNewBook({ ...newBook, title: e.target.value })} required />
          <textarea className="search-input" placeholder="Опис" value={newBook.description || ''}
            onChange={e => setNewBook({ ...newBook, description: e.target.value })} rows="3" />
          <select className="filter-select" value={newBook.format}
            onChange={e => setNewBook({ ...newBook, format: e.target.value })}>
            <option value="PAPERBACK">М'яка</option>
            <option value="HARDCOVER">Тверда</option>
            <option value="EBOOK">Електронна</option>
            <option value="AUDIOBOOK">Аудіокнига</option>
          </select>
          <select className="filter-select" value={newBook.language}
            onChange={e => setNewBook({ ...newBook, language: e.target.value })}>
            <option value="uk">Українська</option>
            <option value="en">Англійська</option>
          </select>
          <input className="search-input" type="number" placeholder="Кількість копій" value={newBook.totalCopies}
            onChange={e => setNewBook({ ...newBook, totalCopies: +e.target.value, availableCopies: +e.target.value })} />
          <select className="filter-select" multiple value={newBook.authorIds}
            onChange={e => setNewBook({ ...newBook, authorIds: [...e.target.selectedOptions].map(o => o.value) })}
            style={{ height: '100px' }}>
            {authors.map(a => (
              <option key={a.id} value={a.id}>{a.firstName} {a.lastName}</option>
            ))}
          </select>
          <p style={{ fontSize: '12px', color: '#888', marginTop: '5px' }}>ISBN згенерується автоматично</p>
          <button type="submit" className="page-btn" style={{ marginTop: '10px' }}>Додати книгу</button>
        </form>
      )}

      {tab === 'add-author' && (
        <form onSubmit={addAuthor} className="book-card" style={{ maxWidth: '500px' }}>
          <input className="search-input" placeholder="Ім'я" value={newAuthor.firstName}
            onChange={e => setNewAuthor({ ...newAuthor, firstName: e.target.value })} required />
          <input className="search-input" placeholder="Прізвище" value={newAuthor.lastName}
            onChange={e => setNewAuthor({ ...newAuthor, lastName: e.target.value })} required />
          <textarea className="search-input" placeholder="Біографія" value={newAuthor.biography}
            onChange={e => setNewAuthor({ ...newAuthor, biography: e.target.value })} rows="3" />
          <input className="search-input" placeholder="Національність" value={newAuthor.nationality}
            onChange={e => setNewAuthor({ ...newAuthor, nationality: e.target.value })} />
          <button type="submit" className="page-btn" style={{ marginTop: '10px' }}>Додати автора</button>
        </form>
      )}
    </div>
  );
}

export default AdminPage;
