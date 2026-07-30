"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiRequest } from "../lib/api";
import { AdminShell } from "./AdminShell";
import { useAuth } from "./AuthProvider";

type SortOrder = "ascend" | "descend" | undefined;

type Creator = {
  id?: string;
  userName?: string | null;
  userAccount?: string | null;
  userAvatar?: string | null;
};

type QuestionBank = {
  id: string;
  title: string;
  picture?: string | null;
  createUserId?: string | null;
  description?: string | null;
  questionCount?: number | null;
  createTime?: string | null;
  editTime?: string | null;
  updateTime?: string | null;
  createUser?: Creator | null;
};

type QuestionBankDetail = {
  id: string;
  title: string;
  picture?: string | null;
  description?: string | null;
  userId?: string | null;
};

type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
};

type Filters = {
  title: string;
  userId: string;
  searchText: string;
};

type BankForm = {
  title: string;
  picture: string;
  description: string;
};

const emptyFilters: Filters = { title: "", userId: "", searchText: "" };
const emptyForm: BankForm = { title: "", picture: "", description: "" };

function compactPayload(value: Record<string, unknown>) {
  return Object.fromEntries(
    Object.entries(value).filter(
      ([, field]) => field !== "" && field !== null && field !== undefined,
    ),
  );
}

function formatDate(value?: string | null) {
  if (!value) return "—";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.valueOf())) return value;
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(parsed);
}

function creatorName(bank: QuestionBank) {
  return (
    bank.createUser?.userName ||
    bank.createUser?.userAccount ||
    bank.createUserId ||
    "Unknown"
  );
}

function creatorInitials(bank: QuestionBank) {
  return creatorName(bank).slice(0, 2).toUpperCase();
}

function isAuthorizationError(error: unknown) {
  return (
    error instanceof ApiError && (error.code === 401 || error.code === 403)
  );
}

