"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, apiRequest } from "../lib/api";
import { useAuth } from "./AuthProvider";
import { AdminShell } from "./AdminShell";

type UserRole = 0 | 1 | 2 | 3;

type UserRecord = {
  id: string;
  userAccount: string;
  userName: string;
  userAvatar?: string | null;
  userRole: UserRole;
  userProfile?: string | null;
  phoneNumber?: string | null;
  email?: string | null;
  grade?: number | null;
  workExperience?: string | null;
  expertiseDirection?: string | null;
  vipNumber?: string | null;
  vipCode?: string | null;
  vipExpireTime?: string | null;
  createTime?: string | null;
  shareCode?: string | null;
  inviteUserId?: string | null;
};

type UserPage = {
  records: UserRecord[];
  total: number;
  size: number;
  current: number;
};

type Filters = {
  id: string;
  userName: string;
  userRole: "" | `${UserRole}`;
  userProfile: string;
};

type SortOrder = "ascend" | "descend" | undefined;
type ModalMode = "create" | "edit" | "view" | null;

type UserForm = {
  userAccount: string;
  userName: string;
  userRole: UserRole;
  userAvatar: string;
  userProfile: string;
};

const emptyFilters: Filters = {
  id: "",
  userName: "",
  userRole: "",
  userProfile: "",
};

const emptyForm: UserForm = {
  userAccount: "",
  userName: "",
  userRole: 1,
  userAvatar: "",
  userProfile: "",
};

const roleLabels: Record<UserRole, string> = {
  0: "Admin",
  1: "User",
  2: "VIP",
  3: "Banned",
};

const detailFields: Array<[keyof UserRecord, string]> = [
  ["phoneNumber", "Phone"],
  ["email", "Email"],
  ["grade", "Grade"],
  ["workExperience", "Work experience"],
  ["expertiseDirection", "Expertise"],
  ["vipNumber", "VIP number"],
  ["vipCode", "VIP code"],
  ["vipExpireTime", "VIP expires"],
  ["shareCode", "Share code"],
  ["inviteUserId", "Invited by"],
];

function compactPayload<T extends Record<string, unknown>>(value: T) {
  return Object.fromEntries(
    Object.entries(value).filter(
      ([, field]) => field !== "" && field !== null && field !== undefined,
    ),
  );
}

