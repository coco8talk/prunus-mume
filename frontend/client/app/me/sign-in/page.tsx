"use client";

import { useMemo, useState } from "react";
import { PersonalShell } from "@/app/components/PersonalShell";

const weekDays = ["一", "二", "三", "四", "五", "六", "日"];

function dateKey(year: number, month: number, day: number) {
  return `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

function mockRecords(year: number, month: number) {
  const days = new Date(year, month + 1, 0).getDate();
  return Array.from({ length: days }, (_, index) => index + 1)
    .filter((day) => day < 25 && (day + month) % 5 !== 0 && (day + year) % 7 !== 0)
    .map((day) => dateKey(year, month, day));
}

export default function SignInPage() {
  const today = new Date();
  const todayKey = dateKey(today.getFullYear(), today.getMonth(), today.getDate());
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth());
  const [records, setRecords] = useState<Record<string, string[]>>(() => {
    const key = `${today.getFullYear()}-${today.getMonth()}`;
    return { [key]: mockRecords(today.getFullYear(), today.getMonth()).filter((item) => item !== todayKey) };
  });
  const [signedToday, setSignedToday] = useState(false);
  const [notice, setNotice] = useState("");

  const monthKey = `${year}-${month}`;
  const monthRecords = useMemo(
    () => records[monthKey] ?? mockRecords(year, month),
    [month, monthKey, records, year],
  );
  const recordSet = new Set(monthRecords);
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;
  const calendarDays = [...Array.from({ length: firstDay }, () => null), ...Array.from({ length: daysInMonth }, (_, index) => index + 1)];
  const isCurrentMonth = year === today.getFullYear() && month === today.getMonth();
  const streak = signedToday ? 13 : 12;

  function changeMonth(delta: number) {
    const next = new Date(year, month + delta, 1);
    setYear(next.getFullYear());
    setMonth(next.getMonth());
    setNotice("");
  }

  function signIn() {
    if (signedToday) return;
    const currentKey = `${today.getFullYear()}-${today.getMonth()}`;
    setRecords((current) => {
      const existing = current[currentKey] ?? mockRecords(today.getFullYear(), today.getMonth());
      return { ...current, [currentKey]: Array.from(new Set([...existing, todayKey])) };
    });
    setSignedToday(true);
    setYear(today.getFullYear());
    setMonth(today.getMonth());
    setNotice("签到成功，连续学习记录已更新。");
  }

  function returnToToday() {
    setYear(today.getFullYear());
    setMonth(today.getMonth());
  }

  return (
    <PersonalShell
      eyebrow="每日签到"
      title="让持续发生，哪怕每天只前进一点。"
      description="记录每一次到访，在稳定的节奏里积累真正的理解。"
    >
      <section data-od-id="daily-sign-in-page">
        {notice && <div className="feedback success" role="status">{notice}</div>}
        <div className="sign-in-stats">
          <article><span>连续签到</span><b>{streak}</b><small>天</small></article>
          <article><span>{month + 1} 月签到</span><b>{monthRecords.length}</b><small>天</small></article>
          <article className={signedToday ? "complete" : ""}>
            <span>今日状态</span><b>{signedToday ? "已签" : "未签"}</b><small>{signedToday ? "明天见" : "等待打卡"}</small>
          </article>
        </div>

        <div className="calendar-card">
          <div className="calendar-heading">
            <div>
              <p className="section-kicker">签到日历</p>
              <h2>{year} 年 {month + 1} 月</h2>
            </div>
            <div className="calendar-controls">
              <button type="button" onClick={() => changeMonth(-1)} aria-label="上个月">←</button>
              {!isCurrentMonth && <button type="button" onClick={returnToToday}>回到本月</button>}
              <button type="button" onClick={() => changeMonth(1)} aria-label="下个月">→</button>
            </div>
          </div>
          <div className="calendar-grid" role="grid" aria-label={`${year} 年 ${month + 1} 月签到日历`}>
            {weekDays.map((day) => <span className="calendar-weekday" role="columnheader" key={day}>{day}</span>)}
            {calendarDays.map((day, index) => day === null ? (
              <span className="calendar-empty" key={`empty-${index}`} />
            ) : (
              <span
                className={[
                  "calendar-day",
                  recordSet.has(dateKey(year, month, day)) ? "signed" : "",
                  isCurrentMonth && day === today.getDate() ? "today" : "",
                ].filter(Boolean).join(" ")}
                role="gridcell"
                aria-label={`${month + 1} 月 ${day} 日${recordSet.has(dateKey(year, month, day)) ? "，已签到" : ""}`}
                key={day}
              >
                <b>{day}</b>
                {recordSet.has(dateKey(year, month, day)) && <small aria-hidden="true">梅</small>}
              </span>
            ))}
          </div>
          <div className="calendar-footer">
            <p>{signedToday ? "今天已经完成签到，保持这个节奏。" : "今天的学习，从一次签到开始。"}</p>
            <button
              className={`form-primary ${signedToday ? "complete" : ""}`}
              type="button"
              onClick={signIn}
              disabled={signedToday}
              data-od-id="sign-in-today-action"
            >
              {signedToday ? "今日已签到" : "立即签到"}
            </button>
          </div>
        </div>
      </section>
    </PersonalShell>
  );
}
