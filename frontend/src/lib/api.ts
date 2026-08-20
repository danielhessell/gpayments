const DEFAULT_API_URL = "http://localhost:8080";

export function apiUrl(path: string) {
  const base = process.env.API_URL ?? DEFAULT_API_URL;
  return `${base}${path}`;
}
