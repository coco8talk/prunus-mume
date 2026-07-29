export function BrandMark({ compact = false }: { compact?: boolean }) {
  return (
    <div className={`brand-mark${compact ? " brand-mark--compact" : ""}`}>
      <span className="brand-symbol" aria-hidden="true">
        梅
      </span>
      {!compact && (
        <span className="brand-copy">
          <strong>Prunus Mume</strong>
          <small>Admin Console</small>
        </span>
      )}
    </div>
  );
}
