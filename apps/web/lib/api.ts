import type { ApiResponse } from '@selfinterior/shared-types';

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

export class ApiRequestError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
  }
}

export async function apiRequest<T>(
  path: string,
  init?: RequestInit,
): Promise<ApiResponse<T>> {
  const isFormData =
    typeof FormData !== 'undefined' && init?.body instanceof FormData;
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: isFormData
      ? { ...(init?.headers ?? {}) }
      : {
          'Content-Type': 'application/json',
          ...(init?.headers ?? {}),
        },
    cache: 'no-store',
  });

  if (!response.ok) {
    throw new ApiRequestError(
      `API 요청 실패: ${response.status}`,
      response.status,
    );
  }

  return (await response.json()) as ApiResponse<T>;
}
