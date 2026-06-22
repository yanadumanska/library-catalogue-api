import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

function ProfilePage() {
  const [borrowings, setBorrowings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const token = localStorage.getItem('token');

  useEffect(() => {
    if (message) {
      const timer = setTimeout(() => setMessage(''), 3000);
      return () => clearTimeout(timer);
    }
  }, [message]);

  const loadBorrowings = () => {
    if (!token) return;
    api.get('/borrowings', {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => {
        setBorrowings(res.data || []);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error:', err);
        setLoading(false);
      });
  };

  useEffect(() => {
    loadBorrowings();
  }, []);

  const handleReturn = (borrowingId) => {
    api.post(`/borrowings/${borrowingId}/return`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(() => {
        setMessage('Книгу повернено');
        loadBorrowings();
      })
      .catch(err => {
        setMessage('❌ ' + (err.response?.data?.message || 'Помилка'));
      });
  };

  const handleRenew = (borrowingId) => {
    api.post(`/borrowings/${borrowingId}/renew`, {}, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(() => {
        setMessage('Позику продовжено на 14 днів');
        loadBorrowings();
      })
      .catch(err => {
        setMessage('❌ ' + (err.response?.data?.message || 'Помилка'));
      });
  };

  return (
    <div>
      <h1>Особистий кабінет</h1>

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

      <div className="book-card" style={{ marginBottom: '30px' }}>
        <h3>{user.fullName || user.username}</h3>
        <p className="book-meta"><strong>Email:</strong> {user.email}</p>
        <p className="book-meta"><strong>Роль:</strong> {user.role}</p>
      </div>

      <h2>Мої позики</h2>
      {loading ? (
        <p>Завантаження...</p>
      ) : borrowings.length === 0 ? (
        <p>У вас немає активних позик</p>
      ) : (
        borrowings.map(b => (
          <div key={b.id} className="book-card" style={{ marginBottom: '15px' }}>
            <h3>{b.bookTitle}</h3>
            <p className="book-meta">
              <strong>Статус:</strong>{' '}
              {b.status === 'ACTIVE' ? '🟢 Активна' : b.status === 'RETURNED' ? '✅ Повернена' : '🔴 Прострочена'}
            </p>
            <p className="book-meta"><strong>Взято:</strong> {new Date(b.borrowDate).toLocaleDateString('uk-UA')}</p>
            <p className="book-meta"><strong>Повернути до:</strong> {new Date(b.dueDate).toLocaleDateString('uk-UA')}</p>
            {b.fineAmount > 0 && <p className="book-meta"><strong>Штраф:</strong> {b.fineAmount} грн</p>}
            {b.status === 'ACTIVE' && (
              <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                <button className="page-btn" onClick={() => handleReturn(b.id)}>Повернути</button>
                <button className="page-btn" onClick={() => handleRenew(b.id)}>Продовжити</button>
              </div>
            )}
          </div>
        ))
      )}
    </div>
  );
}

export default ProfilePage;
