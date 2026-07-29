"use client";

import { ChangeEvent, FormEvent, useState } from "react";
import Image from "next/image";
import { PersonalShell } from "@/app/components/PersonalShell";
import { initialProfile, type EditableProfile } from "@/app/data/personal";

export default function MyProfilePage() {
  const [profile, setProfile] = useState(initialProfile);
  const [draft, setDraft] = useState(initialProfile);
  const [editing, setEditing] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [savedAvatar, setSavedAvatar] = useState<string | null>(null);
  const [errors, setErrors] = useState<Partial<Record<keyof EditableProfile, string>>>({});
  const [notice, setNotice] = useState("");
  const [passwords, setPasswords] = useState({ current: "", next: "", confirm: "" });
  const [passwordError, setPasswordError] = useState("");
  const [passwordNotice, setPasswordNotice] = useState("");

  function updateField(field: keyof EditableProfile, value: string) {
    setDraft((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  }

  function previewAvatar(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/") || file.size > 3 * 1024 * 1024) {
      setNotice("请选择 3MB 以内的图片文件。");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      setAvatarPreview(String(reader.result));
      setNotice("头像预览已更新，保存资料后生效。");
    };
    reader.readAsDataURL(file);
  }

  function validateProfile() {
    const next: typeof errors = {};
    if (draft.displayName.trim().length < 2) next.displayName = "昵称至少需要 2 个字。";
    if (draft.bio.trim().length > 120) next.bio = "个人简介不能超过 120 个字。";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(draft.email)) next.email = "请输入有效的邮箱地址。";
    if (!/^[\d\s+*()-]{7,20}$/.test(draft.phoneNumber)) next.phoneNumber = "请输入有效的手机号码。";
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice("");
    if (!validateProfile()) return;
    setProfile(draft);
    if (avatarPreview) setSavedAvatar(avatarPreview);
    setEditing(false);
    setNotice("个人资料已保存。");
  }

  function cancelEdit() {
    setDraft(profile);
    setAvatarPreview(savedAvatar);
    setEditing(false);
    setErrors({});
    setNotice("");
  }

  function changePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPasswordError("");
    setPasswordNotice("");
    if (!passwords.current) {
      setPasswordError("请输入当前密码。");
    } else if (passwords.next.length < 8) {
      setPasswordError("新密码至少需要 8 位。");
    } else if (passwords.next !== passwords.confirm) {
      setPasswordError("两次输入的新密码不一致。");
    } else {
      setPasswords({ current: "", next: "", confirm: "" });
      setPasswordNotice("密码修改成功，请在下次登录时使用新密码。");
    }
  }

  const avatar = avatarPreview ?? savedAvatar;

  return (
    <PersonalShell
      eyebrow="个人资料"
      title="让大家认识真实而专注的你。"
      description="管理公开资料与账号联系方式，隐私信息不会出现在公开主页。"
    >
      <section data-od-id="my-profile-page">
        {notice && <div className={`feedback ${notice.includes("保存") ? "success" : "error"}`} role="status">{notice}</div>}
        <form className="profile-editor" onSubmit={saveProfile} noValidate>
          <div className="profile-editor-heading">
            <div className="avatar-editor">
              {avatar ? <Image src={avatar} alt="头像预览" width={68} height={68} unoptimized /> : <span aria-hidden="true">林</span>}
              {editing && (
                <label>
                  更换头像
                  <input type="file" accept="image/png,image/jpeg,image/webp" onChange={previewAvatar} />
                </label>
              )}
            </div>
            <div><p className="section-kicker">基本资料</p><h2>{profile.displayName}</h2><span>认证贡献者 · 加入梅问 2 年</span></div>
            {!editing && <button className="secondary-button" type="button" onClick={() => { setEditing(true); setNotice(""); }}>编辑资料</button>}
          </div>

          <div className="profile-fields">
            <label className="field">
              <span>显示名称</span>
              <input value={draft.displayName} onChange={(event) => updateField("displayName", event.target.value)} disabled={!editing} aria-invalid={Boolean(errors.displayName)} />
              {errors.displayName && <small className="field-error">{errors.displayName}</small>}
            </label>
            <label className="field">
              <span>学历</span>
              <select value={draft.grade} onChange={(event) => updateField("grade", event.target.value)} disabled={!editing}>
                {["高中", "专科", "本科", "硕士", "博士"].map((item) => <option key={item}>{item}</option>)}
              </select>
            </label>
            <label className="field full">
              <span>个人简介</span>
              <textarea value={draft.bio} onChange={(event) => updateField("bio", event.target.value)} disabled={!editing} aria-invalid={Boolean(errors.bio)} />
              <small className={errors.bio ? "field-error" : "field-hint"}>{errors.bio ?? `${draft.bio.length} / 120`}</small>
            </label>
            <label className="field">
              <span>手机号码 <i>仅自己可见</i></span>
              <input value={draft.phoneNumber} onChange={(event) => updateField("phoneNumber", event.target.value)} disabled={!editing} aria-invalid={Boolean(errors.phoneNumber)} />
              {errors.phoneNumber && <small className="field-error">{errors.phoneNumber}</small>}
            </label>
            <label className="field">
              <span>邮箱 <i>仅自己可见</i></span>
              <input type="email" value={draft.email} onChange={(event) => updateField("email", event.target.value)} disabled={!editing} aria-invalid={Boolean(errors.email)} />
              {errors.email && <small className="field-error">{errors.email}</small>}
            </label>
            <label className="field full">
              <span>工作经历</span>
              <textarea value={draft.workExperience} onChange={(event) => updateField("workExperience", event.target.value)} disabled={!editing} />
            </label>
            <label className="field full">
              <span>擅长方向</span>
              <input value={draft.expertiseDirection} onChange={(event) => updateField("expertiseDirection", event.target.value)} disabled={!editing} />
            </label>
          </div>
          {editing && (
            <div className="form-actions profile-save-actions">
              <button className="secondary-button" type="button" onClick={cancelEdit}>取消</button>
              <button className="form-primary" type="submit">保存资料</button>
            </div>
          )}
        </form>

        <form className="password-section" onSubmit={changePassword} noValidate data-od-id="change-password-form">
          <div className="form-intro">
            <div><p className="section-kicker">账号安全</p><h2>修改密码</h2></div>
            <span>建议定期更新密码</span>
          </div>
          {passwordNotice && <div className="feedback success" role="status">{passwordNotice}</div>}
          {passwordError && <div className="feedback error" role="alert">{passwordError}</div>}
          <div className="password-fields">
            <label className="field"><span>当前密码</span><input type="password" autoComplete="current-password" value={passwords.current} onChange={(event) => setPasswords({ ...passwords, current: event.target.value })} /></label>
            <label className="field"><span>新密码</span><input type="password" autoComplete="new-password" value={passwords.next} onChange={(event) => setPasswords({ ...passwords, next: event.target.value })} /></label>
            <label className="field"><span>确认新密码</span><input type="password" autoComplete="new-password" value={passwords.confirm} onChange={(event) => setPasswords({ ...passwords, confirm: event.target.value })} /></label>
          </div>
          <button className="secondary-button" type="submit">更新密码</button>
        </form>
      </section>
    </PersonalShell>
  );
}
