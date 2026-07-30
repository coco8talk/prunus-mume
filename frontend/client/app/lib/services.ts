import { apiRequest, normalizePage, type PageResult } from "@/app/lib/api";
import type { Difficulty, Question, QuestionBank } from "@/app/data/models";

export type ApiUser = {
  id: string;
  userAccount?: string;
  userName?: string;
  userAvatar?: string;
  userRole: number;
  userProfile?: string;
  phoneNumber?: string;
  email?: string;
  grade?: number;
  workExperience?: string;
  expertiseDirection?: string;
  createTime?: string;
  updateTime?: string;
  vipExpireTime?: string;
};

type ApiQuestion = {
  id: string;
  title: string;
  content?: string;
  answer?: string;
  tags?: string[];
  difficulty?: number;
  viewNum?: number;
  thumbNum?: number;
  favourNum?: number;
  needVip?: number | boolean;
};

type ApiQuestionBank = {
  id: string;
  title: string;
  picture?: string;
  userId?: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
  userVO?: ApiUser;
  questionPageVO?: PageResult<ApiQuestion>;
};

export type ProfileUpdate = {
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  phoneNumber?: string;
  email?: string;
  grade?: number;
  workExperience?: string;
  expertiseDirection?: string;
};

export type QuestionDraftRequest = {
  id?: string;
  title: string;
  content: string;
  answer: string;
  tags: string[];
  difficulty: number;
  questionBankId?: number;
};

export type MembershipOrder = {
  outTradeNo: string;
  status: "PENDING" | "PAID" | string;
  amount: number;
  currency: string;
  paymentMethod: string;
  createTime: string;
};

export type AvatarCredentials = Record<string, unknown> & {
  uploadUrl?: string;
  url?: string;
  method?: string;
  headers?: Record<string, string>;
  fields?: Record<string, string>;
  fileField?: string;
};

const tones = ["blue", "coral", "green", "plum"] as const;

function stableTone(id: string) {
  const total = Array.from(id).reduce((sum, character) => sum + character.charCodeAt(0), 0);
  return tones[total % tones.length];
}

function questionDifficulty(value?: number): Difficulty {
  if (value === 2) return "进阶";
  if (value === 1) return "中级";
  return "入门";
}

export function difficultyRequest(value: Difficulty) {
  if (value === "进阶") return 2;
  if (value === "中级") return 1;
  return 0;
}

export function toQuestion(value: ApiQuestion, bankId?: string): Question {
  return {
    id: String(value.id),
    bankId,
    title: value.title,
    content: value.content ?? "",
    answer: value.answer,
    tags: value.tags ?? [],
    difficulty: questionDifficulty(value.difficulty),
    views: Number(value.viewNum) || 0,
    likes: Number(value.thumbNum) || 0,
    favourites: Number(value.favourNum) || 0,
    vip: value.needVip === true || value.needVip === 1,
  };
}

export function toQuestionBank(value: ApiQuestionBank): QuestionBank {
  return {
    id: String(value.id),
    eyebrow: "精选题库",
    title: value.title,
    description: value.description ?? "",
    creator: value.userVO?.userName ?? "梅问用户",
    creatorId: value.userId ? String(value.userId) : undefined,
    progress: 0,
    questions: Number(value.questionPageVO?.total) || 0,
    level: "中级",
    tone: stableTone(String(value.id)),
    category: "题库",
    picture: value.picture,
  };
}

export function isActiveVip(user: ApiUser | null) {
  if (!user || user.userRole !== 2) return false;
  if (!user.vipExpireTime) return true;
  return new Date(user.vipExpireTime).getTime() > Date.now();
}

export const authService = {
  login(userAccount: string, userPassword: string, remember: boolean) {
    return apiRequest<ApiUser>("/auth/login", {
      method: "POST",
      body: { userAccount, userPassword },
      persistResponseToken: remember,
    });
  },
  register(userAccount: string, userPassword: string, checkPassword: string) {
    return apiRequest<number>("/auth/register", {
      method: "POST",
      body: { userAccount, userPassword, checkPassword },
    });
  },
  logout() {
    return apiRequest<null>("/auth/logout", { method: "POST" });
  },
};

export const userService = {
  me(signal?: AbortSignal) {
    return apiRequest<ApiUser>("/users/me", { signal });
  },
  updateMe(body: ProfileUpdate) {
    return apiRequest<null>("/users/me", { method: "PUT", body });
  },
  changePassword(password: string, checkPassword: string) {
    return apiRequest<null>("/users/me/password", {
      method: "PUT",
      body: { password, checkPassword },
    });
  },
  publicProfile(userId: string, signal?: AbortSignal) {
    return apiRequest<ApiUser>(`/users/${encodeURIComponent(userId)}`, { signal });
  },
  avatarCredentials(filename: string) {
    return apiRequest<AvatarCredentials>(
      `/avatars/credentials?filename=${encodeURIComponent(filename)}`,
    );
  },
  confirmAvatarUpload() {
    return apiRequest<string>("/avatars/confirmation", { method: "POST" });
  },
};

