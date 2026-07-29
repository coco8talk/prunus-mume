"use client";

import { FormEvent, useMemo, useState } from "react";

const banks = [
  {
    id: "frontend",
    eyebrow: "前端工程",
    title: "现代前端核心题库",
    description: "从 JavaScript 运行机制到 React 状态设计，建立稳固的工程基础。",
    progress: 68,
    questions: 186,
    level: "中级",
    tone: "blue",
  },
  {
    id: "algorithm",
    eyebrow: "算法与结构",
    title: "高频算法训练营",
    description: "按模式拆解数组、链表、树与动态规划，适合系统刷题。",
    progress: 34,
    questions: 240,
    level: "进阶",
    tone: "coral",
  },
  {
    id: "system-design",
    eyebrow: "系统设计",
    title: "可扩展系统设计",
    description: "围绕真实场景理解缓存、消息队列、分布式一致性与容量规划。",
    progress: 12,
    questions: 96,
    level: "高级",
    tone: "green",
  },
];

const activity = [
  { day: "一", done: true },
  { day: "二", done: true },
  { day: "三", done: true },
  { day: "四", done: true },
  { day: "五", done: true },
  { day: "六", done: false },
  { day: "日", done: false },
];

export default function Home() {
  const [query, setQuery] = useState("");
  const [signedIn, setSignedIn] = useState(false);
  const [activeBank, setActiveBank] = useState("frontend");

  const filteredBanks = useMemo(() => {
    const term = query.trim().toLowerCase();
    if (!term) return banks;
    return banks.filter((bank) =>
      [bank.title, bank.eyebrow, bank.description].some((item) =>
        item.toLowerCase().includes(term),
      ),
    );
  }, [query]);

  function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    document.getElementById("featured-banks")?.focus();
  }

  return (
    <main className="app-shell" data-od-id="learning-dashboard">
      <header className="topbar" data-od-id="top-navigation">
        <a className="brand" href="#" aria-label="梅问首页">
          <span className="brand-mark" aria-hidden="true">梅</span>
          <span>梅问</span>
        </a>
        <nav className="primary-nav" aria-label="主要导航">
          <a className="nav-link active" href="#">首页</a>
          <a className="nav-link" href="#featured-banks">题库</a>
          <a className="nav-link" href="#learning-plan">学习计划</a>
        </nav>
        <div className="topbar-actions">
          <button className="text-button" type="button">我的收藏</button>
          <button className="avatar-button" type="button" aria-label="打开个人中心">
            <span>林</span>
          </button>
        </div>
      </header>

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
              <span className="eyebrow">今日练习</span>
              <strong>12<span>/ 20</span></strong>
            </div>
            <div className="progress-ring" aria-label="今日练习完成 60%">
              <span>60%</span>
            </div>
          </div>
          <div className="mini-divider" />
          <div className="resume-row">
            <div className="resume-icon" aria-hidden="true">JS</div>
            <div>
              <span>上次练到</span>
              <b>JavaScript 闭包与作用域</b>
            </div>
          </div>
          <button className="primary-button" type="button" data-od-id="resume-learning-button">
            继续练习
            <span aria-hidden="true">→</span>
          </button>
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
            <a href="#all-banks">查看全部 <span aria-hidden="true">→</span></a>
          </div>

          <div className="bank-list" id="all-banks">
            {filteredBanks.map((bank) => (
              <article
                className={`bank-card ${activeBank === bank.id ? "selected" : ""}`}
                key={bank.id}
                data-od-id={`bank-card-${bank.id}`}
              >
                <button
                  className={`bank-visual ${bank.tone}`}
                  type="button"
                  aria-label={`打开${bank.title}`}
                  onClick={() => setActiveBank(bank.id)}
                >
                  <span>{bank.eyebrow.slice(0, 2)}</span>
                  <i aria-hidden="true" />
                </button>
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
                <strong><em>{signedIn ? 13 : 12}</em> 天</strong>
              </div>
              <span className="streak-mark" aria-hidden="true">12</span>
            </div>
            <div className="week-row">
              {activity.map((item, index) => {
                const isToday = index === 5;
                return (
                  <div key={item.day} className={isToday ? "today" : ""}>
                    <span className={item.done || (isToday && signedIn) ? "done" : ""}>
                      {item.done || (isToday && signedIn) ? "✓" : index + 22}
                    </span>
                    <small>{item.day}</small>
                  </div>
                );
              })}
            </div>
            <button
              className={signedIn ? "signin-button complete" : "signin-button"}
              type="button"
              onClick={() => setSignedIn(true)}
              disabled={signedIn}
              data-od-id="daily-signin-button"
            >
              {signedIn ? "今日已签到" : "完成今日签到"}
            </button>
          </section>

          <section className="plan-card" data-od-id="weekly-plan-card">
            <div className="plan-title">
              <div>
                <span className="eyebrow">本周计划</span>
                <h3>稳步完成 80 题</h3>
              </div>
              <span className="plan-count">48 / 80</span>
            </div>
            <div className="plan-bars" aria-label="本周练习趋势">
              {[62, 86, 45, 76, 58, 30, 12].map((height, index) => (
                <div key={index}>
                  <span style={{ height: `${height}%` }} className={index < 5 ? "filled" : ""} />
                  <small>{activity[index].day}</small>
                </div>
              ))}
            </div>
            <div className="plan-summary">
              <span>比上周多完成</span>
              <b>+16 题</b>
            </div>
          </section>

          <a className="vip-card" href="#membership" data-od-id="membership-upgrade-card">
            <span className="vip-label">PRO</span>
            <div>
              <strong>解锁完整题解</strong>
              <span>会员专享解析与进阶题库</span>
            </div>
            <span aria-hidden="true">→</span>
          </a>
        </aside>
      </section>
    </main>
  );
}
