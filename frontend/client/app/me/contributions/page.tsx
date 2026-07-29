"use client";

import { FormEvent, useEffect, useState } from "react";
import { PersonalShell } from "@/app/components/PersonalShell";
import {
  initialContributions,
  type Contribution,
  type ContributionStatus,
} from "@/app/data/personal";
import type { Difficulty } from "@/app/data/mock";

type Draft = Pick<Contribution, "title" | "content" | "answer" | "difficulty" | "recommendVip"> & {
  tags: string;
};

const emptyDraft: Draft = {
  title: "",
  content: "",
  answer: "",
  tags: "",
  difficulty: "中级",
  recommendVip: false,
};

const statusLabels: Record<ContributionStatus, string> = {
  pending: "待审核",
  approved: "已通过",
  rejected: "未通过",
};

export default function ContributionsPage() {
  const [tab, setTab] = useState<"submit" | "list">("submit");
  const [contributions, setContributions] = useState(initialContributions);
  const [draft, setDraft] = useState<Draft>(emptyDraft);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [viewing, setViewing] = useState<Contribution | null>(null);
  const [deleting, setDeleting] = useState<Contribution | null>(null);
  const [errors, setErrors] = useState<Partial<Record<keyof Draft, string>>>({});
  const [notice, setNotice] = useState("");

  useEffect(() => {
    function closeModal(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setViewing(null);
        setDeleting(null);
      }
    }
    window.addEventListener("keydown", closeModal);
    return () => window.removeEventListener("keydown", closeModal);
  }, []);

  function updateDraft<K extends keyof Draft>(field: K, value: Draft[K]) {
    setDraft((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  }

  function validate() {
    const next: typeof errors = {};
    if (draft.title.trim().length < 8) next.title = "标题至少需要 8 个字。";
    if (draft.content.trim().length < 20) next.content = "题目内容至少需要 20 个字。";
    if (draft.answer.trim().length < 20) next.answer = "参考答案至少需要 20 个字。";
    if (!draft.tags.split(/[,，]/).some((item) => item.trim())) next.tags = "请至少填写一个标签。";
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice("");
    if (!validate()) return;
    const normalizedTags = draft.tags.split(/[,，]/).map((item) => item.trim()).filter(Boolean);

    if (editingId) {
      setContributions((current) => current.map((item) => (
        item.id === editingId
          ? { ...item, ...draft, tags: normalizedTags, status: "pending", reviewComment: undefined }
          : item
      )));
      setNotice("修改已保存，并重新进入审核。");
    } else {
      setContributions((current) => [{
        id: `c-${Date.now()}`,
        ...draft,
        tags: normalizedTags,
        status: "pending",
        submittedAt: new Intl.DateTimeFormat("en-CA").format(new Date()),
      }, ...current]);
      setNotice("题目已提交，我们会尽快完成审核。");
    }
    setDraft(emptyDraft);
    setEditingId(null);
    setTab("list");
  }

  function startEdit(item: Contribution) {
    setDraft({
      title: item.title,
      content: item.content,
      answer: item.answer,
      tags: item.tags.join("，"),
      difficulty: item.difficulty,
      recommendVip: item.recommendVip,
    });
    setEditingId(item.id);
    setErrors({});
    setNotice("");
    setTab("submit");
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  function cancelEdit() {
    setDraft(emptyDraft);
    setEditingId(null);
    setErrors({});
  }

  function confirmDelete() {
    if (!deleting) return;
    setContributions((current) => current.filter((item) => item.id !== deleting.id));
    setNotice(`已删除「${deleting.title}」`);
    setDeleting(null);
  }

  return (
    <PersonalShell
      eyebrow="题目贡献"
      title="把你的好问题，分享给更多学习者。"
      description="提交原创题目，并在一个地方查看审核进度与反馈。"
    >
      <section data-od-id="contributions-page">
        <div className="segmented-tabs" role="tablist" aria-label="题目贡献">
          <button
            role="tab"
            aria-selected={tab === "submit"}
            className={tab === "submit" ? "active" : ""}
            onClick={() => setTab("submit")}
            type="button"
          >
            {editingId ? "编辑题目" : "提交新题"}
          </button>
          <button
            role="tab"
            aria-selected={tab === "list"}
            className={tab === "list" ? "active" : ""}
            onClick={() => setTab("list")}
            type="button"
          >
            我的贡献 <span>{contributions.length}</span>
          </button>
        </div>

        {notice && <div className="feedback success" role="status">{notice}</div>}

        {tab === "submit" ? (
          <form className="contribution-form" onSubmit={handleSubmit} noValidate data-od-id="contribution-form">
            <div className="form-intro">
              <div><p className="section-kicker">{editingId ? "编辑贡献" : "新题目"}</p><h2>{editingId ? "完善题目后重新提交" : "从一个清晰的问题开始"}</h2></div>
              <span>所有字段均为必填</span>
            </div>
            <label className="field full">
              <span>题目标题</span>
              <input
                value={draft.title}
                onChange={(event) => updateDraft("title", event.target.value)}
                aria-invalid={Boolean(errors.title)}
                placeholder="用一句话说明要回答的问题"
              />
              {errors.title && <small className="field-error">{errors.title}</small>}
            </label>
            <label className="field full">
              <span>题目内容</span>
              <textarea
                value={draft.content}
                onChange={(event) => updateDraft("content", event.target.value)}
                aria-invalid={Boolean(errors.content)}
                placeholder="补充背景、范围和期望的回答角度"
              />
              {errors.content && <small className="field-error">{errors.content}</small>}
            </label>
            <label className="field full">
              <span>参考答案</span>
              <textarea
                value={draft.answer}
                onChange={(event) => updateDraft("answer", event.target.value)}
                aria-invalid={Boolean(errors.answer)}
                placeholder="给出准确、完整、可验证的参考答案"
              />
              {errors.answer && <small className="field-error">{errors.answer}</small>}
            </label>
            <label className="field">
              <span>标签</span>
              <input
                value={draft.tags}
                onChange={(event) => updateDraft("tags", event.target.value)}
                aria-invalid={Boolean(errors.tags)}
                placeholder="React，架构，性能"
              />
              {errors.tags && <small className="field-error">{errors.tags}</small>}
            </label>
            <label className="field">
              <span>难度</span>
              <select
                value={draft.difficulty}
                onChange={(event) => updateDraft("difficulty", event.target.value as Difficulty)}
              >
                {["入门", "中级", "进阶"].map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
            <label className="recommend-check full">
              <input
                type="checkbox"
                checked={draft.recommendVip}
                onChange={(event) => updateDraft("recommendVip", event.target.checked)}
              />
              <span><b>建议设为 VIP 专享</b><small>最终访问权限将由审核人员根据题目深度决定。</small></span>
            </label>
            <div className="form-actions full">
              {editingId && <button className="secondary-button" type="button" onClick={cancelEdit}>取消编辑</button>}
              <button className="form-primary" type="submit">{editingId ? "保存并重新提交" : "提交审核"}</button>
            </div>
          </form>
        ) : contributions.length ? (
          <div className="contribution-list">
            {contributions.map((item) => (
              <article className="contribution-card" key={item.id} data-od-id={`contribution-${item.id}`}>
                <div className="contribution-card-top">
                  <div className="tag-row">
                    <span className={`status-badge ${item.status}`}>{statusLabels[item.status]}</span>
                    <span className={`difficulty ${item.difficulty}`}>{item.difficulty}</span>
                    {item.recommendVip && <span className="vip-pill">建议 VIP</span>}
                  </div>
                  <time dateTime={item.submittedAt}>{item.submittedAt}</time>
                </div>
                <h2>{item.title}</h2>
                <p>{item.content}</p>
                {item.reviewComment && (
                  <div className="review-comment" role="note"><b>审核意见</b><span>{item.reviewComment}</span></div>
                )}
                <div className="card-actions">
                  <button type="button" onClick={() => setViewing(item)}>查看</button>
                  <button type="button" onClick={() => startEdit(item)}>编辑</button>
                  <button className="danger-text" type="button" onClick={() => setDeleting(item)}>删除</button>
                </div>
              </article>
            ))}
          </div>
        ) : (
          <div className="state-panel empty" role="status">
            <span aria-hidden="true">稿</span><h2>还没有贡献记录</h2>
            <p>提交你的第一道原创题目吧。</p>
            <button type="button" onClick={() => setTab("submit")}>提交新题</button>
          </div>
        )}
      </section>

      {viewing && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setViewing(null)}>
          <div className="modal-card detail-modal" role="dialog" aria-modal="true" aria-labelledby="view-title" onMouseDown={(event) => event.stopPropagation()}>
            <button className="modal-close" type="button" onClick={() => setViewing(null)} aria-label="关闭">×</button>
            <p className="section-kicker">贡献详情</p>
            <h2 id="view-title">{viewing.title}</h2>
            <h3>题目内容</h3><p>{viewing.content}</p>
            <h3>参考答案</h3><p>{viewing.answer}</p>
            <div className="tag-row">{viewing.tags.map((item) => <span key={item}>{item}</span>)}</div>
          </div>
        </div>
      )}
      {deleting && (
        <div className="modal-backdrop" role="presentation" onMouseDown={() => setDeleting(null)}>
          <div className="modal-card confirm-modal" role="alertdialog" aria-modal="true" aria-labelledby="delete-title" aria-describedby="delete-description" onMouseDown={(event) => event.stopPropagation()}>
            <span className="warning-mark" aria-hidden="true">!</span>
            <h2 id="delete-title">确认删除这条贡献？</h2>
            <p id="delete-description">「{deleting.title}」删除后无法恢复。</p>
            <div className="form-actions">
              <button className="secondary-button" type="button" onClick={() => setDeleting(null)}>取消</button>
              <button className="danger-button" type="button" onClick={confirmDelete}>确认删除</button>
            </div>
          </div>
        </div>
      )}
    </PersonalShell>
  );
}
