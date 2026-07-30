"use client";

import { ChangeEvent, FormEvent, useEffect, useState } from "react";
import Image from "next/image";
import { PersonalShell } from "@/app/components/PersonalShell";
import { ProtectedContent, useAuth } from "@/app/components/AuthProvider";
import { errorMessage } from "@/app/lib/api";
import {
  userService,
  type ApiUser,
  type AvatarCredentials,
  type ProfileUpdate,
} from "@/app/lib/services";

type EditableProfile = {
  displayName: string;
  bio: string;
  phoneNumber: string;
  email: string;
  grade: string;
  workExperience: string;
  expertiseDirection: string;
};

const emptyProfile: EditableProfile = {
  displayName: "",
  bio: "",
  phoneNumber: "",
  email: "",
  grade: "1",
  workExperience: "",
  expertiseDirection: "",
};

function editableProfile(user: ApiUser): EditableProfile {
  return {
    displayName: user.userName ?? user.userAccount ?? "",
    bio: user.userProfile ?? "",
    phoneNumber: user.phoneNumber ?? "",
    email: user.email ?? "",
    grade: user.grade ? String(user.grade) : "1",
    workExperience: user.workExperience ?? "",
    expertiseDirection: user.expertiseDirection ?? "",
  };
}

function uploadDirectly(
  credentials: AvatarCredentials,
  file: File,
  onProgress: (progress: number) => void,
) {
  const url = credentials.uploadUrl ?? credentials.url;
  if (!url) return Promise.reject(new Error("上传凭证中缺少上传地址，请联系后端确认凭证字段"));

  return new Promise<void>((resolve, reject) => {
    const request = new XMLHttpRequest();
    request.open(credentials.method ?? (credentials.fields ? "POST" : "PUT"), url);
    Object.entries(credentials.headers ?? {}).forEach(([name, value]) => request.setRequestHeader(name, value));
    request.upload.onprogress = (event) => {
      if (event.lengthComputable) onProgress(Math.round((event.loaded / event.total) * 100));
    };
    request.onload = () => {
      if (request.status >= 200 && request.status < 300) resolve();
      else reject(new Error(`头像上传失败（${request.status}）`));
    };
    request.onerror = () => reject(new Error("头像上传失败，请检查网络后重试"));

    if (credentials.fields) {
      const form = new FormData();
      Object.entries(credentials.fields).forEach(([name, value]) => form.append(name, value));
      form.append(credentials.fileField ?? "file", file);
      request.send(form);
    } else {
      request.send(file);
    }
  });
}

