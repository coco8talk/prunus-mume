"use client";

import { FormEvent, useEffect, useState } from "react";
import { PersonalShell } from "@/app/components/PersonalShell";
import { ProtectedContent, useAuth } from "@/app/components/AuthProvider";
import type { Difficulty } from "@/app/data/models";
import { errorMessage } from "@/app/lib/api";
import { difficultyRequest, questionService } from "@/app/lib/services";

type Contribution = {
  id: string;
  title: string;
  content: string;
  answer: string;
  tags: string[];
  difficulty: Difficulty;
  status: "pending";
  submittedAt: string;
};

type Draft = Pick<Contribution, "title" | "content" | "answer" | "difficulty"> & {
  tags: string;
};

const emptyDraft: Draft = {
  title: "",
  content: "",
  answer: "",
  tags: "",
  difficulty: "中级",
};

export default function ContributionsPage() {
  const { user } = useAuth();
  const [tab, setTab] = useState<"submit" | "list">("submit");
  const [contributions, setContributions] = useState<Contribution[]>([]);
  const [draft, setDraft] = useState<Draft>(emptyDraft);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [viewing, setViewing] = useState<Contribution | null>(null);
  const [deleting, setDeleting] = useState<Contribution | null>(null);
  const [errors, setErrors] = useState<Partial<Record<keyof Draft, string>>>({});
  const [notice, setNotice] = useState("");
  const [noticeType, setNoticeType] = useState<"success" | "error">("success");
  const [submitting, setSubmitting] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [ownerUserId, setOwnerUserId] = useState<string | null>(null);

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

  useEffect(() => {
    if (!user || ownerUserId === user.id) return;
    void Promise.resolve().then(() => {
      setContributions([]);
      setDraft(emptyDraft);
      setEditingId(null);
      setViewing(null);
      setDeleting(null);
      setNotice("");
      setTab("submit");
      setOwnerUserId(user.id);
    });
  }, [ownerUserId, user]);

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

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice("");
    if (!validate()) return;
    const normalizedTags = draft.tags.split(/[,，]/).map((item) => item.trim()).filter(Boolean);
    setSubmitting(true);

    try {
      const request = {
        title: draft.title.trim(),
        content: draft.content.trim(),
        answer: draft.answer.trim(),
        tags: normalizedTags,
        difficulty: difficultyRequest(draft.difficulty),
      };
      if (editingId) {
        await questionService.update({ ...request, id: editingId });
        setContributions((current) => current.map((item) => (
          item.id === editingId
            ? { ...item, ...draft, tags: normalizedTags, status: "pending" }
            : item
        )));
        setNotice("修改已保存，并重新进入审核。");
      } else {
        const id = await questionService.create(request);
        setContributions((current) => [{
          id: String(id),
          ...draft,
          tags: normalizedTags,
          status: "pending",
          submittedAt: new Intl.DateTimeFormat("en-CA").format(new Date()),
        }, ...current]);
        setNotice("题目已提交，我们会尽快完成审核。");
      }
      setNoticeType("success");
      setDraft(emptyDraft);
      setEditingId(null);
      setTab("list");
    } catch (error) {
      setNotice(errorMessage(error));
      setNoticeType("error");
    } finally {
      setSubmitting(false);
    }
  }

  function startEdit(item: Contribution) {
    setDraft({
      title: item.title,
      content: item.content,
      answer: item.answer,
      tags: item.tags.join("，"),
      difficulty: item.difficulty,
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

  async function confirmDelete() {
    if (!deleting) return;
    setDeletingId(deleting.id);
    setNotice("");
    try {
      await questionService.remove(deleting.id);
      setContributions((current) => current.filter((item) => item.id !== deleting.id));
      setNotice(`已删除「${deleting.title}」`);
      setNoticeType("success");
      setDeleting(null);
    } catch (error) {
      setNotice(errorMessage(error));
      setNoticeType("error");
    } finally {
      setDeletingId(null);
    }
  }

  if (user && ownerUserId !== user.id) {
    return (
      <ProtectedContent>
        <PersonalShell
          eyebrow="题目贡献"
          title="把你的好问题，分享给更多学习者。"
          description="提交原创题目，并在一个地方查看审核进度与反馈。"
        >
          <section className="state-panel" role="status"><h1>正在准备贡献记录…</h1></section>
        </PersonalShell>
      </ProtectedContent>
    );
  }

  return (
    <ProtectedContent><PersonalShell
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

        {notice && <div className={`feedback ${noticeType}`} role={noticeType === "error" ? "alert" : "status"}>{notice}</div>}

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
            <div className="form-actions full">
              {editingId && <button className="secondary-button" type="button" onClick={cancelEdit}>取消编辑</button>}
              <button className="form-primary" type="submit" disabled={submitting}>
                {submitting ? "正在提交…" : editingId ? "保存并重新提交" : "提交审核"}
              </button>
            </div>
          </form>
        ) : contributions.length ? (
          <div className="contribution-list">
            {contributions.map((item) => (
              <article className="contribution-card" key={item.id} data-od-id={`contribution-${item.id}`}>
                <div className="contribution-card-top">
                  <div className="tag-row">
                    <span className={`status-badge ${item.status}`}>待审核</span>
                    <span className={`difficulty ${item.difficulty}`}>{item.difficulty}</span>
                  </div>
                  <time dateTime={item.submittedAt}>{item.submittedAt}</time>
                </div>
                <h2>{item.title}</h2>
                <p>{item.content}</p>
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
            <span aria-hidden="true">稿</span><h2>本次会话还没有贡献记录</h2>
            <p>当前接口规范没有提供历史贡献列表；这里会显示你本次成功提交的题目。</p>
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
              <button className="danger-button" type="button" onClick={confirmDelete} disabled={deletingId === deleting.id}>
                {deletingId === deleting.id ? "正在删除…" : "确认删除"}
              </button>
            </div>
          </div>
        </div>
      )}
    </PersonalShell></ProtectedContent>
  );
}
