import {
  ArrowRight,
  ArrowUpRight,
  CircleCheck,
  CreditCard,
  Layers3,
} from "lucide-react";
import type { Drawdown } from "../lib/types";
import { currency, shortDate, shortId } from "../lib/format";
import styles from "../App.module.css";

interface Props {
  drawdowns: Drawdown[];
  onOpen: (id: string) => void;
  onUtilize: () => void;
}

export function DrawdownsView({ drawdowns, onOpen, onUtilize }: Props) {
  const active = drawdowns.filter((item) => item.status === "ACTIVE").length;
  const paid = drawdowns.length - active;
  return (
    <div className={styles.pageFade}>
      <div className={styles.pageEyebrow}>
        <span /> CREDIT UTILIZATION · HISTORY
      </div>
      <div className={styles.pageHeading}>
        <div>
          <h1>
            Các khoản <em>đã sử dụng.</em>
          </h1>
          <p>Mỗi khoản đều có lịch trả gốc minh bạch và trạng thái rõ ràng.</p>
        </div>
        <button className={styles.outlineButton} onClick={onUtilize}>
          Tạo khoản mới <ArrowUpRight size={17} />
        </button>
      </div>
      <div className={styles.historyMetrics}>
        <div>
          <span>TẤT CẢ KHOẢN</span>
          <strong>{String(drawdowns.length).padStart(2, "0")}</strong>
        </div>
        <div>
          <span>ĐANG HOẠT ĐỘNG</span>
          <strong>{String(active).padStart(2, "0")}</strong>
        </div>
        <div>
          <span>ĐÃ TẤT TOÁN</span>
          <strong>{String(paid).padStart(2, "0")}</strong>
        </div>
      </div>
      <div className={styles.sectionHeading}>
        <div>
          <span className={styles.overline}>DRAWDOWN HISTORY</span>
          <h2>Danh sách khoản sử dụng</h2>
        </div>
      </div>
      {drawdowns.length ? (
        <div className={styles.historyList}>
          {drawdowns.map((item, index) => (
            <button
              className={styles.historyItem}
              key={item.drawdownId}
              onClick={() => onOpen(item.drawdownId)}
            >
              <span className={styles.historyNumber}>
                {String(drawdowns.length - index).padStart(2, "0")}
              </span>
              <span className={styles.historyIcon}>
                {item.status === "PAID_OFF" ? (
                  <CircleCheck size={24} />
                ) : (
                  <CreditCard size={24} />
                )}
              </span>
              <span className={styles.historyName}>
                <strong>Khoản sử dụng {shortId(item.drawdownId)}</strong>
                <small>Ngày tạo {shortDate(item.createdAt)}</small>
              </span>
              <span className={styles.historyAmount}>
                <strong>{currency(item.originalAmount)}</strong>
                <small>Dư nợ {currency(item.outstandingPrincipal)}</small>
              </span>
              <span
                className={`${styles.statusPill} ${item.status === "PAID_OFF" ? styles.statusPaid : ""}`}
              >
                {item.status === "PAID_OFF" ? "Đã tất toán" : "Đang trả góp"}
              </span>
              <ArrowUpRight className={styles.historyArrow} size={18} />
            </button>
          ))}
        </div>
      ) : (
        <div className={styles.emptyPanel}>
          <span className={styles.emptySymbol}>
            <Layers3 size={25} />
          </span>
          <strong>Chưa có khoản sử dụng</strong>
          <p>Bắt đầu sử dụng hạn mức để theo dõi các kỳ thanh toán tại đây.</p>
          <button className={styles.linkButton} onClick={onUtilize}>
            Sử dụng hạn mức <ArrowRight size={16} />
          </button>
        </div>
      )}
    </div>
  );
}
