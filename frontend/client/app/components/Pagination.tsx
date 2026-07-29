export function Pagination({
  current,
  total,
  onChange,
}: {
  current: number;
  total: number;
  onChange: (page: number) => void;
}) {
  if (total <= 1) return null;

  return (
    <nav className="pagination" aria-label="分页导航" data-od-id="pagination">
      <button
        type="button"
        onClick={() => onChange(current - 1)}
        disabled={current === 1}
        aria-label="上一页"
      >
        ←
      </button>
      {Array.from({ length: total }, (_, index) => index + 1).map((page) => (
        <button
          type="button"
          key={page}
          className={page === current ? "active" : ""}
          onClick={() => onChange(page)}
          aria-current={page === current ? "page" : undefined}
        >
          {page}
        </button>
      ))}
      <button
        type="button"
        onClick={() => onChange(current + 1)}
        disabled={current === total}
        aria-label="下一页"
      >
        →
      </button>
    </nav>
  );
}
