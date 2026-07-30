"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiRequest } from "../lib/api";
import { AdminShell } from "./AdminShell";
import { useAuth } from "./AuthProvider";
import { ReviewStatusBadge } from "./ReviewStatusBadge";

type SortOrder = "ascend" | "descend" | undefined;

type Creator = {
  id?: string;
  userName?: string | null;
  userAccount?: string | null;
  userAvatar?: string | null;
};

type Review = {
  id?: string;
  questionId?: string;
  reviewerId?: string;
  reviewerName?: string | null;
  reviewStatus: number;
  reviewStatusDesc?: string | null;
  reviewMessage?: string | null;
  reviewTime?: string | null;
};

type Question = {
  id: string;
  title: string;
  content: string;
  answer: string;
  tags?: string[] | null;
  difficulty?: number | null;
  questionBankId?: string | null;
  createUserId?: string | null;
  viewNum?: number | null;
  thumbNum?: number | null;
  favourNum?: number | null;
  needVip?: number | null;
  createUser?: Creator | null;
  reviewVo?: Review | null;
};

type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
};

type Filters = {
  searchText: string;
  title: string;
  questionBankId: string;
  createUserId: string;
  tags: string;
  difficulty: string;
};

type QuestionForm = {
  title: string;
  content: string;
  answer: string;
  tags: string;
  difficulty: string;
  questionBankId: string;
};

const emptyFilters: Filters = {
  searchText: "",
  title: "",
  questionBankId: "",
  createUserId: "",
  tags: "",
  difficulty: "",
};

const emptyForm: QuestionForm = {
  title: "",
  content: "",
  answer: "",
  tags: "",
  difficulty: "",
  questionBankId: "",
};

function compactPayload(value: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(value).filter(
      ([, field]) =>
        field !== "" &&
        field !== null &&
        field !== undefined &&
        (!Array.isArray(field) || field.length > 0),
    ),
  );
}

function parseTags(value: string) {
  return Array.from(
    new Set(
      value
        .split(",")
        .map((tag) => tag.trim())
        .filter(Boolean),
    ),
  );
}

function creatorName(question: Question) {
  return (
    question.createUser?.userName ||
    question.createUser?.userAccount ||
    question.createUserId ||
    "Unknown"
  );
}

function creatorInitials(question: Question) {
  return creatorName(question).slice(0, 2).toUpperCase();
}

function difficultyLabel(value?: number | null) {
  if (value === 0) return "Easy";
  if (value === 1) return "Medium";
  if (value === 2 || value === 3) return "Hard";
  return "Unset";
}

function difficultyClass(value?: number | null) {
  if (value === 0) return 1;
  if (value === 1) return 2;
  if (value === 2 || value === 3) return 3;
  return 0;
}

function isAuthorizationError(error: unknown) {
  return error instanceof ApiError && (error.code === 401 || error.code === 403);
}

