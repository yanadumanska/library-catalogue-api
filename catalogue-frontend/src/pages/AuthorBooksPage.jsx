import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import BookCard from '../components/books/BookCard';

function AuthorBooksPage() {
  const { authorId } = useParams();
  const [author, setAuthor] = useState(null);
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get(`/authors/${authorId}`),
      api.get(`/authors/${authorId}/books`)
    ])
      .then(([authorRes, booksRes]) => {
        setAuthor(authorRes.data);
        setBooks(booksRes.data.content || []);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error:', err);
        setLoading(false);
      });
  }, [authorId]);

  if (loading) return <p>Завантаження...</p>;
  if (!author) return <p>Автора не знайдено</p>;

  return (
    <div>
      <Link to="/authors" style={{ color: '#3498db', textDecoration: 'none' }}>
        ← Назад до авторів
      </Link>
      <h1>✍️ {author.firstName} {author.lastName}</h1>
      {author.nationality && <p className="book-meta"><strong>Національність:</strong> {author.nationality}</p>}
      {author.birthDate && <p className="book-meta"><strong>Дата народження:</strong> {author.birthDate}</p>}
      {author.biography && <p style={{ color: '#888', marginBottom: '20px' }}>{author.biography}</p>}

      <h3>📚 Книги автора</h3>
      {books.length === 0 ? (
        <p>Немає книг цього автора</p>
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

export default AuthorBooksPage;
