import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    api.post('/users/login', { email, password })
      .then(res => {
        localStorage.setItem('token', res.data.accessToken);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        navigate('/');
      })
      .catch(err => {
        setError(err.response?.data?.message || 'Помилка входу');
      });
  };

  return (
    <div style={{ maxWidth: '400px', margin: '40px auto' }}>
      <h1>Вхід</h1>
      <form onSubmit={handleSubmit} className="book-card">
        {error && <p style={{ color: 'red', marginBottom: '15px' }}>{error}</p>}
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          required
          className="search-input"
          style={{ marginBottom: '10px' }}
        />
        <input
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
          className="search-input"
          style={{ marginBottom: '15px' }}
        />
        <button type="submit" className="page-btn" style={{ width: '100%' }}>Увійти</button>
      </form>
      <p style={{ textAlign: 'center', marginTop: '15px' }}>
        Немає акаунту? <Link to="/register" style={{ color: '#3498db' }}>Зареєструватись</Link>
      </p>
    </div>
  );
}

export default LoginPage;
