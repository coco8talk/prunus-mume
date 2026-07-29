"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useState } from "react";

type Errors = Partial<Record<"account" | "password" | "confirm" | "terms", string>>;

export function AuthCard({ mode }: { mode: "login" | "register" }) {
  const router = useRouter();
  const isLogin = mode === "login";
  const [account, setAccount] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [visible, setVisible] = useState(false);
  const [remember, setRemember] = useState(true);
  const [terms, setTerms] = useState(false);
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors: Errors = {};

    if (account.trim().length < 3) nextErrors.account = "请输入至少 3 个字符的账号";
    if (password.length < 6) nextErrors.password = "密码至少需要 6 个字符";
    if (!isLogin && confirm !== password) nextErrors.confirm = "两次输入的密码不一致";
    if (!isLogin && !terms) nextErrors.terms = "请先阅读并同意服务条款";

    setErrors(nextErrors);
    if (Object.keys(nextErrors).length > 0) return;

    setSubmitting(true);
    window.setTimeout(() => {
      if (isLogin) {
        router.push("/");
      } else {
        router.push("/login?registered=1");
      }
    }, 700);
  }

  return (
    <main className="auth-page app-shell" data-od-id={`${mode}-page`}>
      <section className="auth-panel">
        <div className="auth-intro">
          <span className="brand-mark" aria-hidden="true">梅</span>
          <p className="section-kicker">{isLogin ? "欢迎回来" : "加入梅问"}</p>
          <h1>{isLogin ? "继续把知识练成直觉。" : "从今天开始，稳步掌握。"}</h1>
          <p>
            {isLogin
              ? "你的进度、收藏与学习计划，都已为你准备好。"
              : "建立个人学习节奏，保存进度，并解锁持续反馈。"}
          </p>
        </div>

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
          <div className="auth-form-heading">
            <p className="section-kicker">{isLogin ? "账号登录" : "创建账号"}</p>
            <h2>{isLogin ? "登录梅问" : "注册梅问"}</h2>
          </div>

          <label className="field">
            <span>账号</span>
            <input
              value={account}
              onChange={(event) => {
                setAccount(event.target.value);
                setErrors((current) => ({ ...current, account: undefined }));
              }}
              autoComplete="username"
              placeholder="请输入账号"
              aria-invalid={Boolean(errors.account)}
              aria-describedby={errors.account ? "account-error" : undefined}
            />
            {errors.account && <small className="field-error" id="account-error">{errors.account}</small>}
          </label>

          <label className="field">
            <span>密码</span>
            <span className="password-field">
              <input
                type={visible ? "text" : "password"}
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value);
                  setErrors((current) => ({ ...current, password: undefined }));
                }}
                autoComplete={isLogin ? "current-password" : "new-password"}
                placeholder="请输入密码"
                aria-invalid={Boolean(errors.password)}
                aria-describedby={errors.password ? "password-error" : undefined}
              />
              <button
                type="button"
                onClick={() => setVisible((current) => !current)}
                aria-label={visible ? "隐藏密码" : "显示密码"}
              >
                {visible ? "隐藏" : "显示"}
              </button>
            </span>
            {errors.password && <small className="field-error" id="password-error">{errors.password}</small>}
          </label>

          {!isLogin && (
            <label className="field">
              <span>确认密码</span>
              <input
                type={visible ? "text" : "password"}
                value={confirm}
                onChange={(event) => {
                  setConfirm(event.target.value);
                  setErrors((current) => ({ ...current, confirm: undefined }));
                }}
                autoComplete="new-password"
                placeholder="请再次输入密码"
                aria-invalid={Boolean(errors.confirm)}
                aria-describedby={errors.confirm ? "confirm-error" : undefined}
              />
              {errors.confirm && <small className="field-error" id="confirm-error">{errors.confirm}</small>}
            </label>
          )}

          {isLogin ? (
            <label className="check-row">
              <input
                type="checkbox"
                checked={remember}
                onChange={(event) => setRemember(event.target.checked)}
              />
              <span>在这台设备上保持登录</span>
            </label>
          ) : (
            <div>
              <label className="check-row">
                <input
                  type="checkbox"
                  checked={terms}
                  onChange={(event) => {
                    setTerms(event.target.checked);
                    setErrors((current) => ({ ...current, terms: undefined }));
                  }}
                  aria-invalid={Boolean(errors.terms)}
                />
                <span>我已阅读并同意服务条款与隐私政策</span>
              </label>
              {errors.terms && <small className="field-error">{errors.terms}</small>}
            </div>
          )}

          <button
            className="form-primary"
            type="submit"
            disabled={submitting}
            data-od-id={`${mode}-submit`}
          >
            {submitting ? (isLogin ? "正在登录…" : "正在注册…") : (isLogin ? "登录" : "注册")}
          </button>

          <p className="auth-switch">
            {isLogin ? "还没有账号？" : "已经有账号？"}
            <Link href={isLogin ? "/register" : "/login"}>
              {isLogin ? "立即注册" : "返回登录"}
            </Link>
          </p>
        </form>
      </section>
    </main>
  );
}
