import { useState } from "react";
import {
  ArrowRight,
  BadgeCheck,
  CircleAlert,
  Landmark,
  ShieldCheck,
  Sparkles,
} from "lucide-react";
import type { ApplicationInput, CreditApplication } from "../lib/types";
import { currency, fullDate, parseMoney, reasonLabel } from "../lib/format";
import styles from "../App.module.css";

interface Props {
  application: CreditApplication | null;
  busy: boolean;
  onSubmit: (
    input: ApplicationInput,
    profile?: { onTimePayments: number; missedPayments: number },
  ) => Promise<void>;
  onDashboard: () => void;
}

export function AssessmentView({
  application,
  busy,
  onSubmit,
  onDashboard,
}: Props) {
  const [income, setIncome] = useState("3000.00");
  const [expenses, setExpenses] = useState("1400.00");
  const [debt, setDebt] = useState("500.00");
  const [validation, setValidation] = useState("");
  const [useHistory, setUseHistory] = useState(false);
  const [onTime, setOnTime] = useState("4");
  const [missed, setMissed] = useState("0");
  const values = [parseMoney(income), parseMoney(expenses), parseMoney(debt)];
  const surplus = values.every((value) => value !== null)
    ? values[0]! - values[1]! - values[2]!
    : null;

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    if (values.some((value) => value === null)) {
      setValidation("Nhập số tiền không âm với tối đa 2 chữ số thập phân.");
      return;
    }
    if (useHistory && (!/^\d+$/.test(onTime) || !/^\d+$/.test(missed))) {
      setValidation("Số lần trả nợ phải là số nguyên không âm.");
      return;
    }
    setValidation("");
    await onSubmit(
      {
        monthlyNetIncome: values[0]!,
        monthlyLivingExpenses: values[1]!,
        monthlyDebtPayments: values[2]!,
        currency: "SGD",
      },
      useHistory
        ? { onTimePayments: Number(onTime), missedPayments: Number(missed) }
        : undefined,
    );
  }

  if (application?.decision === "APPROVED") {
    return (
      <div className={styles.pageFade}>
        <div className={styles.pageEyebrow}>
          <span /> CREDIT ASSESSMENT · {application.policyVersion}
        </div>
        <div className={styles.pageHeading}>
          <div>
            <h1>Hạn mức đã sẵn sàng.</h1>
            <p>
              Thông tin đánh giá của tài khoản demo, dựa trên chính sách mô
              phỏng.
            </p>
          </div>
        </div>
        <div className={styles.decisionGrid}>
          <div className={styles.decisionCard}>
            <div className={styles.decisionCheck}>
              <BadgeCheck size={25} />
            </div>
            <span className={styles.overlineLight}>ĐÁNH GIÁ THÀNH CÔNG</span>
            <h2>Bạn đã được cấp hạn mức</h2>
            <div className={styles.decisionLimit}>
              {currency(application.approvedLimit)}
            </div>
            <p>{reasonLabel[application.reasonCode]}</p>
            <button className={styles.goldButton} onClick={onDashboard}>
              Khám phá hạn mức <ArrowRight size={17} />
            </button>
            <span className={styles.decisionDecor} />
          </div>
          <div className={styles.decisionDetails}>
            <h3>Chi tiết quyết định</h3>
            <div>
              <span>Trạng thái</span>
              <strong className={styles.positive}>Đã phê duyệt</strong>
            </div>
            <div>
              <span>Dư thu nhập hàng tháng</span>
              <strong>{currency(application.monthlySurplus)}</strong>
            </div>
            <div>
              <span>Mã lý do</span>
              <strong>{application.reasonCode}</strong>
            </div>
            {application.debtToIncomeRatio !== undefined && (
              <div>
                <span>Tỷ lệ nghĩa vụ nợ</span>
                <strong>
                  {(application.debtToIncomeRatio * 100).toFixed(2)}%
                </strong>
              </div>
            )}
            {application.missedPayments !== undefined && (
              <div>
                <span>Lịch sử giả lập</span>
                <strong>
                  {application.onTimePayments} đúng hạn ·{" "}
                  {application.missedPayments} chậm trả
                </strong>
              </div>
            )}
            <div>
              <span>Phiên bản chính sách</span>
              <strong>{application.policyVersion}</strong>
            </div>
            <div>
              <span>Thời điểm đánh giá</span>
              <strong>{fullDate(application.assessedAt)}</strong>
            </div>
            <p>
              <ShieldCheck size={16} /> Quyết định chỉ phục vụ mục đích học tập
              và thử nghiệm.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.pageFade}>
      <div className={styles.pageEyebrow}>
        <span /> BƯỚC ĐẦU TIÊN · CREDIT ASSESSMENT
      </div>
      <div className={styles.pageHeading}>
        <div>
          <h1>
            Một khởi đầu rõ ràng
            <br />
            <em>cho mọi kế hoạch.</em>
          </h1>
          <p>Nhập ba con số cơ bản để xem hạn mức mô phỏng phù hợp với bạn.</p>
        </div>
      </div>
      {application?.decision === "REJECTED" && (
        <div className={styles.rejectionBanner}>
          <CircleAlert size={20} />
          <div>
            <strong>Chưa thể cấp hạn mức</strong>
            <span>
              {reasonLabel[application.reasonCode] ?? application.reasonCode}{" "}
              Bạn có thể nhập lại thông tin để đánh giá lần nữa.
            </span>
          </div>
        </div>
      )}
      <div className={styles.assessmentGrid}>
        <div className={styles.formCard}>
          <div className={styles.formCardTop}>
            <span className={styles.iconTile}>
              <Landmark size={20} />
            </span>
            <div>
              <span className={styles.overline}>HỒ SƠ TÀI CHÍNH</span>
              <h2>Đánh giá hạn mức</h2>
            </div>
          </div>
          <form onSubmit={submit} noValidate>
            <MoneyField
              label="Thu nhập ròng hàng tháng"
              description="Số tiền bạn nhận được sau thuế"
              value={income}
              onChange={setIncome}
            />
            <MoneyField
              label="Chi phí sinh hoạt"
              description="Các khoản chi tiêu thường xuyên"
              value={expenses}
              onChange={setExpenses}
            />
            <MoneyField
              label="Khoản trả nợ hiện có"
              description="Nghĩa vụ trả nợ mỗi tháng"
              value={debt}
              onChange={setDebt}
            />
            <label className={styles.historyToggle}>
              <input
                type="checkbox"
                checked={useHistory}
                onChange={(event) => setUseHistory(event.target.checked)}
              />
              Dùng lịch sử trả nợ giả lập (policy DEMO_V2)
            </label>
            {useHistory && (
              <div className={styles.historyFields}>
                <label>
                  Số lần đúng hạn
                  <input
                    type="number"
                    min="0"
                    step="1"
                    value={onTime}
                    onChange={(event) => setOnTime(event.target.value)}
                  />
                </label>
                <label>
                  Số lần chậm trả
                  <input
                    type="number"
                    min="0"
                    step="1"
                    value={missed}
                    onChange={(event) => setMissed(event.target.value)}
                  />
                </label>
              </div>
            )}
            <div className={styles.surplusPreview}>
              <span>
                Dư thu nhập dự kiến <small>Thu nhập − chi phí − nợ</small>
              </span>
              <strong>{surplus === null ? "—" : currency(surplus)}</strong>
            </div>
            {validation && (
              <p className={styles.fieldError} role="alert">
                {validation}
              </p>
            )}
            <button className={styles.darkButton} type="submit" disabled={busy}>
              {busy ? "Đang đánh giá…" : "Đánh giá hạn mức"}{" "}
              <ArrowRight size={17} />
            </button>
          </form>
        </div>
        <div className={styles.editorialCard}>
          <span className={styles.editorialOrb} />
          <div className={styles.editorialTop}>
            <Sparkles size={22} />
            <span>MINI LENDING / 01</span>
          </div>
          <div>
            <span className={styles.overlineLight}>
              A MORE CONSIDERED WAY FORWARD
            </span>
            <h2>
              Biết rõ khả năng.
              <br />
              <em>Chủ động tương lai.</em>
            </h2>
            <p>
              Quyết định minh bạch, hạn mức rõ ràng và từng kỳ thanh toán nằm
              trong tầm tay.
            </p>
          </div>
          <div className={styles.editorialFooter}>
            <span>01 / 03</span>
            <span>ĐÁNH GIÁ · SỬ DỤNG · HOÀN TRẢ</span>
          </div>
        </div>
      </div>
      <div className={styles.finePrint}>
        <ShieldCheck size={15} /> Đây là ứng dụng demo. Không sử dụng để cấp tín
        dụng hoặc xử lý tiền thật.
      </div>
    </div>
  );
}

function MoneyField({
  label,
  description,
  value,
  onChange,
}: {
  label: string;
  description: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className={styles.moneyField}>
      <span>
        <strong>{label}</strong>
        <small>{description}</small>
      </span>
      <div className={styles.moneyInput}>
        <span>SGD</span>
        <input
          inputMode="decimal"
          autoComplete="off"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          aria-label={label}
        />
      </div>
    </label>
  );
}
