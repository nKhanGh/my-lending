import {
  ArrowRight,
  ArrowUpRight,
  CalendarDays,
  CircleCheck,
  CreditCard,
  MoveUpRight,
  Sparkles,
} from "lucide-react";
import type { CreditAccount, CreditApplication, Drawdown } from "../lib/types";
import { currency, shortDate, shortId } from "../lib/format";
import styles from "../App.module.css";

interface Props {
  account: CreditAccount;
  application: CreditApplication | null;
  drawdowns: Drawdown[];
  onNavigate: (page: "facility" | "drawdowns") => void;
  onOpen: (id: string) => void;
}

export function HomeView({
  account,
  application,
  drawdowns,
  onNavigate,
  onOpen,
}: Props) {
  const utilization = account.totalLimit
    ? Math.round((account.usedLimit / account.totalLimit) * 100)
    : 0;
  const active = drawdowns.filter((drawdown) => drawdown.status === "ACTIVE");
  return (
    <div className={styles.pageFade}>
      <div className={styles.pageEyebrow}>
        <span /> TỔNG QUAN · CREDIT LINE
      </div>
      <div className={styles.pageHeading}>
        <div>
          <h1>
            Xin chào, <em>bạn.</em>
          </h1>
          <p>Một góc nhìn rõ ràng cho hành trình tài chính của bạn.</p>
        </div>
        <button
          className={styles.outlineButton}
          onClick={() => onNavigate("facility")}
        >
          Sử dụng hạn mức <ArrowUpRight size={17} />
        </button>
      </div>

      <div className={styles.homeGrid}>
        <section className={styles.heroCard}>
          <div className={styles.heroPattern} />
          <div className={styles.heroTop}>
            <span className={styles.heroBrand}>
              <span className={styles.heroBrandMark}>L</span> LUMEN{" "}
              <small>· CREDIT LINE</small>
            </span>
            <span className={styles.heroChip}>ACTIVE</span>
          </div>
          <div className={styles.heroBalance}>
            <span>HẠN MỨC KHẢ DỤNG</span>
            <strong>{currency(account.availableLimit)}</strong>
            <p>Sẵn sàng cho kế hoạch tiếp theo của bạn.</p>
          </div>
          <div className={styles.heroBottom}>
            <div>
              <span>TỔNG HẠN MỨC</span>
              <strong>{currency(account.totalLimit)}</strong>
            </div>
            <div>
              <span>ĐÃ SỬ DỤNG</span>
              <strong>{currency(account.usedLimit)}</strong>
            </div>
            <CreditCard size={32} strokeWidth={1.3} />
          </div>
        </section>
        <section className={styles.snapshotCard}>
          <div className={styles.snapshotTop}>
            <span className={styles.iconTile}>
              <Sparkles size={19} />
            </span>
            <span className={styles.overline}>SỨC KHỎE HẠN MỨC</span>
          </div>
          <div className={styles.snapshotMain}>
            <div
              className={styles.ring}
              style={{
                background: `conic-gradient(#b28d5c ${utilization}%, #eee9e1 ${utilization}% 100%)`,
              }}
            >
              <div>
                <strong>{utilization}%</strong>
                <small>đã sử dụng</small>
              </div>
            </div>
            <div className={styles.snapshotNumbers}>
              <div>
                <span>Hạn mức hiện tại</span>
                <strong>{currency(account.totalLimit)}</strong>
              </div>
              <div>
                <span>Còn khả dụng</span>
                <strong>{currency(account.availableLimit)}</strong>
              </div>
            </div>
          </div>
          <div className={styles.snapshotFoot}>
            <CircleCheck size={16} /> Hạn mức được hoàn lại khi bạn trả từng kỳ.
          </div>
        </section>
      </div>

      <div className={styles.metricGrid}>
        <div className={styles.metric}>
          <span className={styles.metricIcon}>
            <MoveUpRight size={19} />
          </span>
          <span>DƯ NỢ GỐC</span>
          <strong>{currency(account.usedLimit)}</strong>
          <small>Không có lãi và phí trong bản demo</small>
        </div>
        <div className={styles.metric}>
          <span className={styles.metricIcon}>
            <CalendarDays size={19} />
          </span>
          <span>KHOẢN ĐANG HOẠT ĐỘNG</span>
          <strong>{String(active.length).padStart(2, "0")}</strong>
          <small>Trong tổng số {drawdowns.length} lần sử dụng</small>
        </div>
        <div className={styles.metric}>
          <span className={styles.metricIcon}>
            <CircleCheck size={19} />
          </span>
          <span>TRẠNG THÁI HỒ SƠ</span>
          <strong className={styles.metricStatus}>Đã phê duyệt</strong>
          <small>
            {application?.policyVersion ?? "DEMO_V1"} ·{" "}
            {application ? shortDate(application.assessedAt) : "—"}
          </small>
        </div>
      </div>

      <div className={styles.sectionHeading}>
        <div>
          <span className={styles.overline}>CÁC KHOẢN ĐÃ SỬ DỤNG</span>
          <h2>Hoạt động gần đây</h2>
        </div>
        <button
          className={styles.linkButton}
          onClick={() => onNavigate("drawdowns")}
        >
          Xem tất cả <ArrowRight size={16} />
        </button>
      </div>
      {drawdowns.length ? (
        <div className={styles.drawdownList}>
          {drawdowns.slice(0, 3).map((drawdown) => (
            <button
              key={drawdown.drawdownId}
              className={styles.drawdownRow}
              onClick={() => onOpen(drawdown.drawdownId)}
            >
              <span className={styles.drawdownRowIcon}>
                <CreditCard size={21} />
              </span>
              <span className={styles.drawdownRowTitle}>
                <strong>{shortId(drawdown.drawdownId)}</strong>
                <small>
                  {shortDate(drawdown.createdAt)} ·{" "}
                  {drawdown.status === "PAID_OFF"
                    ? "Đã tất toán"
                    : "Đang trả góp"}
                </small>
              </span>
              <span className={styles.drawdownRowAmount}>
                <strong>{currency(drawdown.originalAmount)}</strong>
                <small>Dư nợ {currency(drawdown.outstandingPrincipal)}</small>
              </span>
              <ArrowUpRight size={18} />
            </button>
          ))}
        </div>
      ) : (
        <div className={styles.emptyPanel}>
          <span className={styles.emptySymbol}>✦</span>
          <strong>Chưa sử dụng hạn mức</strong>
          <p>
            Khi bạn tạo một khoản sử dụng, lịch trả góp sẽ xuất hiện tại đây.
          </p>
          <button
            className={styles.linkButton}
            onClick={() => onNavigate("facility")}
          >
            Bắt đầu sử dụng <ArrowRight size={16} />
          </button>
        </div>
      )}
      <div className={styles.finePrint}>
        Hạn mức và giao dịch trên ứng dụng này là dữ liệu mô phỏng, không phải
        sản phẩm tín dụng thật.
      </div>
    </div>
  );
}
