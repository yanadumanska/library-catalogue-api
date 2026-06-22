import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import BookCard from '../components/books/BookCard';

function CategoryBooksPage() {
  const { categoryId } = useParams();
  const [category, setCategory] = useState(null);
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get(`/categories/${categoryId}`),
      api.get(`/categories/${categoryId}/books`)
    ])
      .then(([catRes, booksRes]) => {
        setCategory(catRes.data);
        setBooks(booksRes.data.content || []);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error:', err);
        setLoading(false);
      });
  }, [categoryId]);

  if (loading) return <p>Завантаження...</p>;
  if (!category) return <p>Категорію не знайдено</p>;

  return (
    <div>
      <Link to="/categories" style={{ color: '#3498db', textDecoration: 'none' }}>
        ← Назад до категорій
      </Link>
      <h1>📂 {category.name}</h1>
      <p style={{ color: '#888', marginBottom: '20px' }}>{category.description || 'Опис відсутній'}</p>
      
      {books.length === 0 ? (
        <p>Немає книг у цій категорії</p>
      ) : (
        <div className="books-grid">
          {books.map(book => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      )}
    </div>
  );
}

export default CategoryBooksPage;
