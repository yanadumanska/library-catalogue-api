import React from 'react';
import { Link } from 'react-router-dom';

function BookCard({ book }) {
  return (
    <Link to={`/books/${book.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
    <div className="book-card">
      <h3>{book.title}</h3>
      
      <p className="book-meta"><strong>ISBN:</strong> {book.isbn}</p>
      <p className="book-meta"><strong>Формат:</strong> {book.format}</p>
      <p className="book-meta">
        <strong>Статус:</strong> {book.status === 'AVAILABLE' ? ' Доступна' : ' Зайнята'}
      </p>
      <p className="book-meta"><strong>Рейтинг:</strong> ⭐ {book.averageRating || 'Немає оцінок'}</p>
      <p className="book-meta"><strong>Копій:</strong> {book.availableCopies} / {book.totalCopies}</p>
      
      {book.authors?.length > 0 && (
        <p className="book-meta">
          <strong>Автори:</strong> {book.authors.map(a => `${a.firstName} ${a.lastName}`).join(', ')}
        </p>
      )}
    </div>
    </Link>
  );
}

export default BookCard;
