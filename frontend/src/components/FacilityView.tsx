import { useState } from "react";
import {
  ArrowRight,
  CircleAlert,
  CreditCard,
  Info,
  ShieldCheck,
  Sparkles,
} from "lucide-react";
import type { CreditAccount } from "../lib/types";
import { currency, parseMoney, schedulePreview } from "../lib/format";
import styles from "../App.module.css";

interface Props {
  account: CreditAccount | null;
  busy: boolean;
  onApply: () => void;
  onUtilize: (amount: number) => Promise<void>;
}

export function FacilityView({ account, busy, onApply, onUtilize }: Props) {
  const [amountText, setAmountText] = useState("800.00");
  const [validation, setValidation] = useState("");
  const amount = parseMoney(amountText);
  const schedule =
    amount !== null && amount >= 0.04 ? schedulePreview(amount) : null;

  if (!account)
    return (
      <div className={styles.pageFade}>
        <div className={styles.pageEyebrow}>
          <span /> MY CREDIT LINE
        </div>
        <div className={styles.pageHeading}>
          <div>
            <h1>Hạn mức của bạn.</h1>
            <p>Bắt đầu bằng một đánh giá đơn giản để mở hạn mức demo.</p>
          </div>
        </div>
        <div className={styles.emptyPanel}>
          <span className={styles.emptySymbol}>✦</span>
          <strong>Chưa có hạn mức</strong>
          <p>Gửi hồ sơ tài chính để xem quyết định và hạn mức phù hợp.</p>
          <button className={styles.darkButton} onClick={onApply}>
            Đánh giá ngay <ArrowRight size={16} />
          </button>
        </div>
      </div>
    );

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (amount === null || amount < 0.04) {
      setValidation("Nhập ít nhất SGD 0.04, tối đa 2 chữ số thập phân.");
      return;
    }
    if (amount > account!.availableLimit) {
      setValidation("Số tiền vượt hạn mức khả dụng của bạn.");
      return;
    }
    setValidation("");
    await onUtilize(amount);
  }

  const utilization = account.totalLimit
    ? (account.usedLimit / account.totalLimit) * 100
    : 0;
  return (
    <div className={styles.pageFade}>
      <div className={styles.pageEyebrow}>
        <span /> YOUR FACILITY · ACTIVE
      </div>
      <div className={styles.pageHeading}>
        <div>
          <h1>
            Hạn mức <em>của bạn.</em>
          </h1>
          <p>
            Kiểm soát số dư và sử dụng tín dụng theo kế hoạch của riêng mình.
          </p>
        </div>
      </div>
      <div className={styles.facilityGrid}>
        <div className={styles.facilityBalance}>
          <div className={styles.facilityBalanceTop}>
            <span className={styles.iconTileDark}>
              <CreditCard size={20} />
            </span>
            <span className={styles.overlineLight}>
              LUMEN CREDIT LINE · SGD
            </span>
          </div>
          <span className={styles.balanceLabel}>Hạn mức khả dụng</span>
          <strong className={styles.balanceNumber}>
            {currency(account.availableLimit)}
          </strong>
          <div className={styles.balanceTrack}>
            <span style={{ width: `${utilization}%` }} />
          </div>
          <div className={styles.balanceSplit}>
            <div>
              <span>TỔNG HẠN MỨC</span>
              <strong>{currency(account.totalLimit)}</strong>
            </div>
            <div>
              <span>ĐANG SỬ DỤNG</span>
              <strong>{currency(account.usedLimit)}</strong>
            </div>
          </div>
          <div className={styles.balanceQuote}>
            “Mỗi kỳ hoàn trả là một phần hạn mức được mở lại.”
          </div>
        </div>
        <div className={styles.utilizeCard}>
          <div className={styles.formCardTop}>
            <span className={styles.iconTile}>
              <Sparkles size={20} />
            </span>
            <div>
              <span className={styles.overline}>NEW DRAWDOWN</span>
              <h2>Sử dụng hạn mức</h2>
            </div>
          </div>
          <p className={styles.formDescription}>
            Chọn số tiền cần sử dụng. Khoản này sẽ được chia thành 4 kỳ trả gốc
            hàng tháng, không lãi và phí trong bản demo.
          </p>
          <form onSubmit={submit} noValidate>
            <label className={styles.utilizeLabel} htmlFor="drawdown-amount">
              Số tiền muốn sử dụng
            </label>
            <div className={styles.largeMoneyInput}>
              <span>SGD</span>
              <input
                id="drawdown-amount"
                value={amountText}
                onChange={(event) => setAmountText(event.target.value)}
                inputMode="decimal"
                autoComplete="off"
              />
              <button
                type="button"
                onClick={() => setAmountText(account.availableLimit.toFixed(2))}
              >
                TỐI ĐA
              </button>
            </div>
            <div className={styles.availableHint}>
              <Info size={15} /> Khả dụng:{" "}
              <strong>{currency(account.availableLimit)}</strong>
            </div>
            {validation && (
              <p className={styles.fieldError} role="alert">
                <CircleAlert size={15} /> {validation}
              </p>
            )}
            <button
              className={styles.darkButton}
              type="submit"
              disabled={busy || account.availableLimit < 0.04}
            >
              {busy ? "Đang xử lý…" : "Sử dụng hạn mức"}{" "}
              <ArrowRight size={17} />
            </button>
          </form>
        </div>
      </div>
      <div className={styles.previewGrid}>
        <div className={styles.previewIntro}>
          <span className={styles.overline}>THANH TOÁN CÓ KẾ HOẠCH</span>
          <h2>
            Minh bạch
            <br />
            <em>từng kỳ.</em>
          </h2>
          <p>
            Khoản sử dụng được chia thành bốn kỳ. Kỳ cuối nhận phần lẻ để tổng
            luôn chính xác.
          </p>
          <div>
            <ShieldCheck size={18} /> Tính toán chính xác đến từng cent.
          </div>
        </div>
        <div className={styles.previewSchedule}>
          <div className={styles.previewScheduleTop}>
            <span>LỊCH DỰ KIẾN</span>
            <strong>{amount && amount >= 0.04 ? currency(amount) : "—"}</strong>
          </div>
          {[0, 1, 2, 3].map((index) => (
            <div key={index} className={styles.previewRow}>
              <span className={styles.previewIndex}>0{index + 1}</span>
              <span>
                Kỳ {index + 1}
                <small>Sau {index + 1} tháng</small>
              </span>
              <strong>{schedule ? currency(schedule[index]) : "—"}</strong>
            </div>
          ))}
        </div>
      </div>
      <div className={styles.finePrint}>
        Sử dụng hạn mức chỉ tạo nghĩa vụ trả nợ giả lập; không có tiền được
        chuyển đi.
      </div>
    </div>
  );
}