export const bankService = {
  async search(
    request: {
      current: number;
      pageSize: number;
      searchText?: string;
      sortField?: string;
      sortOrder?: "ascend" | "descend";
    },
    signal?: AbortSignal,
  ) {
    const result = await apiRequest<unknown>("/question-banks/search", {
      method: "POST",
      body: request,
      signal,
    });
    const page = normalizePage<ApiQuestionBank>(result);
    return { ...page, records: page.records.map(toQuestionBank) };
  },
  async detail(questionBankId: string, signal?: AbortSignal) {
    const value = await apiRequest<ApiQuestionBank>(
      `/question-banks/${encodeURIComponent(questionBankId)}?needQueryQuestionList=false`,
      { signal },
    );
    return toQuestionBank(value);
  },
  async questions(questionBankId: string, current: number, pageSize: number, signal?: AbortSignal) {
    const query = new URLSearchParams({ current: String(current), pageSize: String(pageSize) });
    const result = await apiRequest<unknown>(
      `/question-bank-relations/banks/${encodeURIComponent(questionBankId)}/questions?${query}`,
      { signal },
    );
    const page = normalizePage<ApiQuestion>(result);
    return {
      ...page,
      records: page.records.map((question) => toQuestion(question, questionBankId)),
    };
  },
};

export const questionService = {
  async search(
    request: {
      current: number;
      pageSize: number;
      questionBankId?: number;
      searchText?: string;
      tags?: string[];
      difficulty?: number;
    },
    signal?: AbortSignal,
  ) {
    const result = await apiRequest<unknown>("/questions/search", {
      method: "POST",
      body: request,
      signal,
    });
    const page = normalizePage<ApiQuestion>(result);
    return { ...page, records: page.records.map((question) => toQuestion(question)) };
  },
  async detail(id: string, signal?: AbortSignal) {
    const value = await apiRequest<ApiQuestion>(`/questions/${encodeURIComponent(id)}`, { signal });
    return toQuestion(value);
  },
  create(body: QuestionDraftRequest) {
    return apiRequest<string | number>("/questions", { method: "POST", body });
  },
  update(body: QuestionDraftRequest & { id: string }) {
    const { id, ...request } = body;
    return apiRequest<boolean>(`/questions/${encodeURIComponent(id)}`, { method: "PUT", body: request });
  },
  remove(id: string) {
    return apiRequest<null>(`/questions/${encodeURIComponent(id)}`, { method: "DELETE" });
  },
};

export const favouriteService = {
  async mine(current: number, pageSize: number, signal?: AbortSignal) {
    const query = new URLSearchParams({ current: String(current), pageSize: String(pageSize) });
    const result = await apiRequest<unknown>(`/favourites/my?${query}`, { signal });
    const page = normalizePage<ApiQuestion>(result);
    return { ...page, records: page.records.map((question) => toQuestion(question)) };
  },
  status(id: string, signal?: AbortSignal) {
    return apiRequest<boolean>(`/favourites/questions/${encodeURIComponent(id)}/status`, { signal });
  },
  count(id: string, signal?: AbortSignal) {
    return apiRequest<number>(`/favourites/questions/${encodeURIComponent(id)}/count`, { signal });
  },
  add(id: string) {
    return apiRequest<boolean>(`/favourites/questions/${encodeURIComponent(id)}`, { method: "POST" });
  },
  remove(id: string) {
    return apiRequest<boolean>(`/favourites/questions/${encodeURIComponent(id)}`, { method: "DELETE" });
  },
};

export const thumbService = {
  status(id: string, signal?: AbortSignal) {
    return apiRequest<boolean>(`/thumbs/questions/${encodeURIComponent(id)}/status`, { signal });
  },
  count(id: string, signal?: AbortSignal) {
    return apiRequest<number>(`/thumbs/questions/${encodeURIComponent(id)}/count`, { signal });
  },
  add(id: string) {
    return apiRequest<boolean>(`/thumbs/questions/${encodeURIComponent(id)}`, { method: "POST" });
  },
  remove(id: string) {
    return apiRequest<boolean>(`/thumbs/questions/${encodeURIComponent(id)}`, { method: "DELETE" });
  },
};

export const signInService = {
  add() {
    return apiRequest<boolean>("/sign-ins", { method: "POST" });
  },
  streak(signal?: AbortSignal) {
    return apiRequest<number>("/sign-ins/streak", { signal });
  },
  today(signal?: AbortSignal) {
    return apiRequest<boolean>("/sign-ins/today", { signal });
  },
  currentMonthCount(signal?: AbortSignal) {
    return apiRequest<number>("/sign-ins/monthly-count", { signal });
  },
  records(year: number, month: number, signal?: AbortSignal) {
    const query = new URLSearchParams({ year: String(year), month: String(month) });
    return apiRequest<number[]>(`/sign-ins/records?${query}`, { signal });
  },
};

export const membershipService = {
  submit(levelId: number, durationType: string, durationValue: number) {
    return apiRequest<MembershipOrder>("/membership-orders", {
      method: "POST",
      body: { levelId, durationType, durationValue, currency: "CNY" },
    });
  },
};
