export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  const pages = Array.from({ length: totalPages }, (_, i) => i);

  return (
    <div className="d-flex justify-content-center gap-2 mt-4">
      <button
        className="btn-ghost"
        style={{ padding: '0.4rem 0.9rem' }}
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
      >
        Prev
      </button>
      {pages.map((p) => (
        <button
          key={p}
          onClick={() => onPageChange(p)}
          style={{
            width: '38px',
            height: '38px',
            borderRadius: '8px',
            border: p === page ? 'none' : '1.5px solid var(--line)',
            background: p === page ? 'var(--chili)' : 'transparent',
            color: p === page ? '#fff' : 'var(--ink)',
            fontWeight: 600,
            cursor: 'pointer',
          }}
        >
          {p + 1}
        </button>
      ))}
      <button
        className="btn-ghost"
        style={{ padding: '0.4rem 0.9rem' }}
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}