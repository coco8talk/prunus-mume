"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { BrandMark } from "./BrandMark";
import { useAuth } from "./AuthProvider";

type FormErrors = {
  userAccount?: string;
  userPassword?: string;
};

function validate(userAccount: string, userPassword: string): FormErrors {
  const errors: FormErrors = {};
  const trimmedAccount = userAccount.trim();

  if (!trimmedAccount) {
    errors.userAccount = "Enter your account name.";
  } else if (trimmedAccount.length < 5 || trimmedAccount.length > 15) {
    errors.userAccount = "Account names must be 5–15 characters.";
  }

  if (!userPassword) {
    errors.userPassword = "Enter your password.";
  } else if (userPassword.length < 8 || userPassword.length > 20) {
    errors.userPassword = "Passwords must be 8–20 characters.";
  }

  return errors;
}

export function LoginScreen() {
  const router = useRouter();
  const { user, ready, login } = useAuth();
  const [userAccount, setUserAccount] = useState("");
  const [userPassword, setUserPassword] = useState("");
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (ready && user) {
      router.replace("/users");
    }
  }, [ready, router, user]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validate(userAccount, userPassword);
    setErrors(nextErrors);
    setSubmitError("");

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    setSubmitting(true);

    try {
      await login({
        userAccount: userAccount.trim(),
        userPassword,
      });
      router.replace("/users");
    } catch (error) {
      setSubmitError(
        error instanceof Error ? error.message : "Sign-in failed. Try again.",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-story" aria-label="Console introduction">
        <div className="login-story__top">
          <BrandMark />
          <span className="environment-pill">
            <span className="status-dot" />
            Internal operations
          </span>
        </div>

        <div className="login-story__body">
          <p className="eyebrow eyebrow--light">Learning operations, in focus</p>
          <h1>Keep every question worthy of the answer.</h1>
          <p className="login-story__lede">
            A focused workspace for account administration, question quality,
            and a review queue that never loses its place.
          </p>

          <div className="workflow-rail" aria-label="Admin workflow">
            <div className="workflow-step">
              <span>01</span>
              <div>
                <strong>Monitor</strong>
                <small>See review demand at a glance</small>
              </div>
            </div>
            <div className="workflow-line" />
            <div className="workflow-step">
              <span>02</span>
              <div>
                <strong>Review</strong>
                <small>Keep content standards consistent</small>
              </div>
            </div>
            <div className="workflow-line" />
            <div className="workflow-step">
              <span>03</span>
              <div>
                <strong>Resolve</strong>
                <small>Move the learning library forward</small>
              </div>
            </div>
          </div>
        </div>

        <p className="login-story__foot">
          Protected by role-based access and server-enforced sessions.
        </p>
      </section>

      <section className="login-panel">
        <div className="login-card">
          <div className="login-card__heading">
            <div className="mobile-brand">
              <BrandMark />
            </div>
            <p className="eyebrow">Administrator access</p>
            <h2>Welcome back</h2>
            <p>Use your Prunus Mume account to enter the console.</p>
          </div>

          <form className="login-form" onSubmit={handleSubmit} noValidate>
            <label className="field">
              <span>Account name</span>
              <input
                autoComplete="username"
                autoFocus
                value={userAccount}
                onChange={(event) => {
                  setUserAccount(event.target.value);
                  setErrors((current) => ({
                    ...current,
                    userAccount: undefined,
                  }));
                }}
                placeholder="Enter your account"
                aria-invalid={Boolean(errors.userAccount)}
                aria-describedby={
                  errors.userAccount ? "account-error" : undefined
                }
              />
              {errors.userAccount && (
                <small className="field-error" id="account-error">
                  {errors.userAccount}
                </small>
              )}
            </label>

            <label className="field">
              <span>Password</span>
              <input
                type="password"
                autoComplete="current-password"
                value={userPassword}
                onChange={(event) => {
                  setUserPassword(event.target.value);
                  setErrors((current) => ({
                    ...current,
                    userPassword: undefined,
                  }));
                }}
                placeholder="Enter your password"
                aria-invalid={Boolean(errors.userPassword)}
                aria-describedby={
                  errors.userPassword ? "password-error" : undefined
                }
              />
              {errors.userPassword && (
                <small className="field-error" id="password-error">
                  {errors.userPassword}
                </small>
              )}
            </label>

            {submitError && (
              <div className="form-error" role="alert">
                <span aria-hidden="true">!</span>
                {submitError}
              </div>
            )}

            <button
              className="primary-button"
              type="submit"
              disabled={submitting}
            >
              {submitting ? (
                <>
                  <span className="button-spinner" aria-hidden="true" />
                  Verifying access…
                </>
              ) : (
                <>
                  Enter admin console
                  <span aria-hidden="true">→</span>
                </>
              )}
            </button>
          </form>

          <p className="login-help">
            Only accounts with administrator role <code>userRole = 0</code> can
            continue.
          </p>
        </div>
      </section>
    </main>
  );
}
