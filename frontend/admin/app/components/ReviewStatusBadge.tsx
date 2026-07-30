type ReviewStatusBadgeProps = {
  status: number;
  description?: string | null;
};

export function reviewStatusLabel(
  status: number,
  description?: string | null,
) {
  return (
    description ||
    ({ 0: "Pending", 1: "Approved", 2: "Rejected" }[status] ?? "Unknown")
  );
}

export function ReviewStatusBadge({
  status,
  description,
}: ReviewStatusBadgeProps) {
  const normalizedStatus = [0, 1, 2].includes(status) ? status : 3;

  return (
    <span className={`review-status review-status--${normalizedStatus}`}>
      <i aria-hidden="true" />
      {reviewStatusLabel(status, description)}
    </span>
  );
}
