import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';

function CategoriesPage() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [flatView, setFlatView] = useState(false);

  useEffect(() => {
    api.get('/categories')
      .then(res => {
        setCategories(res.data);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error:', err);
        setLoading(false);
      });
  }, []);

  if (loading) return <p>Завантаження...</p>;

  return (
    <div>
      <h1>📂 Категорії</h1>
      
      <button 
        onClick={() => setFlatView(!flatView)} 
        style={{
          padding: '10px 20px',
          border: 'none',
          background: '#3498db',
          color: 'white',
          borderRadius: '8px',
          cursor: 'pointer',
          marginBottom: '20px'
        }}
      >
        {flatView ? '🌳 Дерево' : '📋 Плаский список'}
      </button>

      {flatView ? (
        <div className="books-grid">
          {categories.map(cat => (
            <Link 
              key={cat.id} 
              to={`/categories/${cat.id}/books`}
              style={{ textDecoration: 'none' }}
            >
              <div className="book-card">
                <h3>{cat.name}</h3>
                <p className="book-meta">{cat.description || 'Опис відсутній'}</p>
              </div>
            </Link>
          ))}
        </div>
      ) : (
        <div className="book-card">
          {categories.map(cat => (
            <CategoryTreeItem key={cat.id} category={cat} />
          ))}
        </div>
      )}
    </div>
  );
}

function CategoryTreeItem({ category, level = 0 }) {
  const [expanded, setExpanded] = useState(true);

  return (
    <div style={{ marginLeft: level * 20 }}>
      <div 
        style={{ 
          display: 'flex', 
          alignItems: 'center',
          gap: '8px',
          padding: '8px 0'
        }}
      >
        <span 
          onClick={(e) => {
            e.stopPropagation();
            setExpanded(!expanded);
          }} 
          style={{ cursor: 'pointer', userSelect: 'none' }}
        >
          {category.subcategories?.length > 0 ? (expanded ? '📂' : '📁') : '📄'}
        </span>
        <Link 
          to={`/categories/${category.id}/books`}
          style={{ 
            fontWeight: category.subcategories?.length > 0 ? 'bold' : 'normal',
            color: '#2c3e50',
            textDecoration: 'none'
          }}
        >
          {category.name}
        </Link>
      </div>
      {expanded && category.subcategories?.length > 0 && (
        <div>
          {category.subcategories.map(sub => (
            <CategoryTreeItem key={sub.id} category={sub} level={level + 1} />
          ))}
        </div>
      )}
    </div>
  );
}

export default CategoriesPage;