export function QuestionManagement() {
  const router = useRouter();
  const { token, ready, user, clearSession } = useAuth();
  const [filters, setFilters] = useState<Filters>(emptyFilters);
  const [appliedFilters, setAppliedFilters] = useState<Filters>(emptyFilters);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState<string>();
  const [sortOrder, setSortOrder] = useState<SortOrder>();
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
  const [modalMode, setModalMode] = useState<"create" | "edit" | null>(null);
  const [selectedQuestion, setSelectedQuestion] = useState<Question | null>(null);
  const [form, setForm] = useState<QuestionForm>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [modalBusy, setModalBusy] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<Question | "batch" | null>(null);

  const totalPages = Math.max(1, Math.ceil(result.total / pageSize));
  const allVisibleSelected =
    result.records.length > 0 &&
    result.records.every((question) => selectedIds.has(question.id));

  const requestBody = useMemo(
    () =>
      compactPayload({
        current: page,
        pageSize,
        sortField,
        sortOrder,
        questionBankId: appliedFilters.questionBankId.trim(),
        title: appliedFilters.title.trim(),
        createUserId: appliedFilters.createUserId.trim(),
        tags: parseTags(appliedFilters.tags),
        difficulty:
          appliedFilters.difficulty === ""
            ? undefined
            : Number(appliedFilters.difficulty),
        searchText: appliedFilters.searchText.trim(),
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

  const loadQuestions = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError("");
    try {
      const response = await apiRequest<PageResult<Question>>(
        "/questions/search",
        { method: "POST", body: JSON.stringify(requestBody) },
        token,
      );
      setResult(response.data);
      setSelectedIds(new Set());
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load questions.");
    } finally {
      setLoading(false);
    }
  }, [handleRequestError, requestBody, token, user]);

  useEffect(() => {
    if (!ready || !user) return;
    const timer = window.setTimeout(() => void loadQuestions(), 0);
    return () => window.clearTimeout(timer);
  }, [loadQuestions, ready, user]);

  function validateNumericFilters() {
    for (const [label, value] of [
      ["Question bank ID", filters.questionBankId],
      ["Creator ID", filters.createUserId],
    ]) {
      if (value && !/^\d+$/.test(value)) {
        setError(`${label} must contain digits only.`);
        return false;
      }
    }
    return true;
  }

  function applyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!validateNumericFilters()) return;
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

  function toggleAllVisible() {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (allVisibleSelected) {
        result.records.forEach((question) => next.delete(question.id));
      } else {
        result.records.forEach((question) => next.add(question.id));
      }
      return next;
    });
  }

  function toggleQuestion(id: string) {
    setSelectedIds((current) => {
      const next = new Set(current);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function openCreate() {
    setSelectedQuestion(null);
    setForm(emptyForm);
    setFormErrors({});
    setModalMode("create");
  }

  function openEdit(question: Question) {
    setSelectedQuestion(question);
    setForm({
      title: question.title,
      content: question.content,
      answer: question.answer,
      tags: (question.tags ?? []).join(", "),
      difficulty:
        question.difficulty === null || question.difficulty === undefined
          ? ""
          : String(Math.min(question.difficulty, 2)),
      questionBankId: question.questionBankId ?? "",
    });
    setFormErrors({});
    setModalMode("edit");
  }

  function closeModal() {
    if (modalBusy) return;
    setModalMode(null);
    setSelectedQuestion(null);
    setFormErrors({});
  }

  function validateForm() {
    const errors: Record<string, string> = {};
    const tags = parseTags(form.tags);
    if (!form.title.trim()) errors.title = "Title is required.";
    else if (form.title.trim().length > 200) {
      errors.title = "Title must be 200 characters or fewer.";
    }
    if (!form.content.trim()) errors.content = "Content is required.";
    else if (form.content.trim().length > 500) {
      errors.content = "Content must be 500 characters or fewer.";
    }
    if (!form.answer.trim()) errors.answer = "Answer is required.";
    else if (form.answer.trim().length > 20000) {
      errors.answer = "Answer must be 20,000 characters or fewer.";
    }
    if (tags.length > 10) errors.tags = "Use no more than 10 tags.";
    if (form.questionBankId && !/^\d+$/.test(form.questionBankId)) {
      errors.questionBankId = "Question bank ID must contain digits only.";
    }
    return errors;
  }

  async function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const errors = validateForm();
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setModalBusy(true);
    setError("");
    try {
      const payload = compactPayload({
        title: form.title.trim(),
        content: form.content.trim(),
        answer: form.answer.trim(),
        tags: parseTags(form.tags),
        difficulty: form.difficulty === "" ? undefined : Number(form.difficulty),
        questionBankId:
          modalMode === "create" ? form.questionBankId.trim() : undefined,
      });
      if (modalMode === "create") {
        await apiRequest<string>(
          "/questions",
          { method: "POST", body: JSON.stringify(payload) },
          token,
        );
        setNotice(`Created and approved “${form.title.trim()}”.`);
      } else if (selectedQuestion) {
        await apiRequest<boolean>(
          `/questions/${encodeURIComponent(selectedQuestion.id)}`,
          {
            method: "PUT",
            body: JSON.stringify(payload),
          },
          token,
        );
        setNotice(`Updated “${form.title.trim()}”.`);
      }
      setModalBusy(false);
      closeModal();
      await loadQuestions();
    } catch (requestError) {
      handleRequestError(requestError, "Unable to save the question.");
    } finally {
      setModalBusy(false);
    }
  }

  async function deleteQuestions() {
    if (!deleteTarget) return;
    const ids =
      deleteTarget === "batch" ? Array.from(selectedIds) : [deleteTarget.id];
    setModalBusy(true);
    setError("");
    try {
      if (deleteTarget === "batch") {
        await apiRequest<boolean>(
          "/questions/batch",
          {
            method: "DELETE",
            body: JSON.stringify(ids),
          },
          token,
        );
        setNotice(`Deleted ${ids.length} ${ids.length === 1 ? "question" : "questions"}.`);
      } else {
        await apiRequest<boolean>(
          `/questions/${encodeURIComponent(deleteTarget.id)}`,
          {
            method: "DELETE",
          },
          token,
        );
        setNotice(`Deleted “${deleteTarget.title}”.`);
      }
      setDeleteTarget(null);
      setSelectedIds(new Set());
      if (ids.length >= result.records.length && page > 1) {
        setPage((current) => current - 1);
      } else {
        await loadQuestions();
      }
    } catch (requestError) {
      handleRequestError(requestError, "Unable to delete the selected questions.");
    } finally {
      setModalBusy(false);
    }
  }

  return (
    <AdminShell>
      <main className="users-page questions-page">
        <section className="users-heading">
          <div>
            <p className="eyebrow">Question library</p>
            <h1>Questions</h1>
            <p>Search every question, monitor review state, and manage content.</p>
          </div>
          <button type="button" className="primary-action" onClick={openCreate}>
            <span aria-hidden="true">＋</span>
            New question
          </button>
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

        <section className="filter-card" aria-labelledby="question-filter-title">
          <div className="filter-card__heading">
            <div>
              <h2 id="question-filter-title">Find questions</h2>
              <p>Use broad keywords or combine structured filters.</p>
            </div>
            <button type="button" className="text-button" onClick={clearFilters}>
              Clear filters
            </button>
          </div>
          <form className="filter-grid filter-grid--questions" onSubmit={applyFilters}>
            <label className="question-search-field">
              <span>Keyword</span>
              <input
                value={filters.searchText}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    searchText: event.target.value,
                  }))
                }
                placeholder="Search title, content, or answer"
              />
            </label>
            <label>
              <span>Title</span>
              <input
                value={filters.title}
                onChange={(event) =>
                  setFilters((current) => ({ ...current, title: event.target.value }))
                }
                placeholder="Question title"
              />
            </label>
            <label>
              <span>Question bank ID</span>
              <input
                inputMode="numeric"
                value={filters.questionBankId}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    questionBankId: event.target.value,
                  }))
                }
                placeholder="Bank ID"
              />
            </label>
            <label>
              <span>Creator ID</span>
              <input
                inputMode="numeric"
                value={filters.createUserId}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    createUserId: event.target.value,
                  }))
                }
                placeholder="User ID"
              />
            </label>
            <label>
              <span>Tags</span>
              <input
                value={filters.tags}
                onChange={(event) =>
                  setFilters((current) => ({ ...current, tags: event.target.value }))
                }
                placeholder="java, concurrency"
              />
            </label>
            <label>
              <span>Difficulty</span>
              <select
                value={filters.difficulty}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    difficulty: event.target.value,
                  }))
                }
              >
                <option value="">All levels</option>
                <option value="0">Easy</option>
                <option value="1">Medium</option>
                <option value="2">Hard</option>
              </select>
            </label>
            <button className="filter-submit" type="submit">
              Search
            </button>
          </form>
        </section>

        <section className="table-card" aria-labelledby="question-directory-title">
          <div className="table-card__heading">
            <div>
              <h2 id="question-directory-title">Question directory</h2>
              <p>
                {loading
                  ? "Loading questions…"
                  : `${result.total.toLocaleString()} matching ${
                      result.total === 1 ? "question" : "questions"
                    }`}
              </p>
            </div>
            <div className="table-toolbar">
              {selectedIds.size > 0 && (
                <>
                  <span className="selection-count">
                    {selectedIds.size} selected
                  </span>
                  <button
                    className="danger-outline-button"
                    type="button"
                    onClick={() => setDeleteTarget("batch")}
                  >
                    Delete selected
                  </button>
                </>
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
            <table className="user-table admin-question-table">
              <thead>
                <tr>
                  <th>
                    <input
                      type="checkbox"
                      checked={allVisibleSelected}
                      onChange={toggleAllVisible}
                      aria-label="Select all questions on this page"
                    />
                  </th>
                  <th>
                    <button type="button" onClick={() => changeSort("title")}>
                      Question <span>{sortIndicator("title")}</span>
                    </button>
                  </th>
                  <th>Review</th>
                  <th>
                    <button type="button" onClick={() => changeSort("difficulty")}>
                      Difficulty <span>{sortIndicator("difficulty")}</span>
                    </button>
                  </th>
                  <th>Creator</th>
                  <th>Tags</th>
                  <th>
                    <button type="button" onClick={() => changeSort("viewNum")}>
                      Engagement <span>{sortIndicator("viewNum")}</span>
                    </button>
                  </th>
                  <th>
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, index) => (
                    <tr className="skeleton-row" key={index}>
                      {Array.from({ length: 8 }).map((__, cell) => (
                        <td key={cell}>
                          <span />
                        </td>
                      ))}
                    </tr>
                  ))
                ) : result.records.length === 0 ? (
                  <tr>
                    <td className="empty-state" colSpan={8}>
                      <span aria-hidden="true">?</span>
                      <strong>No questions found</strong>
                      <p>Adjust the filters or create the first question.</p>
                    </td>
                  </tr>
                ) : (
                  result.records.map((question) => (
                    <tr
                      key={question.id}
                      className={selectedIds.has(question.id) ? "row-selected" : ""}
                    >
                      <td>
                        <input
                          type="checkbox"
                          checked={selectedIds.has(question.id)}
                          onChange={() => toggleQuestion(question.id)}
                          aria-label={`Select ${question.title}`}
                        />
                      </td>
                      <td>
                        <div className="question-title-cell">
                          <strong title={question.title}>{question.title}</strong>
                          <code>{question.id}</code>
                          <small title={question.content}>{question.content}</small>
                        </div>
                      </td>
                      <td>
                        <span title={question.reviewVo?.reviewMessage || undefined}>
                          <ReviewStatusBadge
                            status={question.reviewVo?.reviewStatus ?? 1}
                            description={question.reviewVo?.reviewStatusDesc}
                          />
                        </span>
                      </td>
                      <td>
                        <span
                          className={`difficulty-badge difficulty-badge--${difficultyClass(
                            question.difficulty,
                          )}`}
                        >
                          {difficultyLabel(question.difficulty)}
                        </span>
                      </td>
                      <td>
                        <div className="creator-cell">
                          {question.createUser?.userAvatar ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={question.createUser.userAvatar} alt="" />
                          ) : (
                            <span aria-hidden="true">{creatorInitials(question)}</span>
                          )}
                          <div>
                            <strong>{creatorName(question)}</strong>
                            <code>{question.createUser?.id || question.createUserId || "—"}</code>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="tag-list">
                          {(question.tags ?? []).slice(0, 2).map((tag) => (
                            <span key={tag}>{tag}</span>
                          ))}
                          {(question.tags ?? []).length > 2 && (
                            <span className="tag-muted">
                              +{(question.tags ?? []).length - 2}
                            </span>
                          )}
                          {(question.tags ?? []).length === 0 && (
                            <span className="tag-muted">No tags</span>
                          )}
                        </div>
                      </td>
                      <td>
                        <div className="metric-list">
                          <span title="Views">◉ {(question.viewNum ?? 0).toLocaleString()}</span>
                          <span title="Likes">♡ {(question.thumbNum ?? 0).toLocaleString()}</span>
                          <span title="Favourites">☆ {(question.favourNum ?? 0).toLocaleString()}</span>
                        </div>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button type="button" onClick={() => openEdit(question)}>
                            Edit
                          </button>
                          <button
                            type="button"
                            className="danger-link"
                            onClick={() => setDeleteTarget(question)}
                          >
                            Delete
                          </button>
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

      {modalMode && (
        <div className="modal-layer" role="presentation">
          <button
            className="modal-backdrop"
            type="button"
            aria-label="Close dialog"
            onClick={closeModal}
          />
          <section
            className="modal-card question-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="question-modal-title"
          >
            <header className="modal-header">
              <div>
                <p className="eyebrow">
                  {modalMode === "create" ? "Admin-authored" : "Question settings"}
                </p>
                <h2 id="question-modal-title">
                  {modalMode === "create" ? "Create question" : "Edit question"}
                </h2>
              </div>
              <button
                type="button"
                className="modal-close"
                aria-label="Close dialog"
                onClick={closeModal}
              >
                ×
              </button>
            </header>
            <form className="user-form question-form" onSubmit={submitQuestion} noValidate>
              {modalMode === "create" && (
                <div className="approval-note form-span">
                  <span aria-hidden="true">✓</span>
                  Admin-created questions are approved immediately and skip review.
                </div>
              )}
              <label className="form-span">
                <span>Title</span>
                <input
                  autoFocus
                  value={form.title}
                  maxLength={200}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, title: event.target.value }))
                  }
                  placeholder="Write a precise question title"
                  aria-invalid={Boolean(formErrors.title)}
                />
                <small className={formErrors.title ? "form-field-error" : ""}>
                  {formErrors.title || `${form.title.length}/200 characters`}
                </small>
              </label>
              <label className="form-span">
                <span>Question content</span>
                <textarea
                  value={form.content}
                  maxLength={500}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, content: event.target.value }))
                  }
                  placeholder="Enter the complete prompt"
                  aria-invalid={Boolean(formErrors.content)}
                />
                <small className={formErrors.content ? "form-field-error" : ""}>
                  {formErrors.content || `${form.content.length}/500 characters`}
                </small>
              </label>
              <label className="form-span">
                <span>Answer</span>
                <textarea
                  className="question-answer"
                  value={form.answer}
                  maxLength={20000}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, answer: event.target.value }))
                  }
                  placeholder="Provide the expected answer or explanation"
                  aria-invalid={Boolean(formErrors.answer)}
                />
                <small className={formErrors.answer ? "form-field-error" : ""}>
                  {formErrors.answer || `${form.answer.length.toLocaleString()}/20,000 characters`}
                </small>
              </label>
              <label>
                <span>Tags</span>
                <input
                  value={form.tags}
                  onChange={(event) =>
                    setForm((current) => ({ ...current, tags: event.target.value }))
                  }
                  placeholder="java, concurrency"
                  aria-invalid={Boolean(formErrors.tags)}
                />
                <small className={formErrors.tags ? "form-field-error" : ""}>
                  {formErrors.tags || "Comma-separated, up to 10"}
                </small>
              </label>
              <label>
                <span>Difficulty</span>
                <select
                  value={form.difficulty}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      difficulty: event.target.value,
                    }))
                  }
                >
                  <option value="">Not set</option>
                  <option value="0">Easy</option>
                  <option value="1">Medium</option>
                  <option value="2">Hard</option>
                </select>
              </label>
              {modalMode === "create" && (
                <label className="form-span">
                  <span>Question bank ID</span>
                  <input
                    inputMode="numeric"
                    value={form.questionBankId}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        questionBankId: event.target.value,
                      }))
                    }
                    placeholder="Optional bank assignment"
                    aria-invalid={Boolean(formErrors.questionBankId)}
                  />
                  {formErrors.questionBankId && (
                    <small className="form-field-error">{formErrors.questionBankId}</small>
                  )}
                </label>
              )}
              <footer className="modal-footer form-span">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={closeModal}
                  disabled={modalBusy}
                >
                  Cancel
                </button>
                <button type="submit" className="primary-action" disabled={modalBusy}>
                  {modalBusy
                    ? "Saving…"
                    : modalMode === "create"
                      ? "Create question"
                      : "Save changes"}
                </button>
              </footer>
            </form>
          </section>
        </div>
      )}

      {deleteTarget && (
        <div className="modal-layer" role="presentation">
          <button
            className="modal-backdrop"
            type="button"
            aria-label="Cancel deletion"
            onClick={() => !modalBusy && setDeleteTarget(null)}
          />
          <section
            className="confirm-card"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="delete-question-title"
          >
            <span className="confirm-icon" aria-hidden="true">
              !
            </span>
            <h2 id="delete-question-title">
              {deleteTarget === "batch" ? "Delete selected questions?" : "Delete question?"}
            </h2>
            <p>
              {deleteTarget === "batch"
                ? `This permanently removes ${selectedIds.size} selected ${
                    selectedIds.size === 1 ? "question" : "questions"
                  }.`
                : `“${deleteTarget.title}” will be permanently removed.`}
            </p>
            <div>
              <button
                type="button"
                className="secondary-button"
                disabled={modalBusy}
                onClick={() => setDeleteTarget(null)}
              >
                Keep questions
              </button>
              <button
                type="button"
                className="danger-button"
                disabled={modalBusy}
                onClick={() => void deleteQuestions()}
              >
                {modalBusy ? "Deleting…" : "Delete permanently"}
              </button>
            </div>
          </section>
        </div>
      )}
    </AdminShell>
  );
}
