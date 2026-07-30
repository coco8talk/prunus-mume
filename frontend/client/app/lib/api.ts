const TOKEN_KEY = "meiwen.satoken";
const SUCCESS_CODES = new Set([0, 200]);

export type ApiEnvelope<T> = {
  code: number;
  data: T;
  message: string;
};

export type PageResult<T> = {
  records: T[];
  total: number;
  size: number;
  current: number;
};

export class ApiError extends Error {
  code?: number;
  status?: number;

  constructor(message: string, options?: { code?: number; status?: number }) {
    super(message);
    this.name = "ApiError";
    this.code = options?.code;
    this.status = options?.status;
  }
}

declare global {
  interface Window {
    __MEIWEN_RUNTIME_CONFIG__?: {
      apiBaseUrl?: string;
      membershipLevelId?: string;
    };
  }
}

const BUILD_API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

export function apiUrl(path: string) {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const runtimeBaseUrl = typeof window === "undefined"
    ? BUILD_API_BASE_URL
    : window.__MEIWEN_RUNTIME_CONFIG__?.apiBaseUrl ?? BUILD_API_BASE_URL;
  return `${runtimeBaseUrl.replace(/\/+$/, "")}${normalizedPath}`;
}

export function membershipLevelId() {
  const value = typeof window === "undefined"
    ? process.env.NEXT_PUBLIC_MEMBERSHIP_LEVEL_ID
    : window.__MEIWEN_RUNTIME_CONFIG__?.membershipLevelId ??
      process.env.NEXT_PUBLIC_MEMBERSHIP_LEVEL_ID;
  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

function readToken() {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(TOKEN_KEY) ?? window.sessionStorage.getItem(TOKEN_KEY);
}

export function persistToken(token: string, persistent: boolean) {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(TOKEN_KEY);
  window.sessionStorage.removeItem(TOKEN_KEY);
  (persistent ? window.localStorage : window.sessionStorage).setItem(TOKEN_KEY, token);
}

export function clearToken() {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(TOKEN_KEY);
  window.sessionStorage.removeItem(TOKEN_KEY);
}

type ApiRequestOptions = Omit<RequestInit, "body"> & {
  body?: unknown;
  persistResponseToken?: boolean;
};

export async function apiRequest<T>(
  path: string,
  { body, headers, persistResponseToken = true, ...init }: ApiRequestOptions = {},
): Promise<T> {
  const token = readToken();
  const response = await fetch(apiUrl(path), {
    ...init,
    body: body === undefined ? undefined : JSON.stringify(body),
    credentials: "include",
    headers: {
      Accept: "application/json",
      ...(body === undefined ? {} : { "Content-Type": "application/json" }),
      ...(token ? { satoken: token } : {}),
      ...headers,
    },
  });

  const responseToken = response.headers.get("satoken");
  if (responseToken) persistToken(responseToken, persistResponseToken);

  let envelope: ApiEnvelope<T>;
  try {
    envelope = await response.json() as ApiEnvelope<T>;
  } catch {
    throw new ApiError(response.ok ? "服务器返回了无法识别的响应" : `请求失败（${response.status}）`, {
      status: response.status,
    });
  }

  if (!response.ok || !SUCCESS_CODES.has(envelope.code)) {
    throw new ApiError(envelope.message || `请求失败（${response.status}）`, {
      code: envelope.code,
      status: response.status,
    });
  }

  return envelope.data;
}

export function normalizePage<T>(value: unknown): PageResult<T> {
  const candidate = value && typeof value === "object" && "data" in value
    ? (value as { data: unknown }).data
    : value;

  if (!candidate || typeof candidate !== "object") {
    return { records: [], total: 0, size: 10, current: 1 };
  }

  const page = candidate as Partial<PageResult<T>>;
  return {
    records: Array.isArray(page.records) ? page.records : [],
    total: Number(page.total) || 0,
    size: Number(page.size) || 10,
    current: Number(page.current) || 1,
  };
}

export function errorMessage(error: unknown) {
  if (error instanceof Error && error.message) return error.message;
  return "请求失败，请稍后重试";
}
