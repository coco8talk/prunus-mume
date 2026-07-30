"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiRequest } from "../lib/api";
import {
  formatAdminDate,
  type PageResult,
  type PendingQuestionReview,
  questionDifficultyClass,
  questionDifficultyLabel,
} from "../lib/reviews";
import { AdminPagination } from "./AdminPagination";
import { AdminShell } from "./AdminShell";
import { useAuth } from "./AuthProvider";
import { ReviewStatusBadge } from "./ReviewStatusBadge";

function isAuthorizationError(error: unknown) {
  return error instanceof ApiError && (error.code === 401 || error.code === 403);
}

type ReviewDecisionPanelProps = {
  question: PendingQuestionReview;
  busy: boolean;
  onSubmit: (
    question: PendingQuestionReview,
    status: 1 | 2,
    message: string,
  ) => Promise<void>;
};

function ReviewDecisionPanel({
  question,
  busy,
  onSubmit,
}: ReviewDecisionPanelProps) {
  const [message, setMessage] = useState("");
  const [decision, setDecision] = useState<1 | 2 | null>(null);

  async function submit(status: 1 | 2) {
    if (busy) return;
    setDecision(status);
    await onSubmit(question, status, message.trim());
  }

  return (
    <div className="review-decision">
      <label>
        <span>Reviewer note <small>Optional</small></span>
        <textarea
          value={message}
          maxLength={500}
          onChange={(event) => setMessage(event.target.value)}
          placeholder="Add context for the author or audit trail"
          disabled={busy}
        />
        <small>{message.length}/500</small>
      </label>
      <div className="review-decision__actions">
        <button
          type="button"
          className="approve-button"
          disabled={busy}
          onClick={() => void submit(1)}
        >
          <span aria-hidden="true">✓</span>
          {busy && decision === 1 ? "Approving…" : "Approve"}
        </button>
        <button
          type="button"
          className="reject-button"
          disabled={busy}
          onClick={() => void submit(2)}
        >
          <span aria-hidden="true">×</span>
          {busy && decision === 2 ? "Rejecting…" : "Reject"}
        </button>
      </div>
    </div>
  );
}

