export type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
};

export type QuestionReview = {
  id: string;
  questionId: string;
  questionTitle: string;
  reviewerId: string;
  reviewerName?: string | null;
  reviewStatus: number;
  reviewStatusDesc?: string | null;
  reviewMessage?: string | null;
  reviewTime?: string | null;
};

export type PendingQuestionReview = QuestionReview & {
  questionContent: string;
  questionAnswer: string;
  questionTags?: string[] | null;
  questionDifficulty?: number | null;
  questionCreateTime?: string | null;
};

export function compactPayload(value: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(value).filter(
      ([, field]) => field !== "" && field !== null && field !== undefined,
    ),
  );
}

export function formatAdminDate(value?: string | null) {
  if (!value) return "Not recorded";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export function questionDifficultyLabel(value?: number | null) {
  if (value === 0) return "Easy";
  if (value === 1) return "Medium";
  if (value === 2 || value === 3) return "Hard";
  return "Unset";
}

export function questionDifficultyClass(value?: number | null) {
  if (value === 0) return 1;
  if (value === 1) return 2;
  if (value === 2 || value === 3) return 3;
  return 0;
}
