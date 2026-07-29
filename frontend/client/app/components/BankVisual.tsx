export function BankVisual({
  label,
}: {
  label: string;
}) {
  return (
    <>
      <span>{label.slice(0, 2)}</span>
      <i aria-hidden="true" />
    </>
  );
}
