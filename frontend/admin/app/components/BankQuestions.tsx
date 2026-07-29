"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiRequest } from "../lib/api";
import { AdminShell } from "./AdminShell";
import { useAuth } from "./AuthProvider";

type QuestionBankDetail = {
  id: string;
  title: string;
  picture?: string | null;
  description?: string | null;
};

type Question = {
  id: string;
  title: string;
  tags?: string[] | null;
  difficulty?: number | null;
  viewNum?: number | null;
  thumbNum?: number | null;
  favourNum?: number | null;
  needVip?: number | null;
};

type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
};

const difficultyLabels: Record<number, string> = {
  1: "Easy",
  2: "Medium",
  3: "Hard",
};

function isAuthorizationError(error: unknown) {
  return (
    error instanceof ApiError && (error.code === 401 || error.code === 403)
  );
}

function metric(value?: number | null) {
  return (value ?? 0).toLocaleString();
}

export function BankQuestions({ bankId }: { bankId: string }) {
  const router = useRouter();
  const { token, ready, user, clearSession } = useAuth();
  const [bank, setBank] = useState<QuestionBankDetail | null>(null);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [result, setResult] = useState<PageResult<Question>>({
    records: [],
    total: 0,
    size: 10,
    current: 1,
  });
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [removeBusy, setRemoveBusy] = useState(false);
  const [confirmRemove, setConfirmRemove] = useState(false);
  const [pickerOpen, setPickerOpen] = useState(false);
  const [pickerQuery, setPickerQuery] = useState("");
  const [pickerAppliedQuery, setPickerAppliedQuery] = useState("");
  const [pickerDifficulty, setPickerDifficulty] = useState("");
  const [pickerAppliedDifficulty, setPickerAppliedDifficulty] = useState("");
  const [pickerPage, setPickerPage] = useState(1);
  const [pickerPageSize] = useState(8);
  const [pickerResult, setPickerResult] = useState<PageResult<Question>>({
    records: [],
    total: 0,
    size: 8,
    current: 1,
  });
  const [pickerSelectedIds, setPickerSelectedIds] = useState<Set<string>>(
    new Set(),
  );
  const [pickerLoading, setPickerLoading] = useState(false);
  const [pickerError, setPickerError] = useState("");
  const [addBusy, setAddBusy] = useState(false);

  const totalPages = Math.max(1, Math.ceil(result.total / pageSize));
  const pickerTotalPages = Math.max(
    1,
    Math.ceil(pickerResult.total / pickerPageSize),
  );
  const visibleQuestionIds = useMemo(
    () => new Set(result.records.map((question) => question.id)),
    [result.records],
  );
  const selectablePickerRecords = useMemo(
    () =>
      pickerResult.records.filter(
        (question) => !visibleQuestionIds.has(question.id),
      ),
    [pickerResult.records, visibleQuestionIds],
  );

  const handleRequestError = useCallback(
    (requestError: unknown, fallback: string, target: "page" | "picker" = "page") => {
      if (isAuthorizationError(requestError)) {
        clearSession();
        router.replace("/login");
        return;
      }
      const message =
        requestError instanceof Error ? requestError.message : fallback;
      if (target === "picker") setPickerError(message);
      else setError(message);
    },
    [clearSession, router],
  );

  const loadBank = useCallback(async () => {
    try {
      const response = await apiRequest<QuestionBankDetail>(
        `/question-banks/${encodeURIComponent(bankId)}`,
        {},
        token,
      );
      setBank(response.data);
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load question bank details.");
    }
  }, [bankId, handleRequestError, token]);

  const loadQuestions = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError("");
    try {
      const query = new URLSearchParams({
        current: String(page),
        pageSize: String(pageSize),
      });
      const response = await apiRequest<PageResult<Question>>(
        `/question-bank-relations/banks/${encodeURIComponent(
          bankId,
        )}/questions?${query}`,
        {},
        token,
      );
      setResult(response.data);
      setSelectedIds(new Set());
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load bank questions.");
    } finally {
      setLoading(false);
    }
  }, [bankId, handleRequestError, page, pageSize, token, user]);

  useEffect(() => {
    if (!ready || !user) return;
    const timer = window.setTimeout(() => {
      void Promise.all([loadBank(), loadQuestions()]);
    }, 0);
    return () => window.clearTimeout(timer);
  }, [loadBank, loadQuestions, ready, user]);

  const loadPickerQuestions = useCallback(async () => {
    if (!pickerOpen) return;
    setPickerLoading(true);
    setPickerError("");
    try {
      const body = Object.fromEntries(
        Object.entries({
          current: pickerPage,
          pageSize: pickerPageSize,
          searchText: pickerAppliedQuery.trim() || undefined,
          difficulty: pickerAppliedDifficulty
            ? Number(pickerAppliedDifficulty)
            : undefined,
          sortField: "createTime",
          sortOrder: "descend",
        }).filter(([, value]) => value !== undefined),
      );
      const response = await apiRequest<PageResult<Question>>(
        "/questions/search",
        { method: "POST", body: JSON.stringify(body) },
        token,
      );
      setPickerResult(response.data);
    } catch (requestError) {
      handleRequestError(
        requestError,
        "Unable to load available questions.",
        "picker",
      );
    } finally {
      setPickerLoading(false);
    }
  }, [
    handleRequestError,
    pickerAppliedDifficulty,
    pickerAppliedQuery,
    pickerOpen,
    pickerPage,
    pickerPageSize,
    token,
  ]);

  useEffect(() => {
    if (!pickerOpen) return;
    const timer = window.setTimeout(() => void loadPickerQuestions(), 0);
    return () => window.clearTimeout(timer);
  }, [loadPickerQuestions, pickerOpen]);

  function toggleSelection(id: string, target: "table" | "picker") {
    const setter =
      target === "table" ? setSelectedIds : setPickerSelectedIds;
    setter((current) => {
      const next = new Set(current);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleCurrentPage() {
    const pageIds = result.records.map((question) => question.id);
    const allSelected =
      pageIds.length > 0 && pageIds.every((id) => selectedIds.has(id));
    setSelectedIds(allSelected ? new Set() : new Set(pageIds));
  }

  function openPicker() {
    setPickerQuery("");
    setPickerAppliedQuery("");
    setPickerDifficulty("");
    setPickerAppliedDifficulty("");
    setPickerPage(1);
    setPickerSelectedIds(new Set());
    setPickerError("");
    setPickerOpen(true);
  }

  function applyPickerFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPickerPage(1);
    setPickerAppliedQuery(pickerQuery);
    setPickerAppliedDifficulty(pickerDifficulty);
  }

  async function removeQuestions() {
    if (!selectedIds.size) return;
    setRemoveBusy(true);
    setError("");
    try {
      await apiRequest<boolean>(
        `/question-bank-relations/banks/${encodeURIComponent(
          bankId,
        )}/questions/batch`,
        {
          method: "DELETE",
          body: JSON.stringify([...selectedIds]),
        },
        token,
      );
      setNotice(
        `Removed ${selectedIds.size} ${
          selectedIds.size === 1 ? "question" : "questions"
        } from this bank.`,
      );
      setConfirmRemove(false);
      if (result.records.length === selectedIds.size && page > 1) {
        setPage((current) => current - 1);
      } else {
        await loadQuestions();
      }
      await loadBank();
    } catch (requestError) {
      handleRequestError(requestError, "Unable to remove the selected questions.");
    } finally {
      setRemoveBusy(false);
    }
  }

  async function addQuestions() {
    if (!pickerSelectedIds.size) return;
    setAddBusy(true);
    setPickerError("");
    try {
      await apiRequest<boolean>(
        `/question-bank-relations/banks/${encodeURIComponent(
          bankId,
        )}/questions/batch`,
        {
          method: "POST",
          body: JSON.stringify([...pickerSelectedIds]),
        },
        token,
      );
      setNotice(
        `Added ${pickerSelectedIds.size} ${
          pickerSelectedIds.size === 1 ? "question" : "questions"
        } to this bank.`,
      );
      setPickerOpen(false);
      setPickerSelectedIds(new Set());
      if (page === 1) {
        await Promise.all([loadBank(), loadQuestions()]);
      } else {
        setPage(1);
        await loadBank();
      }
    } catch (requestError) {
      handleRequestError(
        requestError,
        "Unable to add the selected questions.",
        "picker",
      );
    } finally {
      setAddBusy(false);
    }
  }

  return (
    <AdminShell>
      <main className="users-page bank-questions-page">
        <div className="breadcrumb-row">
          <Link href="/question-banks">Question banks</Link>
          <span aria-hidden="true">/</span>
          <span>Questions</span>
        </div>

        <section className="bank-context">
          <div className="bank-context__cover">
            {bank?.picture ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={bank.picture} alt="" />
            ) : (
              <span aria-hidden="true">梅</span>
            )}
          </div>
          <div className="bank-context__copy">
            <p className="eyebrow">Collection contents</p>
            <h1>{bank?.title || "Question bank"}</h1>
            <p>
              {bank?.description ||
                "Choose approved questions to build this learning collection."}
            </p>
          </div>
          <div className="bank-context__actions">
            <span className="bank-count">
              <strong>{result.total.toLocaleString()}</strong>
              <small>{result.total === 1 ? "question" : "questions"}</small>
            </span>
            <button type="button" className="primary-action" onClick={openPicker}>
              <span aria-hidden="true">＋</span>
              Add questions
            </button>
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

        <section className="table-card bank-question-card" aria-labelledby="bank-question-title">
          <div className="table-card__heading">
            <div>
              <h2 id="bank-question-title">Questions in this bank</h2>
              <p>
                {selectedIds.size
                  ? `${selectedIds.size} selected`
                  : "Only approved questions appear in learner-facing collections."}
              </p>
            </div>
            <div className="table-toolbar">
              {selectedIds.size > 0 && (
                <button
                  type="button"
                  className="danger-outline-button"
                  onClick={() => setConfirmRemove(true)}
                >
                  Remove selected
                </button>
              )}
              <button
                className="refresh-button"
                type="button"
                onClick={() => void loadQuestions()}
                disabled={loading}
              >
                <span className={loading ? "spin" : ""} aria-hidden="true">
                  ↻
                </span>
                Refresh
              </button>
            </div>
          </div>

          {error && (
            <div className="table-error" role="alert">
              <span aria-hidden="true">!</span>
              <div>
                <strong>We couldn&apos;t complete that request.</strong>
                <p>{error}</p>
              </div>
              <button type="button" onClick={() => void loadQuestions()}>
                Retry
              </button>
            </div>
          )}

          <div className="user-table-wrap">
            <table className="user-table question-table">
              <thead>
                <tr>
                  <th>
                    <input
                      type="checkbox"
                      aria-label="Select all questions on this page"
                      checked={
                        result.records.length > 0 &&
                        result.records.every((question) =>
                          selectedIds.has(question.id),
                        )
                      }
                      onChange={toggleCurrentPage}
                    />
                  </th>
                  <th>Question</th>
                  <th>Tags</th>
                  <th>Difficulty</th>
                  <th>Access</th>
                  <th>Engagement</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, index) => (
                    <tr className="skeleton-row" key={index}>
                      {Array.from({ length: 6 }).map((__, cell) => (
                        <td key={cell}>
                          <span />
                        </td>
                      ))}
                    </tr>
                  ))
                ) : result.records.length === 0 ? (
                  <tr>
                    <td className="empty-state" colSpan={6}>
                      <span aria-hidden="true">?</span>
                      <strong>This bank is empty</strong>
                      <p>Add approved questions to start the collection.</p>
                    </td>
                  </tr>
                ) : (
                  result.records.map((question) => (
                    <tr
                      className={selectedIds.has(question.id) ? "row-selected" : ""}
                      key={question.id}
                    >
                      <td>
                        <input
                          type="checkbox"
                          aria-label={`Select ${question.title}`}
                          checked={selectedIds.has(question.id)}
                          onChange={() => toggleSelection(question.id, "table")}
                        />
                      </td>
                      <td>
                        <div className="question-title-cell">
                          <strong>{question.title}</strong>
                          <code>{question.id}</code>
                        </div>
                      </td>
                      <td>
                        <div className="tag-list">
                          {(question.tags ?? []).length ? (
                            question.tags?.slice(0, 3).map((tag) => (
                              <span key={tag}>{tag}</span>
                            ))
                          ) : (
                            <span className="tag-muted">No tags</span>
                          )}
                        </div>
                      </td>
                      <td>
                        <span
                          className={`difficulty-badge difficulty-badge--${
                            question.difficulty ?? 0
                          }`}
                        >
                          {difficultyLabels[question.difficulty ?? 0] || "Unrated"}
                        </span>
                      </td>
                      <td>
                        <span className={question.needVip ? "vip-badge" : "access-badge"}>
                          {question.needVip ? "VIP" : "Open"}
                        </span>
                      </td>
                      <td>
                        <div className="metric-list">
                          <span title="Views">◉ {metric(question.viewNum)}</span>
                          <span title="Likes">♡ {metric(question.thumbNum)}</span>
                          <span title="Favourites">☆ {metric(question.favourNum)}</span>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <div className="pagination">
            <label>
              Rows per page
              <select
                value={pageSize}
                onChange={(event) => {
                  setPageSize(Number(event.target.value));
                  setPage(1);
                }}
              >
                {[10, 20, 50].map((size) => (
                  <option value={size} key={size}>
                    {size}
                  </option>
                ))}
              </select>
            </label>
            <p>
              Page {Math.min(page, totalPages)} of {totalPages}
            </p>
            <div>
              <button
                type="button"
                aria-label="Previous page"
                disabled={page <= 1 || loading}
                onClick={() => setPage((current) => current - 1)}
              >
                ←
              </button>
              <button
                type="button"
                aria-label="Next page"
                disabled={page >= totalPages || loading}
                onClick={() => setPage((current) => current + 1)}
              >
                →
              </button>
            </div>
          </div>
        </section>
      </main>

      {pickerOpen && (
        <div className="modal-layer" role="presentation">
          <button
            className="modal-backdrop"
            type="button"
            aria-label="Close question picker"
            onClick={() => setPickerOpen(false)}
          />
          <section
            className="modal-card picker-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="picker-title"
          >
            <header className="modal-header">
              <div>
                <p className="eyebrow">Curate collection</p>
                <h2 id="picker-title">Add approved questions</h2>
              </div>
              <button
                type="button"
                className="modal-close"
                aria-label="Close question picker"
                onClick={() => setPickerOpen(false)}
              >
                ×
              </button>
            </header>

            <form className="picker-filters" onSubmit={applyPickerFilters}>
              <label>
                <span className="sr-only">Search questions</span>
                <input
                  value={pickerQuery}
                  onChange={(event) => setPickerQuery(event.target.value)}
                  placeholder="Search question title or content"
                />
              </label>
              <label>
                <span className="sr-only">Difficulty</span>
                <select
                  value={pickerDifficulty}
                  onChange={(event) => setPickerDifficulty(event.target.value)}
                >
                  <option value="">All difficulties</option>
                  <option value="1">Easy</option>
                  <option value="2">Medium</option>
                  <option value="3">Hard</option>
                </select>
              </label>
              <button type="submit" className="filter-submit">
                Search
              </button>
            </form>

            {pickerError && (
              <div className="table-error picker-error" role="alert">
                <span aria-hidden="true">!</span>
                <div>
                  <strong>Questions could not be loaded.</strong>
                  <p>{pickerError}</p>
                </div>
                <button type="button" onClick={() => void loadPickerQuestions()}>
                  Retry
                </button>
              </div>
            )}

            <div className="picker-list">
              {pickerLoading ? (
                <div className="picker-loading">
                  <span className="button-spinner" />
                  Loading available questions…
                </div>
              ) : selectablePickerRecords.length === 0 ? (
                <div className="picker-empty">
                  <span aria-hidden="true">?</span>
                  <strong>No available questions found</strong>
                  <p>Try a broader search or move to another page.</p>
                </div>
              ) : (
                selectablePickerRecords.map((question) => (
                  <label className="picker-row" key={question.id}>
                    <input
                      type="checkbox"
                      checked={pickerSelectedIds.has(question.id)}
                      onChange={() => toggleSelection(question.id, "picker")}
                    />
                    <span className="picker-row__copy">
                      <strong>{question.title}</strong>
                      <small>
                        {difficultyLabels[question.difficulty ?? 0] || "Unrated"}
                        {" · "}
                        {(question.tags ?? []).slice(0, 3).join(", ") || "No tags"}
                      </small>
                    </span>
                    {question.needVip ? (
                      <span className="vip-badge">VIP</span>
                    ) : (
                      <span className="access-badge">Open</span>
                    )}
                  </label>
                ))
              )}
            </div>

            <footer className="picker-footer">
              <div className="picker-pagination">
                <button
                  type="button"
                  aria-label="Previous available question page"
                  disabled={pickerPage <= 1 || pickerLoading}
                  onClick={() => setPickerPage((current) => current - 1)}
                >
                  ←
                </button>
                <span>
                  Page {Math.min(pickerPage, pickerTotalPages)} of {pickerTotalPages}
                </span>
                <button
                  type="button"
                  aria-label="Next available question page"
                  disabled={pickerPage >= pickerTotalPages || pickerLoading}
                  onClick={() => setPickerPage((current) => current + 1)}
                >
                  →
                </button>
              </div>
              <div>
                <span className="selection-count">
                  {pickerSelectedIds.size} selected
                </span>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setPickerOpen(false)}
                  disabled={addBusy}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="primary-action"
                  onClick={() => void addQuestions()}
                  disabled={addBusy || !pickerSelectedIds.size}
                >
                  {addBusy ? "Adding…" : "Add to bank"}
                </button>
              </div>
            </footer>
          </section>
        </div>
      )}

      {confirmRemove && (
        <div className="modal-layer" role="presentation">
          <button
            className="modal-backdrop"
            type="button"
            aria-label="Cancel removal"
            onClick={() => setConfirmRemove(false)}
          />
          <section
            className="confirm-card"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="remove-question-title"
          >
            <span className="confirm-icon" aria-hidden="true">
              !
            </span>
            <h2 id="remove-question-title">
              Remove {selectedIds.size}{" "}
              {selectedIds.size === 1 ? "question" : "questions"}?
            </h2>
            <p>
              The questions stay in the platform library; only their relationship
              with this bank will be removed.
            </p>
            <div>
              <button
                type="button"
                className="secondary-button"
                onClick={() => setConfirmRemove(false)}
                disabled={removeBusy}
              >
                Cancel
              </button>
              <button
                type="button"
                className="danger-button"
                onClick={() => void removeQuestions()}
                disabled={removeBusy}
              >
                {removeBusy ? "Removing…" : "Remove questions"}
              </button>
            </div>
          </section>
        </div>
      )}
    </AdminShell>
  );
}
