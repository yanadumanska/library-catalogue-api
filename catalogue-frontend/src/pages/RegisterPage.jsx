import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');

    api.post('/users/register', { username, email, password, fullName })
      .then(res => {
        localStorage.setItem('token', res.data.accessToken);
        localStorage.setItem('user', JSON.stringify(res.data.user));
        navigate('/');
      })
      .catch(err => {
        setError(err.response?.data?.message || 'Помилка реєстрації');
      });
  };

  return (
    <div style={{ maxWidth: '400px', margin: '40px auto' }}>
      <h1>Реєстрація</h1>
      <form onSubmit={handleSubmit} className="book-card">
        {error && <p style={{ color: 'red', marginBottom: '15px' }}>{error}</p>}
        <input
          type="text"
          placeholder="Ім'я"
          value={fullName}
          onChange={e => setFullName(e.target.value)}
          required
          className="search-input"
          style={{ marginBottom: '10px' }}
        />
        <input
          type="text"
          placeholder="Логін"
          value={username}
          onChange={e => setUsername(e.target.value)}
          required
          className="search-input"
          style={{ marginBottom: '10px' }}
        />
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
          placeholder="Пароль (мін 6 символів)"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
          className="search-input"
          style={{ marginBottom: '15px' }}
        />
        <button type="submit" className="page-btn" style={{ width: '100%' }}>Зареєструватись</button>
      </form>
      <p style={{ textAlign: 'center', marginTop: '15px' }}>
        Вже є акаунт? <Link to="/login" style={{ color: '#3498db' }}>Увійти</Link>
      </p>
    </div>
  );
}

export default RegisterPage;
