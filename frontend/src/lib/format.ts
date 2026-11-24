export const currency = (value: number) =>
  new Intl.NumberFormat("en-SG", { style: "currency", currency: "SGD", currencyDisplay: "code" }).format(
    value,
  );

export const compactCurrency = (value: number) =>
  new Intl.NumberFormat("en-SG", { maximumFractionDigits: 0 }).format(value);

export const shortDate = (value: string) =>
  new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  }).format(new Date(value));

export const fullDate = (value: string) =>
  new Intl.DateTimeFormat("vi-VN", {
    day: "2-digit",
    month: "long",
    year: "numeric",
  }).format(new Date(value));

export const shortId = (id: string) => `#${id.slice(0, 8).toUpperCase()}`;

export function schedulePreview(
  amount: number,
): [number, number, number, number] {
  const cents = Math.round(amount * 100);
  const first = Math.floor(cents / 4);
  return [first / 100, first / 100, first / 100, (cents - first * 3) / 100];
}

export function parseMoney(value: string): number | null {
  if (!/^\d+(?:\.\d{1,2})?$/.test(value.trim())) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

export const reasonLabel: Record<string, string> = {
  INSUFFICIENT_SURPLUS:
    "Thu nhập còn lại dưới mức tối thiểu của chính sách demo.",
  APPROVED_TIER_500: "Hạn mức được cấp theo bậc dư thu nhập 400–799 SGD.",
  APPROVED_TIER_1000: "Hạn mức được cấp theo bậc dư thu nhập 800–1.499 SGD.",
  APPROVED_TIER_2000: "Hạn mức được cấp theo bậc dư thu nhập từ 1.500 SGD.",
};
