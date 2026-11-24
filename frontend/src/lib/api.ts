import axios, { AxiosError } from "axios";
import type {
  ApiResponse,
  AuthSession,
  ApplicationInput,
  CreditAccount,
  CreditApplication,
  Drawdown,
  DrawdownDetail,
  Repayment,
  RepaymentHistory,
} from "./types";

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const client = axios.create({
  baseURL: "/api",
  timeout: 15000,
  headers: { Accept: "application/json" },
});

client.interceptors.request.use((config) => {
  const session = getAuthSession();
  if (session) config.headers.Authorization = `Bearer ${session.accessToken}`;
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<never>>) => {
    const status = error.response?.status;
    const message =
      error.response?.data?.message ??
      (status
        ? `Yêu cầu thất bại (${status}).`
        : "Không kết nối được backend. Hãy kiểm tra máy chủ và thử lại.");
    return Promise.reject(new ApiError(message, status));
  },
);

async function get<T>(path: string): Promise<T> {
  return (await client.get<ApiResponse<T>>(path)).data.result;
}

async function post<T>(path: string, body?: unknown, key?: string): Promise<T> {
  return (
    await client.post<ApiResponse<T>>(path, body, {
      headers: key ? { "Idempotency-Key": key } : undefined,
    })
  ).data.result;
}

export async function optional<T>(request: Promise<T>): Promise<T | null> {
  try {
    return await request;
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) return null;
    throw error;
  }
}

const SESSION_KEY = "mini-lending:session";
export function getAuthSession(): AuthSession | null {
  try {
    const saved = sessionStorage.getItem(SESSION_KEY);
    return saved ? (JSON.parse(saved) as AuthSession) : null;
  } catch {
    return null;
  }
}
export function setAuthSession(session: AuthSession | null): void {
  if (session) sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
  else sessionStorage.removeItem(SESSION_KEY);
}

export const api = {
  login: (email: string, password: string) =>
    post<AuthSession>("/auth/login", { email, password }),
  register: (displayName: string, email: string, password: string) =>
    post<AuthSession>("/auth/register", { displayName, email, password }),
  payment: (id: string) => get<Repayment>(`/payments/${id}`),
  latestApplication: () =>
    get<CreditApplication>("/credit-applications/latest"),
  submitApplication: (body: ApplicationInput) =>
    post<CreditApplication>("/credit-applications", body),
  account: () => get<CreditAccount>("/credit-account"),
  drawdowns: () => get<Drawdown[]>("/credit-drawdowns"),
  drawdown: (id: string) => get<DrawdownDetail>(`/credit-drawdowns/${id}`),
  repayments: (id: string) =>
    get<RepaymentHistory[]>(`/credit-drawdowns/${id}/repayments`),
  utilize: (amount: number, key: string) =>
    post<Drawdown>("/credit-drawdowns", { amount }, key),
  repay: (instalmentId: string, key: string) =>
    post<Repayment>(`/instalments/${instalmentId}/repay`, undefined, key),
};

export function idempotencyKey(action: string): string {
  const storageKey = `mini-lending:${getAuthSession()?.userId ?? "anonymous"}:${action}`;
  const existing = sessionStorage.getItem(storageKey);
  if (existing) return existing;
  const key = crypto.randomUUID();
  sessionStorage.setItem(storageKey, key);
  return key;
}

export function clearIdempotencyKey(action: string): void {
  sessionStorage.removeItem(
    `mini-lending:${getAuthSession()?.userId ?? "anonymous"}:${action}`,
  );
}
