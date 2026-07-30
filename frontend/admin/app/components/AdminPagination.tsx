"use client";

type AdminPaginationProps = {
  page: number;
  pageSize: number;
  total: number;
  loading?: boolean;
  onPageChange: (page: number) => void;
  onPageSizeChange: (pageSize: number) => void;
};

export function AdminPagination({
  page,
  pageSize,
  total,
  loading = false,
  onPageChange,
  onPageSizeChange,
}: AdminPaginationProps) {
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  const currentPage = Math.min(page, totalPages);

  return (
    <div className="pagination">
      <label>
        Rows per page
        <select
          value={pageSize}
          onChange={(event) => onPageSizeChange(Number(event.target.value))}
          aria-label="Rows per page"
        >
          {[10, 20, 50].map((size) => (
            <option value={size} key={size}>
              {size}
            </option>
          ))}
        </select>
      </label>
      <p>
        Page {currentPage} of {totalPages}
      </p>
      <div>
        <button
          type="button"
          aria-label="Previous page"
          disabled={currentPage <= 1 || loading}
          onClick={() => onPageChange(currentPage - 1)}
        >
          ←
        </button>
        <button
          type="button"
          aria-label="Next page"
          disabled={currentPage >= totalPages || loading}
          onClick={() => onPageChange(currentPage + 1)}
        >
          →
        </button>
      </div>
    </div>
  );
}
