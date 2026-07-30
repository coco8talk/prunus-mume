"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiRequest } from "../lib/api";
import {
  compactPayload,
  formatAdminDate,
  type PageResult,
  type QuestionReview,
} from "../lib/reviews";
import { AdminPagination } from "./AdminPagination";
import { AdminShell } from "./AdminShell";
import { useAuth } from "./AuthProvider";
import { ReviewStatusBadge } from "./ReviewStatusBadge";

type SortOrder = "ascend" | "descend" | undefined;

type ReviewFilters = {
  questionId: string;
  reviewerId: string;
  reviewStatus: string;
  reviewStartTime: string;
  reviewEndTime: string;
};

const emptyFilters: ReviewFilters = {
  questionId: "",
  reviewerId: "",
  reviewStatus: "",
  reviewStartTime: "",
  reviewEndTime: "",
};

function isAuthorizationError(error: unknown) {
  return error instanceof ApiError && (error.code === 401 || error.code === 403);
}

export function ReviewHistory() {
  const router = useRouter();
  const { token, ready, user, clearSession } = useAuth();
  const [filters, setFilters] = useState<ReviewFilters>(emptyFilters);
  const [appliedFilters, setAppliedFilters] =
    useState<ReviewFilters>(emptyFilters);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState<string>();
  const [sortOrder, setSortOrder] = useState<SortOrder>();
  const [result, setResult] = useState<PageResult<QuestionReview>>({
    records: [],
    total: 0,
    size: 10,
    current: 1,
  });
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedReview, setSelectedReview] = useState<QuestionReview | null>(
    null,
  );
  const [error, setError] = useState("");

  const requestBody = useMemo(
    () =>
      compactPayload({
        current: page,
        pageSize,
        sortField,
        sortOrder,
        questionId: appliedFilters.questionId.trim(),
        reviewerId: appliedFilters.reviewerId.trim(),
        reviewStatus:
          appliedFilters.reviewStatus === ""
            ? undefined
            : Number(appliedFilters.reviewStatus),
        reviewStartTime: appliedFilters.reviewStartTime,
        reviewEndTime: appliedFilters.reviewEndTime,
      }),
    [appliedFilters, page, pageSize, sortField, sortOrder],
  );

  const handleRequestError = useCallback(
    (requestError: unknown, fallback: string) => {
      if (isAuthorizationError(requestError)) {
        clearSession();
        router.replace("/login");
        return;
      }
      setError(requestError instanceof Error ? requestError.message : fallback);
    },
    [clearSession, router],
  );

  const loadHistory = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError("");
    try {
      const response = await apiRequest<PageResult<QuestionReview>>(
        "/question-reviews/search",
        { method: "POST", body: JSON.stringify(requestBody) },
        token,
      );
      setResult(response.data);
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load review history.");
    } finally {
      setLoading(false);
    }
  }, [handleRequestError, requestBody, token, user]);

  useEffect(() => {
    if (!ready || !user) return;
    const timer = window.setTimeout(() => void loadHistory(), 0);
    return () => window.clearTimeout(timer);
  }, [loadHistory, ready, user]);

  function applyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    for (const [label, value] of [
      ["Question ID", filters.questionId],
      ["Reviewer ID", filters.reviewerId],
    ]) {
      if (value && !/^\d+$/.test(value)) {
        setError(`${label} must contain digits only.`);
        return;
      }
    }
    if (
      filters.reviewStartTime &&
      filters.reviewEndTime &&
      filters.reviewStartTime > filters.reviewEndTime
    ) {
      setError("The start time must be earlier than the end time.");
      return;
    }
    setError("");
    setPage(1);
    setAppliedFilters(filters);
  }

  function clearFilters() {
    setFilters(emptyFilters);
    setAppliedFilters(emptyFilters);
    setPage(1);
    setError("");
  }

  function changeSort(field: string) {
    setPage(1);
    if (sortField !== field) {
      setSortField(field);
      setSortOrder("ascend");
    } else if (sortOrder === "ascend") {
      setSortOrder("descend");
    } else if (sortOrder === "descend") {
      setSortField(undefined);
      setSortOrder(undefined);
    } else {
      setSortOrder("ascend");
    }
  }

  function sortIndicator(field: string) {
    if (sortField !== field) return "↕";
    return sortOrder === "ascend" ? "↑" : "↓";
  }

  async function showLatestReview(questionId: string) {
    setDetailLoading(true);
    setError("");
    try {
      const response = await apiRequest<QuestionReview>(
        `/question-reviews/questions/${encodeURIComponent(questionId)}`,
        { method: "GET" },
        token,
      );
      setSelectedReview(response.data);
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load the latest review.");
    } finally {
      setDetailLoading(false);
    }
  }

  return (
    <AdminShell>
      <main className="users-page review-history-page">
        <section className="users-heading">
          <div>
            <p className="eyebrow">Audit trail</p>
            <h1>Review history</h1>
            <p>Trace every decision by question, reviewer, status, or time.</p>
          </div>
        </section>

        <section className="filter-card" aria-labelledby="review-filter-title">
          <div className="filter-card__heading">
            <div>
              <h2 id="review-filter-title">Filter decisions</h2>
              <p>Combine identifiers, outcome, and review window.</p>
            </div>
            <button type="button" className="text-button" onClick={clearFilters}>
              Clear filters
            </button>
          </div>
          <form
            className="filter-grid filter-grid--reviews"
            onSubmit={applyFilters}
          >
            <label>
              <span>Question ID</span>
              <input
                inputMode="numeric"
                value={filters.questionId}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    questionId: event.target.value,
                  }))
                }
                placeholder="Question ID"
              />
            </label>
            <label>
              <span>Reviewer ID</span>
              <input
                inputMode="numeric"
                value={filters.reviewerId}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    reviewerId: event.target.value,
                  }))
                }
                placeholder="Admin user ID"
              />
            </label>
            <label>
              <span>Status</span>
              <select
                value={filters.reviewStatus}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    reviewStatus: event.target.value,
                  }))
                }
              >
                <option value="">All statuses</option>
                <option value="0">Pending</option>
                <option value="1">Approved</option>
                <option value="2">Rejected</option>
              </select>
            </label>
            <label>
              <span>From</span>
              <input
                type="datetime-local"
                value={filters.reviewStartTime}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    reviewStartTime: event.target.value,
                  }))
                }
              />
            </label>
            <label>
              <span>Until</span>
              <input
                type="datetime-local"
                value={filters.reviewEndTime}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    reviewEndTime: event.target.value,
                  }))
                }
              />
            </label>
            <button className="filter-submit" type="submit">
              Search
            </button>
          </form>
        </section>

        <section className="table-card" aria-labelledby="review-history-title">
          <div className="table-card__heading">
            <div>
              <h2 id="review-history-title">Decision log</h2>
              <p>
                {loading
                  ? "Loading decisions…"
                  : `${result.total.toLocaleString()} matching ${
                      result.total === 1 ? "decision" : "decisions"
                    }`}
              </p>
            </div>
            <button
              className="refresh-button"
              type="button"
              onClick={() => void loadHistory()}
              disabled={loading}
            >
              <span className={loading ? "spin" : ""} aria-hidden="true">↻</span>
              Refresh
            </button>
          </div>

          {error && (
            <div className="table-error" role="alert">
              <span aria-hidden="true">!</span>
              <div>
                <strong>We couldn&apos;t complete that request.</strong>
                <p>{error}</p>
              </div>
              <button type="button" onClick={() => void loadHistory()}>
                Retry
              </button>
            </div>
          )}

          <div className="user-table-wrap">
            <table className="user-table review-history-table">
              <thead>
                <tr>
                  <th>
                    <button type="button" onClick={() => changeSort("questionId")}>
                      Question <span>{sortIndicator("questionId")}</span>
                    </button>
                  </th>
                  <th>Status</th>
                  <th>
                    <button type="button" onClick={() => changeSort("reviewerId")}>
                      Reviewer <span>{sortIndicator("reviewerId")}</span>
                    </button>
                  </th>
                  <th>Comment</th>
                  <th>
                    <button type="button" onClick={() => changeSort("reviewTime")}>
                      Reviewed <span>{sortIndicator("reviewTime")}</span>
                    </button>
                  </th>
                  <th><span className="sr-only">Actions</span></th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, index) => (
                    <tr className="skeleton-row" key={index}>
                      {Array.from({ length: 6 }).map((__, cell) => (
                        <td key={cell}><span /></td>
                      ))}
                    </tr>
                  ))
                ) : result.records.length === 0 ? (
                  <tr>
                    <td className="empty-state" colSpan={6}>
                      <span aria-hidden="true">↻</span>
                      <strong>No decisions found</strong>
                      <p>Adjust the filters to widen the audit window.</p>
                    </td>
                  </tr>
                ) : (
                  result.records.map((review) => (
                    <tr key={review.id}>
                      <td>
                        <div className="history-question">
                          <strong title={review.questionTitle}>
                            {review.questionTitle || "Untitled question"}
                          </strong>
                          <code>{review.questionId}</code>
                        </div>
                      </td>
                      <td>
                        <ReviewStatusBadge
                          status={review.reviewStatus}
                          description={review.reviewStatusDesc}
                        />
                      </td>
                      <td>
                        <div className="history-reviewer">
                          <strong>{review.reviewerName || "Unknown admin"}</strong>
                          <code>{review.reviewerId || "—"}</code>
                        </div>
                      </td>
                      <td>
                        <p className="review-comment" title={review.reviewMessage || undefined}>
                          {review.reviewMessage || "No comment"}
                        </p>
                      </td>
                      <td>{formatAdminDate(review.reviewTime)}</td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            onClick={() => void showLatestReview(review.questionId)}
                            disabled={detailLoading}
                          >
                            Latest record
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <AdminPagination
            page={page}
            pageSize={pageSize}
            total={result.total}
            loading={loading}
            onPageChange={setPage}
            onPageSizeChange={(size) => {
              setPageSize(size);
              setPage(1);
            }}
          />
        </section>
      </main>

      {selectedReview && (
        <div className="modal-layer" role="presentation">
          <button
            className="modal-backdrop"
            type="button"
            aria-label="Close dialog"
            onClick={() => setSelectedReview(null)}
          />
          <section
            className="modal-card review-detail-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="review-detail-title"
          >
            <header className="modal-header">
              <div>
                <p className="eyebrow">Latest decision</p>
                <h2 id="review-detail-title">{selectedReview.questionTitle}</h2>
              </div>
              <button
                type="button"
                className="modal-close"
                aria-label="Close dialog"
                onClick={() => setSelectedReview(null)}
              >
                ×
              </button>
            </header>
            <div className="review-detail-body">
              <ReviewStatusBadge
                status={selectedReview.reviewStatus}
                description={selectedReview.reviewStatusDesc}
              />
              <dl>
                <div>
                  <dt>Question ID</dt>
                  <dd>{selectedReview.questionId}</dd>
                </div>
                <div>
                  <dt>Reviewer</dt>
                  <dd>
                    {selectedReview.reviewerName || "Unknown admin"}
                    <small>{selectedReview.reviewerId}</small>
                  </dd>
                </div>
                <div>
                  <dt>Review time</dt>
                  <dd>{formatAdminDate(selectedReview.reviewTime)}</dd>
                </div>
                <div className="review-detail-message">
                  <dt>Comment</dt>
                  <dd>{selectedReview.reviewMessage || "No comment was provided."}</dd>
                </div>
              </dl>
            </div>
          </section>
        </div>
      )}
    </AdminShell>
  );
}
