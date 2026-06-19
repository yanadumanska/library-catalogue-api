import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import bookLogo from './logo.svg';

function Navbar() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="nav-logo">
        <img src={bookLogo} alt="Libro" width="40" height="40" />
        <span>Libro</span>
      </Link>
      <div className="nav-links">
        <Link to="/">Каталог</Link>
        <Link to="/categories">Категорії</Link>
        <Link to="/authors">Автори</Link>
        {user.role === 'ADMIN' || user.role === 'LIBRARIAN' ? (
          <Link to="/admin">Адмін-панель</Link>
        ) : null}
        {token ? (
          <>
            <Link to="/profile">👤 {user.username}</Link>
            <button onClick={handleLogout} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#627588', fontWeight: 600, fontSize: '15px' }}>
              Вийти
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Вхід</Link>
          </>
        )}
      </div>
    </nav>
  );
}

export default Navbar;
