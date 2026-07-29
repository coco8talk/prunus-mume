export type ApiEnvelope<T> = {
  code: number;
  data: T | null;
  message: string;
};

export type AdminUser = {
  id: string;
  userAccount: string;
  userRole: number;
  userAvatar?: string | null;
  userName?: string | null;
};

type ApiResult<T> = {
  data: T;
  token: string | null;
};

const API_BASE_URL = (process.env.NEXT_PUBLIC_API_BASE_URL ?? "").replace(
  /\/$/,
  "",
);

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly code?: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  token?: string | null,
): Promise<ApiResult<T>> {
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");

  if (options.body) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("satoken", token);
  }

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: "include",
    });
  } catch {
    throw new ApiError(
      "Unable to reach the API. Check the configured backend URL and try again.",
    );
  }

  let envelope: ApiEnvelope<T>;

  try {
    envelope = (await response.json()) as ApiEnvelope<T>;
  } catch {
    throw new ApiError("The server returned an unreadable response.");
  }

  if (!response.ok || envelope.code < 200 || envelope.code >= 300) {
    throw new ApiError(
      envelope.message || "The request could not be completed.",
      envelope.code || response.status,
    );
  }

  return {
    data: envelope.data as T,
    token: response.headers.get("satoken"),
  };
}
