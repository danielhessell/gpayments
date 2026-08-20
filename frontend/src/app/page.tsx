import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export default async function HomePage() {
  const cookieStore = await cookies();
  const hasApiKey = Boolean(cookieStore.get("apiKey")?.value);
  redirect(hasApiKey ? "/invoices" : "/login");
}