export function ReviewQueue() {
  const router = useRouter();
  const { token, ready, user, clearSession } = useAuth();
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [pendingCount, setPendingCount] = useState(0);
  const [result, setResult] = useState<PageResult<PendingQuestionReview>>({
    records: [],
    total: 0,
    size: 10,
    current: 1,
  });
  const [loading, setLoading] = useState(true);
  const [busyQuestionId, setBusyQuestionId] = useState<string | null>(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

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

  const loadQueue = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError("");
    try {
      const [countResponse, queueResponse] = await Promise.all([
        apiRequest<number>(
          "/question-reviews/pending/count",
          { method: "GET" },
          token,
        ),
        apiRequest<PageResult<PendingQuestionReview>>(
          `/question-reviews/pending?current=${page}&pageSize=${pageSize}`,
          { method: "GET" },
          token,
        ),
      ]);
      setPendingCount(countResponse.data);
      setResult(queueResponse.data);
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load the review queue.");
    } finally {
      setLoading(false);
    }
  }, [handleRequestError, page, pageSize, token, user]);

  useEffect(() => {
    if (!ready || !user) return;
    const timer = window.setTimeout(() => void loadQueue(), 0);
    return () => window.clearTimeout(timer);
  }, [loadQueue, ready, user]);

  async function submitDecision(
    question: PendingQuestionReview,
    reviewStatus: 1 | 2,
    reviewMessage: string,
  ) {
    setBusyQuestionId(question.questionId);
    setError("");
    try {
      await apiRequest<boolean>(
        "/question-reviews",
        {
          method: "POST",
          body: JSON.stringify({
            questionId: question.questionId,
            reviewStatus,
            reviewMessage: reviewMessage || undefined,
          }),
        },
        token,
      );
      const action = reviewStatus === 1 ? "Approved" : "Rejected";
      setNotice(`${action} “${question.questionTitle}”.`);
      if (result.records.length === 1 && page > 1) {
        setPage((current) => current - 1);
      } else {
        await loadQueue();
      }
    } catch (requestError) {
      handleRequestError(requestError, "Unable to submit this decision.");
    } finally {
      setBusyQuestionId(null);
    }
  }

  return (
    <AdminShell>
      <main className="users-page review-queue-page">
        <section className="users-heading review-page-heading">
          <div>
            <p className="eyebrow">Editorial workflow</p>
            <h1>Review queue</h1>
            <p>Inspect the complete submission and decide without leaving the queue.</p>
          </div>
          <div className="pending-count-badge" aria-label={`${pendingCount} pending reviews`}>
            <span>{loading ? "…" : pendingCount.toLocaleString()}</span>
            <small>Awaiting review</small>
          </div>
        </section>

        {notice && (
          <div className="notice-banner" role="status">
            <span aria-hidden="true">✓</span>
            {notice}
            <button type="button" aria-label="Dismiss" onClick={() => setNotice("")}>
              ×
            </button>
          </div>
        )}

        {error && (
          <div className="table-error review-page-error" role="alert">
            <span aria-hidden="true">!</span>
            <div>
              <strong>We couldn&apos;t complete that request.</strong>
              <p>{error}</p>
            </div>
            <button type="button" onClick={() => void loadQueue()}>
              Retry
            </button>
          </div>
        )}

        <section className="review-queue-toolbar" aria-label="Review queue controls">
          <p>
            {loading
              ? "Loading submissions…"
              : `${result.total.toLocaleString()} ${result.total === 1 ? "submission" : "submissions"} on the queue`}
          </p>
          <button
            className="refresh-button"
            type="button"
            onClick={() => void loadQueue()}
            disabled={loading}
          >
            <span className={loading ? "spin" : ""} aria-hidden="true">↻</span>
            Refresh
          </button>
        </section>

        <section className="review-queue-list" aria-live="polite">
          {loading ? (
            Array.from({ length: 3 }).map((_, index) => (
              <div className="review-card review-card--loading" key={index}>
                <span />
                <span />
                <span />
              </div>
            ))
          ) : result.records.length === 0 ? (
            <div className="queue-empty">
              <span aria-hidden="true">✓</span>
              <h2>Queue cleared</h2>
              <p>There are no questions waiting for an administrator decision.</p>
            </div>
          ) : (
            result.records.map((question) => (
              <article className="review-card" key={question.id}>
                <header className="review-card__header">
                  <div>
                    <div className="review-card__meta">
                      <ReviewStatusBadge
                        status={question.reviewStatus}
                        description={question.reviewStatusDesc}
                      />
                      <span>
                        Submitted {formatAdminDate(question.questionCreateTime)}
                      </span>
                    </div>
                    <h2>{question.questionTitle}</h2>
                    <code>Question {question.questionId}</code>
                  </div>
                  <span
                    className={`difficulty-badge difficulty-badge--${questionDifficultyClass(
                      question.questionDifficulty,
                    )}`}
                  >
                    {questionDifficultyLabel(question.questionDifficulty)}
                  </span>
                </header>

                <div className="review-card__tags">
                  {(question.questionTags ?? []).map((tag) => (
                    <span key={tag}>{tag}</span>
                  ))}
                  {(question.questionTags ?? []).length === 0 && (
                    <span className="tag-muted">No tags</span>
                  )}
                </div>

                <div className="review-content-grid">
                  <section>
                    <p>Question</p>
                    <div className="review-prose">{question.questionContent}</div>
                  </section>
                  <section className="review-answer">
                    <p>Expected answer</p>
                    <div className="review-prose">{question.questionAnswer}</div>
                  </section>
                </div>

                <ReviewDecisionPanel
                  question={question}
                  busy={busyQuestionId === question.questionId}
                  onSubmit={submitDecision}
                />
              </article>
            ))
          )}
        </section>

        {!loading && result.records.length > 0 && (
          <section className="table-card review-pagination-card">
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
        )}
      </main>
    </AdminShell>
  );
}
