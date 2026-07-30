"use client";

import { useEffect, useMemo, useState } from "react";
import { PersonalShell } from "@/app/components/PersonalShell";
import { ProtectedContent, useAuth } from "@/app/components/AuthProvider";
import { errorMessage } from "@/app/lib/api";
import { signInService } from "@/app/lib/services";

const weekDays = ["一", "二", "三", "四", "五", "六", "日"];

export default function SignInPage() {
  const { user, status: authStatus } = useAuth();
  const today = useMemo(() => new Date(), []);
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth());
  const [records, setRecords] = useState<number[]>([]);
  const [signedToday, setSignedToday] = useState(false);
  const [streak, setStreak] = useState(0);
  const [currentMonthCount, setCurrentMonthCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [signing, setSigning] = useState(false);
  const [notice, setNotice] = useState("");
  const [noticeType, setNoticeType] = useState<"success" | "error">("success");
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null);

  const isCurrentMonth = year === today.getFullYear() && month === today.getMonth();
  const summaryReady = loadedUserId === user?.id;

  useEffect(() => {
    if (authStatus !== "authenticated") return;
    const controller = new AbortController();

    const monthRequest = signInService.records(year, month + 1, controller.signal);
    const summaryRequest = isCurrentMonth
      ? Promise.all([
          signInService.today(controller.signal),
          signInService.streak(controller.signal),
          signInService.currentMonthCount(controller.signal),
        ])
      : Promise.resolve(null);

    Promise.all([monthRequest, summaryRequest]).then(([days, summary]) => {
      setRecords(days);
      if (summary) {
        setSignedToday(summary[0]);
        setStreak(summary[1]);
        setCurrentMonthCount(summary[2]);
      }
      setLoadedUserId(user?.id ?? null);
      setLoading(false);
    }).catch((error) => {
      if (controller.signal.aborted) return;
      setNotice(errorMessage(error));
      setNoticeType("error");
      setLoadedUserId(user?.id ?? null);
      setLoading(false);
    });
    return () => controller.abort();
  }, [authStatus, isCurrentMonth, month, user?.id, year]);

  const recordSet = new Set(records);
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;
  const calendarDays = [
    ...Array.from({ length: firstDay }, () => null),
    ...Array.from({ length: daysInMonth }, (_, index) => index + 1),
  ];

  function changeMonth(delta: number) {
    const next = new Date(year, month + delta, 1);
    setYear(next.getFullYear());
    setMonth(next.getMonth());
  }

  async function signIn() {
    if (signedToday || signing) return;
    setSigning(true);
    setNotice("");
    try {
      await signInService.add();
      const [days, nextStreak, count] = await Promise.all([
        signInService.records(today.getFullYear(), today.getMonth() + 1),
        signInService.streak(),
        signInService.currentMonthCount(),
      ]);
      setYear(today.getFullYear());
      setMonth(today.getMonth());
      setRecords(days);
      setStreak(nextStreak);
      setCurrentMonthCount(count);
      setSignedToday(true);
      setNotice("签到成功，连续学习记录已更新。");
      setNoticeType("success");
    } catch (error) {
      setNotice(errorMessage(error));
      setNoticeType("error");
    } finally {
      setSigning(false);
    }
  }

  function returnToToday() {
    setYear(today.getFullYear());
    setMonth(today.getMonth());
  }

  return (
    <ProtectedContent>
      <PersonalShell
        eyebrow="每日签到"
        title="让持续发生，哪怕每天只前进一点。"
        description="记录每一次到访，在稳定的节奏里积累真正的理解。"
      >
        <section data-od-id="daily-sign-in-page">
          {notice && <div className={`feedback ${noticeType}`} role={noticeType === "error" ? "alert" : "status"}>{notice}</div>}
          <div className="sign-in-stats">
            <article><span>连续签到</span><b>{summaryReady ? streak : 0}</b><small>天</small></article>
            <article><span>{month + 1} 月签到</span><b>{summaryReady ? isCurrentMonth ? currentMonthCount : records.length : 0}</b><small>天</small></article>
            <article className={summaryReady && signedToday ? "complete" : ""}>
              <span>今日状态</span><b>{summaryReady && signedToday ? "已签" : "未签"}</b><small>{summaryReady && signedToday ? "明天见" : "等待打卡"}</small>
            </article>
          </div>

          <div className="calendar-card">
            <div className="calendar-heading">
              <div><p className="section-kicker">签到日历</p><h2>{year} 年 {month + 1} 月</h2></div>
              <div className="calendar-controls">
                <button type="button" onClick={() => changeMonth(-1)} aria-label="上个月">←</button>
                {!isCurrentMonth && <button type="button" onClick={returnToToday}>回到本月</button>}
                <button type="button" onClick={() => changeMonth(1)} aria-label="下个月">→</button>
              </div>
            </div>
            {loading || loadedUserId !== user?.id ? (
              <div className="state-panel" role="status"><p>正在加载签到记录…</p></div>
            ) : (
              <div className="calendar-grid" role="grid" aria-label={`${year} 年 ${month + 1} 月签到日历`}>
                {weekDays.map((day) => <span className="calendar-weekday" role="columnheader" key={day}>{day}</span>)}
                {calendarDays.map((day, index) => day === null ? (
                  <span className="calendar-empty" key={`empty-${index}`} />
                ) : (
                  <span
                    className={[
                      "calendar-day",
                      recordSet.has(day) ? "signed" : "",
                      isCurrentMonth && day === today.getDate() ? "today" : "",
                    ].filter(Boolean).join(" ")}
                    role="gridcell"
                    aria-label={`${month + 1} 月 ${day} 日${recordSet.has(day) ? "，已签到" : ""}`}
                    key={day}
                  >
                    <b>{day}</b>
                    {recordSet.has(day) && <small aria-hidden="true">梅</small>}
                  </span>
                ))}
              </div>
            )}
            <div className="calendar-footer">
              <p>{summaryReady && signedToday ? "今天已经完成签到，保持这个节奏。" : "今天的学习，从一次签到开始。"}</p>
              <button
                className={`form-primary ${summaryReady && signedToday ? "complete" : ""}`}
                type="button"
                onClick={signIn}
                disabled={!summaryReady || signedToday || signing}
                data-od-id="sign-in-today-action"
              >
                {summaryReady && signedToday ? "今日已签到" : signing ? "正在签到…" : "立即签到"}
              </button>
            </div>
          </div>
        </section>
      </PersonalShell>
    </ProtectedContent>
  );
}
