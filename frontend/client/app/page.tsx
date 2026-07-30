"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useAuth } from "./components/AuthProvider";
import { BankVisual } from "./components/BankVisual";
import { VipBadge } from "./components/VipBadge";
import type { QuestionBank } from "./data/models";
import { errorMessage } from "./lib/api";
import { bankService, signInService } from "./lib/services";

const weekLabels = ["一", "二", "三", "四", "五", "六", "日"];

export default function Home() {
  const router = useRouter();
  const { status: authStatus } = useAuth();
  const [query, setQuery] = useState("");
  const [featuredBanks, setFeaturedBanks] = useState<QuestionBank[]>([]);
  const [signedIn, setSignedIn] = useState(false);
  const [streak, setStreak] = useState(0);
  const [signedDays, setSignedDays] = useState<number[]>([]);
  const [signing, setSigning] = useState(false);
  const [signMessage, setSignMessage] = useState("");
  const [featuredError, setFeaturedError] = useState("");
  const [activeBank, setActiveBank] = useState("");

  useEffect(() => {
    const controller = new AbortController();
    bankService.search({ current: 1, pageSize: 3 }, controller.signal)
      .then((result) => {
        setFeaturedBanks(result.records);
        setActiveBank(result.records[0]?.id ?? "");
        setFeaturedError("");
      })
      .catch((error) => {
        setFeaturedBanks([]);
        setFeaturedError(errorMessage(error));
      });
    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (authStatus !== "authenticated") return;
    const controller = new AbortController();
    const today = new Date();
    Promise.all([
      signInService.today(controller.signal),
      signInService.streak(controller.signal),
      signInService.records(today.getFullYear(), today.getMonth() + 1, controller.signal),
    ]).then(([todayStatus, currentStreak, records]) => {
      setSignedIn(todayStatus);
      setStreak(currentStreak);
      setSignedDays(records);
    }).catch((error) => {
      if (!controller.signal.aborted) setSignMessage(errorMessage(error));
    });
    return () => controller.abort();
  }, [authStatus]);

  const activity = useMemo(() => {
    const today = new Date();
    const mondayOffset = (today.getDay() + 6) % 7;
    const monday = new Date(today.getFullYear(), today.getMonth(), today.getDate() - mondayOffset);
    return weekLabels.map((day, index) => {
      const date = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + index);
      return {
        day,
        date: date.getDate(),
        today: date.toDateString() === today.toDateString(),
        done: authStatus === "authenticated" &&
          date.getMonth() === today.getMonth() &&
          signedDays.includes(date.getDate()),
      };
    });
  }, [authStatus, signedDays]);
  const hasSignedIn = authStatus === "authenticated" && signedIn;
  const visibleStreak = authStatus === "authenticated" ? streak : 0;

  const filteredBanks = useMemo(() => {
    const term = query.trim().toLowerCase();
    if (!term) return featuredBanks;
    return featuredBanks.filter((bank) =>
      [bank.title, bank.eyebrow, bank.description].some((item) =>
        item.toLowerCase().includes(term),
      ),
    );
  }, [featuredBanks, query]);

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    router.push(query.trim() ? `/banks?query=${encodeURIComponent(query.trim())}` : "/banks");
  }

  async function handleSignIn() {
    if (authStatus !== "authenticated") {
      router.push("/login?next=%2F");
      return;
    }
    if (signedIn || signing) return;
    setSigning(true);
    setSignMessage("");
    try {
      await signInService.add();
      const today = new Date();
      const [nextStreak, records] = await Promise.all([
        signInService.streak(),
        signInService.records(today.getFullYear(), today.getMonth() + 1),
      ]);
      setSignedIn(true);
      setStreak(nextStreak);
      setSignedDays(records);
    } catch (error) {
      setSignMessage(errorMessage(error));
    } finally {
      setSigning(false);
    }
  }

  return (
    <main className="app-shell" data-od-id="learning-dashboard">
      <section className="hero" data-od-id="dashboard-hero">
        <div className="hero-copy">
          <p className="section-kicker">今天，继续向前一步</p>
          <h1 data-od-id="hero-heading">把复杂知识，<br />练成你的直觉。</h1>
          <p className="hero-description">
            精选题库、清晰进度与持续反馈，让每一次练习都更接近真正掌握。
          </p>
          <form className="search-box" onSubmit={handleSearch} role="search">
            <label className="sr-only" htmlFor="bank-search">搜索题库或知识点</label>
            <span className="search-glyph" aria-hidden="true" />
            <input
              id="bank-search"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="搜索题库或知识点"
            />
            <button type="submit" data-od-id="search-button">搜索</button>
          </form>
          <div className="trend-row" aria-label="热门搜索">
            <span>热门：</span>
            {["React", "系统设计", "动态规划"].map((item) => (
              <button key={item} type="button" onClick={() => setQuery(item)}>
                {item}
              </button>
            ))}
          </div>
        </div>

        <aside className="today-card" data-od-id="today-progress-card">
          <div className="today-card-top">
            <div>
              <span className="eyebrow">精选题库</span>
              <strong>{featuredBanks.length}<span> 个推荐</span></strong>
            </div>
            <div className="progress-ring" aria-label={`${featuredBanks.length} 个推荐题库`}>
              <span>{featuredBanks.length}</span>
            </div>
          </div>
          <div className="mini-divider" />
          <div className="resume-row">
            <div className="resume-icon" aria-hidden="true">题</div>
            <div>
              <span>推荐开始</span>
              <b>{featuredBanks[0]?.title ?? "探索梅问题库"}</b>
            </div>
          </div>
          <Link
            className="primary-button"
            href={featuredBanks[0] ? `/banks/${featuredBanks[0].id}` : "/banks"}
            data-od-id="resume-learning-button"
          >
            开始探索
            <span aria-hidden="true">→</span>
          </Link>
        </aside>
      </section>

      <section className="content-grid">
        <div className="main-column">
          <div className="section-heading">
            <div>
              <p className="section-kicker">为你精选</p>
              <h2 id="featured-banks" tabIndex={-1} data-od-id="featured-banks-heading">
                推荐题库
              </h2>
            </div>
            <Link href="/banks">查看全部 <span aria-hidden="true">→</span></Link>
          </div>

          <div className="bank-list" id="all-banks">
            {featuredError && <div className="feedback error" role="alert">{featuredError}</div>}
            {filteredBanks.map((bank) => (
              <article
                className={`bank-card ${activeBank === bank.id ? "selected" : ""}`}
                key={bank.id}
                data-od-id={`bank-card-${bank.id}`}
              >
                <Link
                  className={`bank-visual ${bank.tone}`}
                  href={`/banks/${bank.id}`}
                  aria-label={`打开${bank.title}`}
                  onClick={() => setActiveBank(bank.id)}
                >
                  <BankVisual label={bank.eyebrow} />
                </Link>
                <div className="bank-info">
                  <div className="bank-meta">
                    <span>{bank.eyebrow}</span>
                    <span>{bank.level}</span>
                  </div>
                  <h3>{bank.title}</h3>
                  <p>{bank.description}</p>
                  <div className="bank-footer">
                    <div className="progress-track" aria-label={`已完成 ${bank.progress}%`}>
                      <span style={{ width: `${bank.progress}%` }} />
                    </div>
                    <small>{bank.progress}% · {bank.questions} 题</small>
                  </div>
                </div>
              </article>
            ))}
            {filteredBanks.length === 0 && (
              <div className="empty-state" role="status">
                <strong>暂时没有匹配的题库</strong>
                <span>试试“前端”“算法”或“系统设计”。</span>
              </div>
            )}
          </div>
        </div>

        <aside className="side-column" id="learning-plan">
          <section className="streak-card" data-od-id="daily-signin-card">
            <div className="streak-header">
              <div>
                <span className="eyebrow">连续签到</span>
                <strong><em>{visibleStreak}</em> 天</strong>
              </div>
              <span className="streak-mark" aria-hidden="true">{visibleStreak}</span>
            </div>
            <div className="week-row">
              {activity.map((item) => (
                  <div key={item.day} className={item.today ? "today" : ""}>
                    <span className={item.done ? "done" : ""}>
                      {item.done ? "✓" : item.date}
                    </span>
                    <small>{item.day}</small>
                  </div>
              ))}
            </div>
            {signMessage && <p className="field-error" role="alert">{signMessage}</p>}
            <button
              className={hasSignedIn ? "signin-button complete" : "signin-button"}
              type="button"
              onClick={handleSignIn}
              disabled={hasSignedIn || signing}
              data-od-id="daily-signin-button"
            >
              {hasSignedIn ? "今日已签到" : signing ? "正在签到…" : authStatus === "authenticated" ? "完成今日签到" : "登录后签到"}
            </button>
          </section>

          <section className="plan-card" data-od-id="weekly-plan-card">
            <div className="plan-title">
              <div>
                <span className="eyebrow">本周签到</span>
                <h3>保持稳定学习节奏</h3>
              </div>
              <span className="plan-count">{activity.filter((item) => item.done).length} / 7</span>
            </div>
            <div className="plan-bars" aria-label="本周练习趋势">
              {activity.map((item) => (
                <div key={item.day}>
                  <span style={{ height: item.done ? "76%" : "12%" }} className={item.done ? "filled" : ""} />
                  <small>{item.day}</small>
                </div>
              ))}
            </div>
            <div className="plan-summary">
              <span>本周已签到</span>
              <b>{activity.filter((item) => item.done).length} 天</b>
            </div>
          </section>

          <Link className="vip-card" href="/membership" data-od-id="membership-upgrade-card">
            <VipBadge />
            <div>
              <strong>开通梅问会员</strong>
              <span>解锁会员题完整解析与全部会员题库</span>
            </div>
            <span aria-hidden="true">→</span>
          </Link>
        </aside>
      </section>
    </main>
  );
}