export function QuestionBankManagement() {
  const router = useRouter();
  const { token, ready, user, clearSession } = useAuth();
  const [filters, setFilters] = useState<Filters>(emptyFilters);
  const [appliedFilters, setAppliedFilters] =
    useState<Filters>(emptyFilters);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState<string>();
  const [sortOrder, setSortOrder] = useState<SortOrder>();
  const [result, setResult] = useState<PageResult<QuestionBank>>({
    records: [],
    total: 0,
    size: 10,
    current: 1,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [modalMode, setModalMode] = useState<"create" | "edit" | null>(null);
  const [selectedBank, setSelectedBank] = useState<QuestionBank | null>(null);
  const [form, setForm] = useState<BankForm>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [modalBusy, setModalBusy] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<QuestionBank | null>(null);
  const [coverBusyId, setCoverBusyId] = useState<string | null>(null);
  const [batchCoverBusy, setBatchCoverBusy] = useState(false);

  const totalPages = Math.max(1, Math.ceil(result.total / pageSize));

  const requestBody = useMemo(
    () =>
      compactPayload({
        current: page,
        pageSize,
        sortField,
        sortOrder,
        title: appliedFilters.title.trim(),
        userId: appliedFilters.userId.trim(),
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

  const loadBanks = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError("");
    try {
      const response = await apiRequest<PageResult<QuestionBank>>(
        "/question-banks/search",
        { method: "POST", body: JSON.stringify(requestBody) },
        token,
      );
      setResult(response.data);
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load question banks.");
    } finally {
      setLoading(false);
    }
  }, [handleRequestError, requestBody, token, user]);

  useEffect(() => {
    if (!ready || !user) return;
    const timer = window.setTimeout(() => void loadBanks(), 0);
    return () => window.clearTimeout(timer);
  }, [loadBanks, ready, user]);

  function applyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (filters.userId && !/^\d+$/.test(filters.userId)) {
      setError("Creator ID must contain digits only.");
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

  function openCreate() {
    setSelectedBank(null);
    setForm(emptyForm);
    setFormErrors({});
    setModalMode("create");
  }

  async function openEdit(bank: QuestionBank) {
    setSelectedBank(bank);
    setForm(emptyForm);
    setModalMode("edit");
    setModalBusy(true);
    setFormErrors({});
    setError("");
    try {
      const response = await apiRequest<QuestionBankDetail>(
        `/question-banks/${encodeURIComponent(bank.id)}`,
        {},
        token,
      );
      setForm({
        title: response.data.title,
        picture: response.data.picture ?? "",
        description: response.data.description ?? "",
      });
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load question bank details.");
      setModalMode(null);
      setSelectedBank(null);
    } finally {
      setModalBusy(false);
    }
  }

  function closeModal() {
    if (modalBusy) return;
    setModalMode(null);
    setSelectedBank(null);
    setFormErrors({});
  }

  function validateForm() {
    const errors: Record<string, string> = {};
    const title = form.title.trim();
    const description = form.description.trim();
    const picture = form.picture.trim();
    if (!title) errors.title = "Title is required.";
    else if (title.length > 50) errors.title = "Title must be 50 characters or fewer.";
    if (description.length > 200) {
      errors.description = "Description must be 200 characters or fewer.";
    }
    if (picture) {
      try {
        const url = new URL(picture);
        if (!["http:", "https:"].includes(url.protocol)) throw new Error();
      } catch {
        errors.picture = "Enter a valid http or https URL.";
      }
    }
    return errors;
  }

  async function submitBank(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const errors = validateForm();
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setModalBusy(true);
    setError("");
    try {
      const payload = compactPayload({
        title: form.title.trim(),
        picture: form.picture.trim(),
        description: form.description.trim(),
      });
      if (modalMode === "create") {
        await apiRequest<string>(
          "/question-banks",
          { method: "POST", body: JSON.stringify(payload) },
          token,
        );
        setNotice(`Created “${form.title.trim()}”.`);
      } else if (selectedBank) {
        await apiRequest<boolean>(
          `/question-banks/${encodeURIComponent(selectedBank.id)}`,
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
      await loadBanks();
    } catch (requestError) {
      handleRequestError(requestError, "Unable to save the question bank.");
    } finally {
      setModalBusy(false);
    }
  }

  async function deleteBank() {
    if (!deleteTarget) return;
    setModalBusy(true);
    setError("");
    try {
      await apiRequest<boolean>(
        `/question-banks/${encodeURIComponent(deleteTarget.id)}`,
        {
          method: "DELETE",
        },
        token,
      );
      setNotice(`Deleted “${deleteTarget.title}”.`);
      setDeleteTarget(null);
      if (result.records.length === 1 && page > 1) {
        setPage((current) => current - 1);
      } else {
        await loadBanks();
      }
    } catch (requestError) {
      handleRequestError(requestError, "Unable to delete the question bank.");
    } finally {
      setModalBusy(false);
    }
  }

  async function generateCover(bank: QuestionBank) {
    setCoverBusyId(bank.id);
    setError("");
    try {
      await apiRequest<string>(
        `/covers/question-banks/${encodeURIComponent(bank.id)}`,
        { method: "POST" },
        token,
      );
      setNotice(`Generated a new cover for “${bank.title}”.`);
      await loadBanks();
    } catch (requestError) {
      handleRequestError(requestError, "Unable to generate the cover.");
    } finally {
      setCoverBusyId(null);
    }
  }

  async function generateMissingCovers() {
    setBatchCoverBusy(true);
    setError("");
    try {
      const response = await apiRequest<number>(
        "/covers/question-banks/batch",
        { method: "POST" },
        token,
      );
      setNotice(
        `Generated ${response.data.toLocaleString()} ${
          response.data === 1 ? "cover" : "covers"
        }.`,
      );
      await loadBanks();
    } catch (requestError) {
      handleRequestError(requestError, "Unable to generate missing covers.");
    } finally {
      setBatchCoverBusy(false);
    }
  }

  return (
    <AdminShell>
      <main className="users-page banks-page">
        <section className="users-heading">
          <div>
            <p className="eyebrow">Content library</p>
            <h1>Question banks</h1>
            <p>Curate collections, manage their covers, and organize questions.</p>
          </div>
          <div className="heading-actions">
            <button
              type="button"
              className="secondary-button"
              disabled={batchCoverBusy}
              onClick={() => void generateMissingCovers()}
            >
              {batchCoverBusy ? "Generating…" : "Generate missing covers"}
            </button>
            <button type="button" className="primary-action" onClick={openCreate}>
              <span aria-hidden="true">＋</span>
              New question bank
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

        <section className="filter-card" aria-labelledby="bank-filter-title">
          <div className="filter-card__heading">
            <div>
              <h2 id="bank-filter-title">Find a collection</h2>
              <p>Combine precise fields with a broader keyword search.</p>
            </div>
            <button type="button" className="text-button" onClick={clearFilters}>
              Clear filters
            </button>
          </div>
          <form className="filter-grid filter-grid--banks" onSubmit={applyFilters}>
            <label>
              <span>Title</span>
              <input
                value={filters.title}
                onChange={(event) =>
                  setFilters((current) => ({ ...current, title: event.target.value }))
                }
                placeholder="Exact title match"
              />
            </label>
            <label>
              <span>Creator ID</span>
              <input
                inputMode="numeric"
                value={filters.userId}
                onChange={(event) =>
                  setFilters((current) => ({ ...current, userId: event.target.value }))
                }
                placeholder="e.g. 194501…"
              />
            </label>
            <label>
              <span>Keyword</span>
              <input
                value={filters.searchText}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    searchText: event.target.value,
                  }))
                }
                placeholder="Search title or description"
              />
            </label>
            <button className="filter-submit" type="submit">
              Search
            </button>
          </form>
        </section>

        <section className="table-card" aria-labelledby="bank-directory-title">
          <div className="table-card__heading">
            <div>
              <h2 id="bank-directory-title">Collection directory</h2>
              <p>
                {loading
                  ? "Loading collections…"
                  : `${result.total.toLocaleString()} matching ${
                      result.total === 1 ? "bank" : "banks"
                    }`}
              </p>
            </div>
            <button
              className="refresh-button"
              type="button"
              onClick={() => void loadBanks()}
              disabled={loading}
            >
              <span className={loading ? "spin" : ""} aria-hidden="true">
                ↻
              </span>
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
              <button type="button" onClick={() => void loadBanks()}>
                Retry
              </button>
            </div>
          )}

          <div className="user-table-wrap">
            <table className="user-table bank-table">
              <thead>
                <tr>
                  <th>
                    <button type="button" onClick={() => changeSort("title")}>
                      Collection <span>{sortIndicator("title")}</span>
                    </button>
                  </th>
                  <th>Creator</th>
                  <th>
                    <button type="button" onClick={() => changeSort("questionCount")}>
                      Questions <span>{sortIndicator("questionCount")}</span>
                    </button>
                  </th>
                  <th>Description</th>
                  <th>
                    <button type="button" onClick={() => changeSort("editTime")}>
                      Last edited <span>{sortIndicator("editTime")}</span>
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
                      <span aria-hidden="true">▤</span>
                      <strong>No question banks found</strong>
                      <p>Adjust the filters or create the first collection.</p>
                    </td>
                  </tr>
                ) : (
                  result.records.map((bank) => (
                    <tr key={bank.id}>
                      <td>
                        <div className="bank-identity">
                          {bank.picture ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={bank.picture} alt="" />
                          ) : (
                            <span aria-hidden="true">梅</span>
                          )}
                          <div>
                            <strong>{bank.title}</strong>
                            <code>{bank.id}</code>
                          </div>
                        </div>
                      </td>
                      <td>
                        <div className="creator-cell">
                          {bank.createUser?.userAvatar ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={bank.createUser.userAvatar} alt="" />
                          ) : (
                            <span aria-hidden="true">{creatorInitials(bank)}</span>
                          )}
                          <div>
                            <strong>{creatorName(bank)}</strong>
                            <code>{bank.createUserId || "—"}</code>
                          </div>
                        </div>
                      </td>
                      <td>
                        <span className="count-badge">
                          {(bank.questionCount ?? 0).toLocaleString()}
                        </span>
                      </td>
                      <td className="profile-cell">{bank.description || "—"}</td>
                      <td>{formatDate(bank.editTime || bank.updateTime)}</td>
                      <td>
                        <div className="row-actions">
                          <Link
                            className="row-action-link"
                            href={`/question-banks/${encodeURIComponent(bank.id)}/questions`}
                          >
                            Questions
                          </Link>
                          <button
                            type="button"
                            disabled={coverBusyId === bank.id}
                            onClick={() => void generateCover(bank)}
                          >
                            {coverBusyId === bank.id ? "Generating…" : "Cover"}
                          </button>
                          <button type="button" onClick={() => void openEdit(bank)}>
                            Edit
                          </button>
                          <button
                            type="button"
                            className="danger-link"
                            onClick={() => setDeleteTarget(bank)}
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
            className="modal-card"
            role="dialog"
            aria-modal="true"
            aria-labelledby="bank-modal-title"
          >
            <header className="modal-header">
              <div>
                <p className="eyebrow">
                  {modalMode === "create" ? "New collection" : "Collection settings"}
                </p>
                <h2 id="bank-modal-title">
                  {modalMode === "create" ? "Create question bank" : "Edit question bank"}
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
            {modalBusy && modalMode === "edit" ? (
              <div className="modal-loading">
                <span className="button-spinner" />
                Loading collection…
              </div>
            ) : (
              <form className="user-form bank-form" onSubmit={submitBank} noValidate>
                <label className="form-span">
                  <span>Title</span>
                  <input
                    autoFocus
                    value={form.title}
                    maxLength={50}
                    onChange={(event) =>
                      setForm((current) => ({ ...current, title: event.target.value }))
                    }
                    placeholder="e.g. Java concurrency essentials"
                    aria-invalid={Boolean(formErrors.title)}
                  />
                  <small className={formErrors.title ? "form-field-error" : ""}>
                    {formErrors.title || `${form.title.length}/50 characters`}
                  </small>
                </label>
                <label className="form-span">
                  <span>Cover image URL</span>
                  <input
                    type="url"
                    value={form.picture}
                    onChange={(event) =>
                      setForm((current) => ({ ...current, picture: event.target.value }))
                    }
                    placeholder="https://… (optional)"
                    aria-invalid={Boolean(formErrors.picture)}
                  />
                  {formErrors.picture && (
                    <small className="form-field-error">{formErrors.picture}</small>
                  )}
                </label>
                <label className="form-span">
                  <span>Description</span>
                  <textarea
                    value={form.description}
                    maxLength={200}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        description: event.target.value,
                      }))
                    }
                    placeholder="What learners will find in this collection"
                    aria-invalid={Boolean(formErrors.description)}
                  />
                  <small className={formErrors.description ? "form-field-error" : ""}>
                    {formErrors.description || `${form.description.length}/200 characters`}
                  </small>
                </label>
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
                        ? "Create bank"
                        : "Save changes"}
                  </button>
                </footer>
              </form>
            )}
          </section>
        </div>
      )}

      {deleteTarget && (
        <div className="modal-layer" role="presentation">
          <button
            className="modal-backdrop"
            type="button"
            aria-label="Cancel deletion"
            onClick={() => setDeleteTarget(null)}
          />
          <section
            className="confirm-card"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="delete-bank-title"
          >
            <span className="confirm-icon" aria-hidden="true">
              !
            </span>
            <h2 id="delete-bank-title">Delete this question bank?</h2>
            <p>
              “{deleteTarget.title}” and its question relationships will be removed.
              This action cannot be undone.
            </p>
            <div>
              <button
                type="button"
                className="secondary-button"
                onClick={() => setDeleteTarget(null)}
                disabled={modalBusy}
              >
                Keep bank
              </button>
              <button
                type="button"
                className="danger-button"
                onClick={() => void deleteBank()}
                disabled={modalBusy}
              >
                {modalBusy ? "Deleting…" : "Delete bank"}
              </button>
            </div>
          </section>
        </div>
      )}
    </AdminShell>
  );
}