export default function MyProfilePage() {
  const { user, setUser } = useAuth();
  const [profile, setProfile] = useState(emptyProfile);
  const [draft, setDraft] = useState(emptyProfile);
  const [editing, setEditing] = useState(false);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [confirmedAvatar, setConfirmedAvatar] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadState, setUploadState] = useState<"idle" | "uploading" | "failed" | "success">("idle");
  const [errors, setErrors] = useState<Partial<Record<keyof EditableProfile, string>>>({});
  const [notice, setNotice] = useState("");
  const [noticeType, setNoticeType] = useState<"success" | "error">("success");
  const [saving, setSaving] = useState(false);
  const [passwords, setPasswords] = useState({ next: "", confirm: "" });
  const [passwordError, setPasswordError] = useState("");
  const [passwordNotice, setPasswordNotice] = useState("");
  const [changingPassword, setChangingPassword] = useState(false);
  const [profileUserId, setProfileUserId] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return;
    const next = editableProfile(user);
    void Promise.resolve().then(() => {
      setProfile(next);
      setDraft(next);
      setConfirmedAvatar(user.userAvatar ?? null);
      setProfileUserId(user.id);
    });
  }, [user]);

  useEffect(() => {
    return () => {
      if (avatarPreview?.startsWith("blob:")) URL.revokeObjectURL(avatarPreview);
    };
  }, [avatarPreview]);

  function updateField(field: keyof EditableProfile, value: string) {
    setDraft((current) => ({ ...current, [field]: value }));
    setErrors((current) => ({ ...current, [field]: undefined }));
  }

  function previewAvatar(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/") || file.size > 3 * 1024 * 1024) {
      setNotice("请选择 3MB 以内的图片文件。");
      setNoticeType("error");
      return;
    }
    setAvatarFile(file);
    setAvatarPreview(URL.createObjectURL(file));
    setUploadProgress(0);
    setUploadState("idle");
    setNotice("头像预览已更新，保存资料时会上传。");
    setNoticeType("success");
  }

  function validateProfile() {
    const next: typeof errors = {};
    if (draft.displayName.trim().length < 2) next.displayName = "昵称至少需要 2 个字。";
    if (draft.bio.trim().length > 120) next.bio = "个人简介不能超过 120 个字。";
    if (draft.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(draft.email)) next.email = "请输入有效的邮箱地址。";
    if (draft.phoneNumber && !/^\d{11}$/.test(draft.phoneNumber)) next.phoneNumber = "请输入 11 位手机号码。";
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function uploadAvatar() {
    if (!avatarFile) return confirmedAvatar ?? undefined;
    setUploadState("uploading");
    setUploadProgress(0);
    try {
      const credentials = await userService.avatarCredentials(avatarFile.name);
      await uploadDirectly(credentials, avatarFile, setUploadProgress);
      const finalUrl = await userService.confirmAvatarUpload();
      setConfirmedAvatar(finalUrl);
      setAvatarPreview(finalUrl);
      setAvatarFile(null);
      setUploadProgress(100);
      setUploadState("success");
      return finalUrl;
    } catch (error) {
      setUploadState("failed");
      throw error;
    }
  }

  async function saveProfile(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setNotice("");
    if (!validateProfile() || saving) return;
    setSaving(true);
    try {
      const avatar = await uploadAvatar();
      const update: ProfileUpdate = {
        userName: draft.displayName.trim(),
        userAvatar: avatar,
        userProfile: draft.bio.trim(),
        phoneNumber: draft.phoneNumber || undefined,
        email: draft.email || undefined,
        grade: Number(draft.grade),
        workExperience: draft.workExperience || undefined,
        expertiseDirection: draft.expertiseDirection || undefined,
      };
      await userService.updateMe(update);
      setProfile(draft);
      if (user) setUser({ ...user, ...update });
      setEditing(false);
      setNotice("个人资料已保存。");
      setNoticeType("success");
    } catch (error) {
      setNotice(errorMessage(error));
      setNoticeType("error");
    } finally {
      setSaving(false);
    }
  }

  function cancelEdit() {
    setDraft(profile);
    setAvatarFile(null);
    setAvatarPreview(null);
    setUploadProgress(0);
    setUploadState("idle");
    setEditing(false);
    setErrors({});
    setNotice("");
  }

  async function changePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPasswordError("");
    setPasswordNotice("");
    if (passwords.next.length < 8 || passwords.next.length > 20) {
      setPasswordError("新密码需要 8–20 位。");
      return;
    }
    if (passwords.next !== passwords.confirm) {
      setPasswordError("两次输入的新密码不一致。");
      return;
    }
    setChangingPassword(true);
    try {
      await userService.changePassword(passwords.next, passwords.confirm);
      setPasswords({ next: "", confirm: "" });
      setPasswordNotice("密码修改成功，请在下次登录时使用新密码。");
    } catch (error) {
      setPasswordError(errorMessage(error));
    } finally {
      setChangingPassword(false);
    }
  }

  const avatar = avatarPreview ?? confirmedAvatar;
  const displayName = profile.displayName || user?.userAccount || "梅问用户";
  const profileReady = Boolean(user && profileUserId === user.id);

  return (
    <ProtectedContent>
      <PersonalShell
        eyebrow="个人资料"
        title="让大家认识真实而专注的你。"
        description="管理公开资料与账号联系方式，隐私信息不会出现在公开主页。"
      >
        {!profileReady ? (
          <section className="state-panel" role="status"><h1>正在加载个人资料…</h1></section>
        ) : <section data-od-id="my-profile-page">
          {notice && <div className={`feedback ${noticeType}`} role={noticeType === "error" ? "alert" : "status"}>{notice}</div>}
          <form className="profile-editor" onSubmit={saveProfile} noValidate>
            <div className="profile-editor-heading">
              <div className="avatar-editor">
                {avatar
                  ? <Image src={avatar} alt="头像预览" width={68} height={68} unoptimized />
                  : <span aria-hidden="true">{displayName.slice(0, 1)}</span>}
                {editing && (
                  <label>
                    更换头像
                    <input type="file" accept="image/png,image/jpeg,image/webp" onChange={previewAvatar} />
                  </label>
                )}
              </div>
              <div>
                <p className="section-kicker">基本资料</p>
                <h2>{displayName}</h2>
                <span>{user?.userRole === 2 ? "梅问会员" : "梅问用户"}</span>
              </div>
              {!editing && <button className="secondary-button" type="button" onClick={() => { setEditing(true); setNotice(""); }}>编辑资料</button>}
            </div>

            {editing && uploadState !== "idle" && (
              <div className={`feedback ${uploadState === "failed" ? "error" : "success"}`} role="status">
                {uploadState === "uploading" && `头像上传中：${uploadProgress}%`}
                {uploadState === "failed" && "头像上传失败。保留当前文件后再次保存即可重试。"}
                {uploadState === "success" && "头像上传并确认成功。"}
              </div>
            )}

            <div className="profile-fields">
              <label className="field">
                <span>显示名称</span>
                <input value={draft.displayName} onChange={(event) => updateField("displayName", event.target.value)} disabled={!editing} aria-invalid={Boolean(errors.displayName)} />
                {errors.displayName && <small className="field-error">{errors.displayName}</small>}
              </label>
              <label className="field">
                <span>学历等级</span>
                <select value={draft.grade} onChange={(event) => updateField("grade", event.target.value)} disabled={!editing}>
                  {Array.from({ length: 9 }, (_, index) => String(index + 1)).map((item) => <option key={item} value={item}>等级 {item}</option>)}
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
                <button className="secondary-button" type="button" onClick={cancelEdit} disabled={saving}>取消</button>
                <button className="form-primary" type="submit" disabled={saving}>
                  {saving ? uploadState === "uploading" ? `上传头像 ${uploadProgress}%` : "正在保存…" : uploadState === "failed" ? "重试并保存" : "保存资料"}
                </button>
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
              <label className="field"><span>新密码</span><input type="password" autoComplete="new-password" value={passwords.next} onChange={(event) => setPasswords({ ...passwords, next: event.target.value })} /></label>
              <label className="field"><span>确认新密码</span><input type="password" autoComplete="new-password" value={passwords.confirm} onChange={(event) => setPasswords({ ...passwords, confirm: event.target.value })} /></label>
            </div>
            <button className="secondary-button" type="submit" disabled={changingPassword}>
              {changingPassword ? "正在更新…" : "更新密码"}
            </button>
          </form>
        </section>}
      </PersonalShell>
    </ProtectedContent>
  );
}
