import React from 'react';
import { Link } from 'react-router-dom';
import bookLogo from './logo.svg';

function Navbar() {
  return (
    <nav className="navbar">
      <Link to="/" className="nav-logo">
        <img src={bookLogo} alt="Libro library catalogue logo" width="40" height="40" />
        <span>Libro</span>
      </Link>
      <div className="nav-links">
        <Link to="/"> Каталог книг</Link>
        <Link to="/categories"> Категорії</Link>
        <Link to="/authors"> Автори</Link>
      </div>
    </nav>
  );
}

export default Navbar;