function displayDate(value?: string | null) {
  if (!value) return "—";
  const parsed = new Date(value);
  if (Number.isNaN(parsed.valueOf())) return value;
  return new Intl.DateTimeFormat("en", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(parsed);
}

function displayValue(value: unknown) {
  if (value === null || value === undefined || value === "") return "—";
  return String(value);
}

function initials(user: UserRecord) {
  return (user.userName || user.userAccount).slice(0, 2).toUpperCase();
}

function roleLabel(role: number) {
  return roleLabels[role as UserRole] ?? "Unknown";
}

function isAuthorizationError(error: unknown) {
  return (
    error instanceof ApiError &&
    (error.code === 401 || error.code === 403)
  );
}

export function UserManagement() {
  const router = useRouter();
  const { token, ready, user, clearSession } = useAuth();
  const [filters, setFilters] = useState<Filters>(emptyFilters);
  const [appliedFilters, setAppliedFilters] = useState<Filters>(emptyFilters);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortField, setSortField] = useState<string>();
  const [sortOrder, setSortOrder] = useState<SortOrder>();
  const [result, setResult] = useState<UserPage>({
    records: [],
    total: 0,
    size: 10,
    current: 1,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [modalMode, setModalMode] = useState<ModalMode>(null);
  const [selectedUser, setSelectedUser] = useState<UserRecord | null>(null);
  const [form, setForm] = useState<UserForm>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [modalBusy, setModalBusy] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<UserRecord | null>(null);

  const totalPages = Math.max(1, Math.ceil(result.total / pageSize));

  const requestBody = useMemo(
    () =>
      compactPayload({
        current: page,
        pageSize,
        sortField,
        sortOrder,
        id: appliedFilters.id.trim(),
        userName: appliedFilters.userName.trim(),
        userRole:
          appliedFilters.userRole === ""
            ? undefined
            : Number(appliedFilters.userRole),
        userProfile: appliedFilters.userProfile.trim(),
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

  const loadUsers = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    setError("");
    try {
      const response = await apiRequest<UserPage>(
        "/users/search",
        { method: "POST", body: JSON.stringify(requestBody) },
        token,
      );
      setResult(response.data);
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load users.");
    } finally {
      setLoading(false);
    }
  }, [handleRequestError, requestBody, token, user]);

  useEffect(() => {
    if (!ready || !user) return;
    const timer = window.setTimeout(() => void loadUsers(), 0);
    return () => window.clearTimeout(timer);
  }, [loadUsers, ready, user]);

  function applyFilters(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (filters.id && !/^\d+$/.test(filters.id)) {
      setError("User ID must contain digits only.");
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
      return;
    }
    if (sortOrder === "ascend") {
      setSortOrder("descend");
      return;
    }
    if (sortOrder === "descend") {
      setSortField(undefined);
      setSortOrder(undefined);
      return;
    }
    setSortOrder("ascend");
  }

  function sortIndicator(field: string) {
    if (sortField !== field) return "↕";
    return sortOrder === "ascend" ? "↑" : "↓";
  }

  async function openUser(userRecord: UserRecord, mode: "view" | "edit") {
    setModalMode(mode);
    setSelectedUser(null);
    setModalBusy(true);
    setError("");
    try {
      const response = await apiRequest<UserRecord>(
        `/users/${encodeURIComponent(userRecord.id)}`,
        {},
        token,
      );
      setSelectedUser(response.data);
      setForm({
        userAccount: response.data.userAccount,
        userName: response.data.userName,
        userRole: response.data.userRole,
        userAvatar: response.data.userAvatar ?? "",
        userProfile: response.data.userProfile ?? "",
      });
    } catch (requestError) {
      handleRequestError(requestError, "Unable to load user details.");
      setModalMode(null);
    } finally {
      setModalBusy(false);
    }
  }

  function openCreate() {
    setForm(emptyForm);
    setFormErrors({});
    setSelectedUser(null);
    setModalMode("create");
  }

  function closeModal() {
    if (modalBusy) return;
    setModalMode(null);
    setSelectedUser(null);
    setFormErrors({});
  }

  function validateForm() {
    const errors: Record<string, string> = {};
    if (modalMode === "create") {
      if (!form.userAccount.trim()) {
        errors.userAccount = "Account name is required.";
      } else if (!/^[A-Za-z0-9_]{5,15}$/.test(form.userAccount.trim())) {
        errors.userAccount = "Use 5–15 letters, numbers or underscores.";
      }
    }
    if (!form.userName.trim()) {
      errors.userName = "Display name is required.";
    } else if (form.userName.trim().length > 20) {
      errors.userName = "Display name must be 20 characters or fewer.";
    }
    if (form.userProfile.length > 150) {
      errors.userProfile = "Profile must be 150 characters or fewer.";
    }
    return errors;
  }

  async function submitUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const errors = validateForm();
    setFormErrors(errors);
    if (Object.keys(errors).length) return;

    setModalBusy(true);
    setError("");
    try {
      if (modalMode === "create") {
        await apiRequest<string>(
          "/users",
          {
            method: "POST",
            body: JSON.stringify(
              compactPayload({
                userAccount: form.userAccount.trim(),
                userName: form.userName.trim(),
                userRole: form.userRole,
                userAvatar: form.userAvatar.trim(),
              }),
            ),
          },
          token,
        );
        setNotice(`Created ${form.userName.trim()}.`);
      } else if (modalMode === "edit" && selectedUser) {
        await apiRequest<void>(
          `/users/${encodeURIComponent(selectedUser.id)}`,
          {
            method: "PUT",
            body: JSON.stringify(
              compactPayload({
                userName: form.userName.trim(),
                userRole: form.userRole,
                userAvatar: form.userAvatar.trim(),
                userProfile: form.userProfile.trim(),
              }),
            ),
          },
          token,
        );
        setNotice(`Updated ${form.userName.trim()}.`);
      }
      setModalBusy(false);
      closeModal();
      await loadUsers();
    } catch (requestError) {
      handleRequestError(requestError, "Unable to save the user.");
    } finally {
      setModalBusy(false);
    }
  }

  async function deleteUser() {
    if (!deleteTarget) return;
    setModalBusy(true);
    setError("");
    try {
      await apiRequest<void>(
        `/users/${encodeURIComponent(deleteTarget.id)}`,
        { method: "DELETE" },
        token,
      );
      setNotice(`Deleted ${deleteTarget.userName}.`);
      setDeleteTarget(null);
      if (result.records.length === 1 && page > 1) {
        setPage((current) => current - 1);
      } else {
        await loadUsers();
      }
    } catch (requestError) {
      handleRequestError(requestError, "Unable to delete the user.");
    } finally {
      setModalBusy(false);
    }
  }

  return (
    <AdminShell>
      <main className="users-page">
        <section className="users-heading">
          <div>
            <p className="eyebrow">Directory</p>
            <h1>Users</h1>
            <p>Find accounts, inspect access, and keep profiles current.</p>
          </div>
          <button className="primary-action" type="button" onClick={openCreate}>
            <span aria-hidden="true">＋</span>
            Add user
          </button>
        </section>

        {notice && (
          <div className="notice-banner" role="status">
            <span aria-hidden="true">✓</span>
            {notice}
            <button
              type="button"
              aria-label="Dismiss message"
              onClick={() => setNotice("")}
            >
              ×
            </button>
          </div>
        )}

        <section className="filter-card" aria-labelledby="filter-title">
          <div className="filter-card__heading">
            <div>
              <h2 id="filter-title">Filter users</h2>
              <p>Narrow the directory by identity, role, or profile content.</p>
            </div>
            <button type="button" className="text-button" onClick={clearFilters}>
              Clear filters
            </button>
          </div>
          <form className="filter-grid" onSubmit={applyFilters}>
            <label>
              <span>User ID</span>
              <input
                inputMode="numeric"
                value={filters.id}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    id: event.target.value,
                  }))
                }
                placeholder="e.g. 194501…"
              />
            </label>
            <label>
              <span>Display name</span>
              <input
                value={filters.userName}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    userName: event.target.value,
                  }))
                }
                placeholder="Search names"
              />
            </label>
            <label>
              <span>Role</span>
              <select
                value={filters.userRole}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    userRole: event.target.value as Filters["userRole"],
                  }))
                }
              >
                <option value="">All roles</option>
                {Object.entries(roleLabels).map(([value, label]) => (
                  <option value={value} key={value}>
                    {label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              <span>Profile contains</span>
              <input
                value={filters.userProfile}
                onChange={(event) =>
                  setFilters((current) => ({
                    ...current,
                    userProfile: event.target.value,
                  }))
                }
                placeholder="Keyword"
              />
            </label>
            <button className="filter-submit" type="submit">
              Search
            </button>
          </form>
        </section>

        <section className="table-card" aria-labelledby="directory-title">
          <div className="table-card__heading">
            <div>
              <h2 id="directory-title">User directory</h2>
              <p>
                {loading
                  ? "Loading records…"
                  : `${result.total.toLocaleString()} matching ${
                      result.total === 1 ? "user" : "users"
                    }`}
              </p>
            </div>
            <button
              className="refresh-button"
              type="button"
              onClick={() => void loadUsers()}
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
              <button type="button" onClick={() => void loadUsers()}>
                Retry
              </button>
            </div>
          )}

          <div className="user-table-wrap">
            <table className="user-table">
              <thead>
                <tr>
                  <th>
                    <button type="button" onClick={() => changeSort("id")}>
                      User <span>{sortIndicator("id")}</span>
                    </button>
                  </th>
                  <th>Account</th>
                  <th>
                    <button
                      type="button"
                      onClick={() => changeSort("userName")}
                    >
                      Display name <span>{sortIndicator("userName")}</span>
                    </button>
                  </th>
                  <th>Role</th>
                  <th>Profile</th>
                  <th>
                    <button
                      type="button"
                      onClick={() => changeSort("createTime")}
                    >
                      Created <span>{sortIndicator("createTime")}</span>
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
                      {Array.from({ length: 7 }).map((__, cell) => (
                        <td key={cell}>
                          <span />
                        </td>
                      ))}
                    </tr>
                  ))
                ) : result.records.length === 0 ? (
                  <tr>
                    <td className="empty-state" colSpan={7}>
                      <span aria-hidden="true">◎</span>
                      <strong>No users found</strong>
                      <p>Adjust the filters or add a new account.</p>
                    </td>
                  </tr>
                ) : (
                  result.records.map((userRecord) => (
                    <tr key={userRecord.id}>
                      <td>
                        <div className="user-identity">
                          {userRecord.userAvatar ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={userRecord.userAvatar} alt="" />
                          ) : (
                            <span aria-hidden="true">
                              {initials(userRecord)}
                            </span>
                          )}
                          <code title={userRecord.id}>{userRecord.id}</code>
                        </div>
                      </td>
                      <td className="account-cell">{userRecord.userAccount}</td>
                      <td>
                        <strong>{userRecord.userName}</strong>
                      </td>
                      <td>
                        <span
                          className={`role-badge role-badge--${userRecord.userRole}`}
                        >
                          {roleLabel(userRecord.userRole)}
                        </span>
                      </td>
                      <td className="profile-cell">
                        {userRecord.userProfile || "—"}
                      </td>
                      <td>{displayDate(userRecord.createTime)}</td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            onClick={() => void openUser(userRecord, "view")}
                          >
                            View
                          </button>
                          <button
                            type="button"
                            onClick={() => void openUser(userRecord, "edit")}
                          >
                            Edit
                          </button>
                          <button
                            className="danger-link"
                            type="button"
                            onClick={() => setDeleteTarget(userRecord)}
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
            className={`modal-card${
              modalMode === "view" ? " modal-card--detail" : ""
            }`}
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
          >
            <header className="modal-header">
              <div>
                <p className="eyebrow">
                  {modalMode === "create"
                    ? "New account"
                    : modalMode === "edit"
                      ? "Account settings"
                      : "User record"}
                </p>
                <h2 id="modal-title">
                  {modalMode === "create"
                    ? "Add user"
                    : modalMode === "edit"
                      ? "Edit user"
                      : selectedUser?.userName || "User details"}
                </h2>
              </div>
              <button
                className="modal-close"
                type="button"
                aria-label="Close dialog"
                onClick={closeModal}
              >
                ×
              </button>
            </header>

            {modalBusy && !selectedUser ? (
              <div className="modal-loading">
                <span className="button-spinner" />
                Loading user…
              </div>
            ) : modalMode === "view" && selectedUser ? (
              <div className="detail-content">
                <div className="detail-profile">
                  {selectedUser.userAvatar ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img src={selectedUser.userAvatar} alt="" />
                  ) : (
                    <span aria-hidden="true">{initials(selectedUser)}</span>
                  )}
                  <div>
                    <strong>{selectedUser.userName}</strong>
                    <p>@{selectedUser.userAccount}</p>
                    <span
                      className={`role-badge role-badge--${selectedUser.userRole}`}
                    >
                      {roleLabel(selectedUser.userRole)}
                    </span>
                  </div>
                </div>
                <dl className="detail-grid">
                  <div>
                    <dt>User ID</dt>
                    <dd>
                      <code>{selectedUser.id}</code>
                    </dd>
                  </div>
                  <div>
                    <dt>Created</dt>
                    <dd>{displayDate(selectedUser.createTime)}</dd>
                  </div>
                  {detailFields.map(([field, label]) => (
                    <div key={field}>
                      <dt>{label}</dt>
                      <dd>
                        {field === "vipExpireTime"
                          ? displayDate(selectedUser[field] as string | null)
                          : displayValue(selectedUser[field])}
                      </dd>
                    </div>
                  ))}
                </dl>
                <div className="detail-profile-copy">
                  <span>Profile</span>
                  <p>{selectedUser.userProfile || "No profile provided."}</p>
                </div>
                <footer className="modal-footer">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={closeModal}
                  >
                    Close
                  </button>
                  <button
                    type="button"
                    className="primary-action"
                    onClick={() => {
                      setModalMode("edit");
                      setForm({
                        userAccount: selectedUser.userAccount,
                        userName: selectedUser.userName,
                        userRole: selectedUser.userRole,
                        userAvatar: selectedUser.userAvatar ?? "",
                        userProfile: selectedUser.userProfile ?? "",
                      });
                    }}
                  >
                    Edit user
                  </button>
                </footer>
              </div>
            ) : (
              <form className="user-form" onSubmit={submitUser} noValidate>
                <label>
                  <span>Account name</span>
                  <input
                    value={form.userAccount}
                    disabled={modalMode === "edit"}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        userAccount: event.target.value,
                      }))
                    }
                    placeholder="letters_numbers"
                    aria-invalid={Boolean(formErrors.userAccount)}
                  />
                  {formErrors.userAccount && (
                    <small>{formErrors.userAccount}</small>
                  )}
                  {modalMode === "edit" && (
                    <small>Account names cannot be changed.</small>
                  )}
                </label>
                <label>
                  <span>Display name</span>
                  <input
                    value={form.userName}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        userName: event.target.value,
                      }))
                    }
                    placeholder="Full name"
                    aria-invalid={Boolean(formErrors.userName)}
                  />
                  {formErrors.userName && <small>{formErrors.userName}</small>}
                </label>
                <label>
                  <span>Role</span>
                  <select
                    value={form.userRole}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        userRole: Number(event.target.value) as UserRole,
                      }))
                    }
                  >
                    {Object.entries(roleLabels).map(([value, label]) => (
                      <option value={value} key={value}>
                        {label}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  <span>Avatar URL</span>
                  <input
                    type="url"
                    value={form.userAvatar}
                    onChange={(event) =>
                      setForm((current) => ({
                        ...current,
                        userAvatar: event.target.value,
                      }))
                    }
                    placeholder="https://…"
                  />
                </label>
                {modalMode === "edit" && (
                  <label className="form-span">
                    <span>Profile</span>
                    <textarea
                      value={form.userProfile}
                      onChange={(event) =>
                        setForm((current) => ({
                          ...current,
                          userProfile: event.target.value,
                        }))
                      }
                      placeholder="Short bio or operator note"
                      aria-invalid={Boolean(formErrors.userProfile)}
                    />
                    <small
                      className={
                        formErrors.userProfile ? "form-field-error" : ""
                      }
                    >
                      {formErrors.userProfile ||
                        `${form.userProfile.length}/150 characters`}
                    </small>
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
                  <button
                    type="submit"
                    className="primary-action"
                    disabled={modalBusy}
                  >
                    {modalBusy
                      ? "Saving…"
                      : modalMode === "create"
                        ? "Create user"
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
            onClick={() => !modalBusy && setDeleteTarget(null)}
          />
          <section
            className="confirm-card"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="delete-title"
          >
            <span className="confirm-icon" aria-hidden="true">
              !
            </span>
            <h2 id="delete-title">Delete {deleteTarget.userName}?</h2>
            <p>
              This permanently removes the account and associated session data.
              This action cannot be undone.
            </p>
            <div>
              <button
                className="secondary-button"
                type="button"
                disabled={modalBusy}
                onClick={() => setDeleteTarget(null)}
              >
                Cancel
              </button>
              <button
                className="danger-button"
                type="button"
                disabled={modalBusy}
                onClick={() => void deleteUser()}
              >
                {modalBusy ? "Deleting…" : "Delete user"}
              </button>
            </div>
          </section>
        </div>
      )}
    </AdminShell>
  );
}
