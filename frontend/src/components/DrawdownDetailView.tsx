import {
  ArrowLeft,
  ArrowRight,
  CalendarDays,
  Check,
  CircleCheck,
  Clock3,
  ReceiptText,
  ShieldCheck,
} from "lucide-react";
import type { DrawdownDetail, RepaymentHistory } from "../lib/types";
import { currency, fullDate, shortDate, shortId } from "../lib/format";
import styles from "../App.module.css";

interface Props {
  detail: DrawdownDetail;
  repayments: RepaymentHistory[];
  busy: boolean;
  onBack: () => void;
  onRepay: (id: string) => Promise<void>;
}

export function DrawdownDetailView({
  detail,
  repayments,
  busy,
  onBack,
  onRepay,
}: Props) {
  const paidCount = detail.instalments.filter(
    (item) => item.status === "PAID",
  ).length;
  const next = detail.instalments.find((item) => item.status === "UNPAID");
  return (
    <div className={styles.pageFade}>
      <button className={styles.backButton} onClick={onBack}>
        <ArrowLeft size={17} /> Trở về danh sách
      </button>
      <div className={styles.pageEyebrow}>
        <span /> DRAWDOWN · {shortId(detail.drawdownId)}
      </div>
      <div className={styles.pageHeading}>
        <div>
          <h1>
            Chi tiết <em>khoản sử dụng.</em>
          </h1>
          <p>
            Khởi tạo ngày {fullDate(detail.createdAt)} · 4 kỳ trả gốc hàng
            tháng.
          </p>
        </div>
        <span
          className={`${styles.statusPill} ${detail.status === "PAID_OFF" ? styles.statusPaid : ""}`}
        >
          {detail.status === "PAID_OFF" ? "Đã tất toán" : "Đang trả góp"}
        </span>
      </div>
      <div className={styles.detailSummary}>
        <div>
          <span>GIÁ TRỊ BAN ĐẦU</span>
          <strong>{currency(detail.originalAmount)}</strong>
        </div>
        <div>
          <span>DƯ NỢ CÒN LẠI</span>
          <strong>{currency(detail.outstandingPrincipal)}</strong>
        </div>
        <div>
          <span>TIẾN ĐỘ THANH TOÁN</span>
          <strong>
            {paidCount} <small>/ 4 kỳ</small>
          </strong>
        </div>
        <div className={styles.detailProgress}>
          <span style={{ width: `${paidCount * 25}%` }} />
        </div>
      </div>
      <div className={styles.detailGrid}>
        <section className={styles.scheduleCard}>
          <div className={styles.cardHeading}>
            <div>
              <span className={styles.overline}>PAYMENT PLAN</span>
              <h2>Lịch trả góp</h2>
            </div>
            <CalendarDays size={20} />
          </div>
          <div className={styles.scheduleList}>
            {detail.instalments.map((item) => {
              const payable =
                item.id === next?.id && detail.status === "ACTIVE";
              return (
                <div
                  className={`${styles.instalmentRow} ${item.status === "PAID" ? styles.instalmentPaid : ""}`}
                  key={item.id}
                >
                  <span className={styles.instalmentMark}>
                    {item.status === "PAID" ? (
                      <Check size={19} />
                    ) : (
                      String(item.sequenceNo).padStart(2, "0")
                    )}
                  </span>
                  <span className={styles.instalmentInfo}>
                    <strong>Kỳ {item.sequenceNo}</strong>
                    <small>
                      {item.status === "PAID" && item.paidAt
                        ? `Đã thanh toán ${shortDate(item.paidAt)}`
                        : `Đến hạn ${shortDate(item.dueDate)}`}
                    </small>
                  </span>
                  <span className={styles.instalmentRight}>
                    <strong>{currency(item.amount)}</strong>
                    {item.status === "PAID" ? (
                      <small className={styles.paidLabel}>ĐÃ TRẢ</small>
                    ) : payable ? (
                      <button
                        className={styles.payButton}
                        disabled={busy}
                        onClick={() => onRepay(item.id)}
                      >
                        {busy ? "Đang xử lý…" : "Thanh toán"}{" "}
                        <ArrowRight size={14} />
                      </button>
                    ) : (
                      <small>CHỜ ĐẾN LƯỢT</small>
                    )}
                  </span>
                </div>
              );
            })}
          </div>
        </section>
        <div className={styles.detailSide}>
          <section className={styles.nextCard}>
            {next ? (
              <>
                <span className={styles.iconTileDark}>
                  <Clock3 size={20} />
                </span>
                <span className={styles.overlineLight}>KỲ CẦN THANH TOÁN</span>
                <strong>{currency(next.amount)}</strong>
                <p>
                  Kỳ {next.sequenceNo} · Đến hạn {shortDate(next.dueDate)}
                </p>
                <button
                  className={styles.goldButton}
                  onClick={() => onRepay(next.id)}
                  disabled={busy}
                >
                  {busy ? "Đang xử lý…" : "Trả kỳ tiếp theo"}{" "}
                  <ArrowRight size={16} />
                </button>
              </>
            ) : (
              <>
                <span className={styles.iconTileDark}>
                  <CircleCheck size={20} />
                </span>
                <span className={styles.overlineLight}>HOÀN TẤT</span>
                <strong>Đã tất toán.</strong>
                <p>Toàn bộ dư nợ của khoản sử dụng này đã được hoàn trả.</p>
              </>
            )}
          </section>
          <div className={styles.sideNote}>
            <ShieldCheck size={20} />
            <p>
              Chỉ kỳ chưa trả sớm nhất được thanh toán. Mỗi kỳ được ghi nhận một
              lần và hoàn lại đúng số gốc vào hạn mức.
            </p>
          </div>
        </div>
      </div>
      <div className={styles.sectionHeading}>
        <div>
          <span className={styles.overline}>TRANSACTION HISTORY</span>
          <h2>Lịch sử thanh toán</h2>
        </div>
        <ReceiptText size={20} />
      </div>
      {repayments.length ? (
        <div className={styles.repaymentList}>
          {repayments.map((item) => (
            <div className={styles.repaymentRow} key={item.id}>
              <span className={styles.repaymentIcon}>
                <Check size={17} />
              </span>
              <span>
                <strong>
                  Thanh toán kỳ{" "}
                  {detail.instalments.find(
                    (instalment) => instalment.id === item.instalmentId,
                  )?.sequenceNo ?? "?"}
                </strong>
                <small>
                  {fullDate(item.paidAt)} · {shortId(item.id)}
                </small>
              </span>
              <strong>−{currency(item.amount)}</strong>
            </div>
          ))}
        </div>
      ) : (
        <div className={styles.emptyRepayments}>
          Chưa có giao dịch thanh toán cho khoản này.
        </div>
      )}
    </div>
  );
}
