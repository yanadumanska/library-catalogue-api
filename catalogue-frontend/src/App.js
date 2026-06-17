
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/ui/Navbar';
import HomePage from './pages/HomePage';
import CategoriesPage from './pages/CategoriesPage';
import CategoryBooksPage from './pages/CategoryBooksPage';
import AuthorsPage from './pages/AuthorsPage';
import AuthorBooksPage from './pages/AuthorBooksPage';
import './App.css';

function App() {
  return (
    <Router>
      <Navbar />
      <div className="container">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/categories/:categoryId/books" element={<CategoryBooksPage />} />
          <Route path="/authors" element={<AuthorsPage />} />
          <Route path="/authors/:authorId" element={<AuthorBooksPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
