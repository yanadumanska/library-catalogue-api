
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/ui/Navbar';
import HomePage from './pages/HomePage';
import CategoriesPage from './pages/CategoriesPage';
import CategoryBooksPage from './pages/CategoryBooksPage';
import AuthorsPage from './pages/AuthorsPage';
import AuthorBooksPage from './pages/AuthorBooksPage';
import BookDetailPage from './pages/BookDetailPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import './App.css';

function App() {
  return (
    <Router>
      <Navbar />
      <div className="container">
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/books/:bookId" element={<BookDetailPage />} />
        <Route path="/categories/:categoryId/books" element={<CategoryBooksPage />} />
        <Route path="/categories" element={<CategoriesPage />} />
        <Route path="/authors/:authorId" element={<AuthorBooksPage />} />
        <Route path="/authors" element={<AuthorsPage />} />
        <Route path="/admin" element={<AdminPage />} />
      </Routes>
      </div>
    </Router>
  );
}

export default App;
