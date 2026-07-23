import { useState, useMemo } from 'react';
import { Edit2, Trash2, Search, ChevronLeft, ChevronRight, Inbox } from 'lucide-react';
import Button from './Button';

const Table = ({ columns, data = [], onEdit, onDelete, onView }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // Search logic
  const filteredData = useMemo(() => {
    if (!searchTerm) return data;
    const lowerSearch = searchTerm.toLowerCase();
    return data.filter(row => {
      return Object.values(row).some(val => 
        String(val).toLowerCase().includes(lowerSearch)
      );
    });
  }, [data, searchTerm]);

  // Pagination logic
  const totalPages = Math.max(1, Math.ceil(filteredData.length / itemsPerPage));
  const currentData = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return filteredData.slice(start, start + itemsPerPage);
  }, [filteredData, currentPage]);

  // Reset page when searching
  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1);
  };

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
        <div className="input-group" style={{ marginBottom: 0, width: '250px', position: 'relative' }}>
          <Search size={16} style={{ position: 'absolute', left: '12px', top: '10px', color: 'var(--text-muted)' }} />
          <input 
            type="text" 
            className="input-field" 
            placeholder="Search..." 
            value={searchTerm}
            onChange={handleSearch}
            style={{ paddingLeft: '36px' }}
          />
        </div>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)' }}>
              {columns.map((col, index) => (
                <th key={index} style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  {col.header}
                </th>
              ))}
              {(onEdit || onDelete) && (
                <th style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontWeight: 600, fontSize: '13px', textTransform: 'uppercase', letterSpacing: '0.05em', textAlign: 'right' }}>
                  Actions
                </th>
              )}
            </tr>
          </thead>
          <tbody>
            {currentData.length > 0 ? (
              currentData.map((row, rowIndex) => (
                <tr 
                  key={rowIndex} 
                  onClick={() => onView && onView(row)}
                  style={{ 
                    borderBottom: '1px solid var(--border-color)', 
                    transition: 'background-color 0.2s',
                    cursor: onView ? 'pointer' : 'default'
                  }}
                  className={onView ? 'hover-bg' : ''}
                >
                  {columns.map((col, colIndex) => (
                    <td key={colIndex} style={{ padding: '16px', fontSize: '14px', color: 'var(--text-primary)' }}>
                      {col.render ? col.render(row) : row[col.accessor]}
                    </td>
                  ))}
                  {(onEdit || onDelete) && (
                    <td style={{ padding: '16px', textAlign: 'right' }}>
                      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                        {onEdit && (
                          <button onClick={(event) => { event.stopPropagation(); onEdit(row); }} aria-label="Edit" title="Edit" style={{ padding: '6px', background: 'transparent', border: 'none', color: 'var(--primary)', cursor: 'pointer', borderRadius: '4px' }} className="hover-bg">
                            <Edit2 size={16} />
                          </button>
                        )}
                        {onDelete && (
                          <button onClick={(event) => { event.stopPropagation(); onDelete(row); }} aria-label="Delete" title="Delete" style={{ padding: '6px', background: 'transparent', border: 'none', color: 'var(--danger)', cursor: 'pointer', borderRadius: '4px' }} className="hover-bg">
                            <Trash2 size={16} />
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={columns.length + (onEdit || onDelete ? 1 : 0)} style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
                  <Inbox size={48} style={{ margin: '0 auto', opacity: 0.2, marginBottom: '12px' }} />
                  <p>No data available</p>
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {filteredData.length > 0 && (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '16px', padding: '16px 0 0 0', borderTop: '1px solid var(--border-color)' }}>
          <div style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
            Showing {(currentPage - 1) * itemsPerPage + 1} to {Math.min(currentPage * itemsPerPage, filteredData.length)} of {filteredData.length} entries
          </div>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <Button 
              variant="secondary" 
              size="sm" 
              onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
              disabled={currentPage === 1}
            >
              <ChevronLeft size={16} />
            </Button>
            <span style={{ fontSize: '13px', fontWeight: 600 }}>Page {currentPage} of {totalPages}</span>
            <Button 
              variant="secondary" 
              size="sm" 
              onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
              disabled={currentPage === totalPages}
            >
              <ChevronRight size={16} />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Table;
