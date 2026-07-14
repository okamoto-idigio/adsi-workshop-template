const SAGEMAKER_BASE_PATH = "/codeeditor/default/absports/3000";

export function withBasePath(path: string): string {
  if (process.env.NEXT_PUBLIC_SAGEMAKER === "1") {
    return `${SAGEMAKER_BASE_PATH}${path}`;
  }
  return path;
}

export async function apiClient<T>(
  path: string,
  options?: RequestInit
): Promise<T> {
  const url = withBasePath(`/api${path}`);
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({
      message: "エラーが発生しました",
      code: "UNKNOWN_ERROR",
    }));
    throw error;
  }

  return response.json();
}
