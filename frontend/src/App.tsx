import { useCallback, useEffect, useState } from "react";
import {
  ArrowRight,
  CircleCheck,
  CreditCard,
  FileText,
  LayoutGrid,
  Layers3,
  Menu,
  RefreshCw,
  ShieldCheck,
  Sparkles,
  X,
} from "lucide-react";
import { ApiError, api, clearIdempotencyKey, idempotencyKey, optional } from "./lib/api";
import type {
  ApplicationInput,
  CreditAccount,
  CreditApplication,
  Drawdown,
  DrawdownDetail,
  RepaymentHistory,
} from "./lib/types";
import { AssessmentView } from "./components/AssessmentView";
import { HomeView } from "./components/HomeView";
import { FacilityView } from "./components/FacilityView";
import { DrawdownsView } from "./components/DrawdownsView";
import { DrawdownDetailView } from "./components/DrawdownDetailView";
import styles from "./App.module.css";

type Page = "overview" | "facility" | "drawdowns" | "assessment" | "detail";
const navigation = [
  { page: "overview", label: "Tổng quan", icon: LayoutGrid },
  { page: "facility", label: "Hạn mức của tôi", icon: CreditCard },
  { page: "drawdowns", label: "Khoản đã sử dụng", icon: Layers3 },
  { page: "assessment", label: "Hồ sơ tín dụng", icon: FileText },
] as const;

export default function App() {
  const [page, setPage] = useState<Page>("overview");
  const [application, setApplication] = useState<CreditApplication | null>(
    null,
  );
  const [account, setAccount] = useState<CreditAccount | null>(null);
  const [drawdowns, setDrawdowns] = useState<Drawdown[]>([]);
  const [detail, setDetail] = useState<DrawdownDetail | null>(null);
  const [repayments, setRepayments] = useState<RepaymentHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [mobileNav, setMobileNav] = useState(false);

  const refresh = useCallback(async () => {
    const [nextApplication, nextAccount] = await Promise.all([
      optional(api.latestApplication()),
      optional(api.account()),
    ]);
    const nextDrawdowns = nextAccount ? await api.drawdowns() : [];
    setApplication(nextApplication);
    setAccount(nextAccount);
    setDrawdowns(nextDrawdowns);
    return nextAccount;
  }, []);

  useEffect(() => {
    let active = true;
    refresh()
      .then((nextAccount) => {
        if (active && !nextAccount) setPage("assessment");
      })
      .catch((cause: unknown) => {
        if (active) setError(messageOf(cause));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [refresh]);

  function navigate(next: Page) {
    setPage(next === "overview" && !account ? "assessment" : next);
    setMobileNav(false);
    setError("");
    setNotice("");
  }

  async function openDrawdown(id: string) {
    setPage("detail");
    setDetail(null);
    setDetailLoading(true);
    setError("");
    try {
      const [nextDetail, nextRepayments] = await Promise.all([
        api.drawdown(id),
        api.repayments(id),
      ]);
      setDetail(nextDetail);
      setRepayments(nextRepayments);
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setDetailLoading(false);
    }
  }

  async function submitApplication(input: ApplicationInput) {
    setBusy(true);
    setError("");
    try {
      const decision = await api.submitApplication(input);
      setApplication(decision);
      if (decision.decision === "APPROVED") {
        await refresh();
        setPage("overview");
        setNotice("Hồ sơ đã được duyệt. Hạn mức của bạn đã sẵn sàng.");
      } else {
        setPage("assessment");
      }
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setBusy(false);
    }
  }

  async function utilize(amount: number) {
    const action = `drawdown:${amount.toFixed(2)}`;
    setBusy(true);
    setError("");
    try {
      const created = await api.utilize(amount, idempotencyKey(action));
      await refresh();
      await openDrawdown(created.drawdownId);
      clearIdempotencyKey(action);
      setNotice("Khoản sử dụng đã được tạo cùng 4 kỳ trả góp.");
    } catch (cause) {
      if (isDefinitiveFailure(cause)) clearIdempotencyKey(action);
      setError(messageOf(cause));
    } finally {
      setBusy(false);
    }
  }

  async function repay(instalmentId: string) {
    if (!detail) return;
    const action = `repay:${instalmentId}`;
    setBusy(true);
    setError("");
    try {
      await api.repay(instalmentId, idempotencyKey(action));
      const id = detail.drawdownId;
      await refresh();
      const [nextDetail, nextRepayments] = await Promise.all([
        api.drawdown(id),
        api.repayments(id),
      ]);
      setDetail(nextDetail);
      setRepayments(nextRepayments);
      clearIdempotencyKey(action);
      setNotice(
        nextDetail.status === "PAID_OFF"
          ? "Khoản sử dụng đã được tất toán."
          : "Thanh toán thành công. Hạn mức đã được hoàn lại.",
      );
    } catch (cause) {
      if (isDefinitiveFailure(cause)) clearIdempotencyKey(action);
      setError(messageOf(cause));
    } finally {
      setBusy(false);
    }
  }

  async function reload() {
    setLoading(true);
    setError("");
    try {
      await refresh();
      if (page === "detail" && detail) {
        const [nextDetail, nextRepayments] = await Promise.all([
          api.drawdown(detail.drawdownId),
          api.repayments(detail.drawdownId),
        ]);
        setDetail(nextDetail);
        setRepayments(nextRepayments);
      }
      setNotice("Dữ liệu đã được cập nhật.");
    } catch (cause) {
      setError(messageOf(cause));
    } finally {
      setLoading(false);
    }
  }

  const activeNav = page === "detail" ? "drawdowns" : page;
  const pageLabel =
    navigation.find((item) => item.page === activeNav)?.label ?? "Tổng quan";

  return (
    <div className={styles.app}>
      {mobileNav && (
        <button
          className={styles.mobileVeil}
          onClick={() => setMobileNav(false)}
          aria-label="Đóng điều hướng"
        />
      )}
      <aside
        className={`${styles.sidebar} ${mobileNav ? styles.sidebarOpen : ""}`}
      >
        <button
          className={styles.logo}
          onClick={() => navigate("overview")}
          aria-label="Lumen — về tổng quan"
        >
          <span className={styles.logoMark}>L</span>
          <span>
            LUMEN<small>CREDIT LINE</small>
          </span>
        </button>
        <div className={styles.navCaption}>KHÔNG GIAN CỦA BẠN</div>
        <nav className={styles.nav} aria-label="Điều hướng chính">
          {navigation.map(({ page: itemPage, label, icon: Icon }) => (
            <button
              key={itemPage}
              className={`${styles.navItem} ${activeNav === itemPage ? styles.navActive : ""}`}
              onClick={() => navigate(itemPage)}
              aria-current={activeNav === itemPage ? "page" : undefined}
            >
              <Icon size={19} strokeWidth={1.8} />
              <span>{label}</span>
              {activeNav === itemPage && <span className={styles.navMarker} />}
            </button>
          ))}
        </nav>
        <div className={styles.sidebarBottom}>
          <div className={styles.sidebarInsight}>
            <span>
              <Sparkles size={18} />
            </span>
            <strong>Tài chính có chủ đích.</strong>
            <p>Một hạn mức rõ ràng, từng bước tiến vững vàng.</p>
          </div>
          <div className={styles.sideIdentity}>
            <span className={styles.identityAvatar}>D</span>
            <span>
              <strong>Demo account</strong>
              <small>Personal space</small>
            </span>
            <span className={styles.onlineDot} />
          </div>
        </div>
      </aside>

      <div className={styles.workspace}>
        <header className={styles.topbar}>
          <div className={styles.topLeft}>
            <button
              className={styles.mobileMenu}
              onClick={() => setMobileNav(true)}
              aria-label="Mở điều hướng"
            >
              <Menu size={22} />
            </button>
            <span>Trang chủ</span>
            <span className={styles.breadcrumbSlash}>/</span>
            <strong>{pageLabel}</strong>
          </div>
          <div className={styles.topRight}>
            <span className={styles.demoLabel}>
              <span /> DEMO ENVIRONMENT
            </span>
            <button
              className={styles.headerButton}
              onClick={reload}
              disabled={loading}
              aria-label="Tải lại dữ liệu"
              title="Tải lại dữ liệu"
            >
              <RefreshCw size={18} />
            </button>
            <span className={styles.headerDivider} />
            <span className={styles.headerAvatar}>D</span>
          </div>
        </header>
        <main className={styles.main}>
          {error && (
            <div className={styles.errorBanner} role="alert">
              <span>{error}</span>
              <button onClick={() => setError("")} aria-label="Đóng lỗi">
                <X size={17} />
              </button>
            </div>
          )}
          {notice && (
            <div className={styles.noticeBanner} role="status">
              <CircleCheck size={18} />
              <span>{notice}</span>
              <button onClick={() => setNotice("")} aria-label="Đóng thông báo">
                <X size={17} />
              </button>
            </div>
          )}
          {loading ? (
            <div className={styles.loading}>
              <span className={styles.spinner} /> Đang chuẩn bị không gian của
              bạn…
            </div>
          ) : (
            <>
              {(page === "assessment" || (page === "overview" && !account)) && (
                <AssessmentView
                  application={application}
                  busy={busy}
                  onSubmit={submitApplication}
                  onDashboard={() => navigate("overview")}
                />
              )}
              {page === "overview" && account && (
                <HomeView
                  account={account}
                  application={application}
                  drawdowns={drawdowns}
                  onNavigate={navigate}
                  onOpen={openDrawdown}
                />
              )}
              {page === "facility" && (
                <FacilityView
                  account={account}
                  busy={busy}
                  onApply={() => navigate("assessment")}
                  onUtilize={utilize}
                />
              )}
              {page === "drawdowns" && (
                <DrawdownsView
                  drawdowns={drawdowns}
                  onOpen={openDrawdown}
                  onUtilize={() => navigate("facility")}
                />
              )}
              {page === "detail" &&
                (detailLoading ? (
                  <div className={styles.loading}>
                    <span className={styles.spinner} /> Đang tải khoản sử dụng…
                  </div>
                ) : detail ? (
                  <DrawdownDetailView
                    detail={detail}
                    repayments={repayments}
                    busy={busy}
                    onBack={() => navigate("drawdowns")}
                    onRepay={repay}
                  />
                ) : (
                  <div className={styles.emptyPanel}>
                    <strong>Không tải được chi tiết</strong>
                    <button
                      className={styles.linkButton}
                      onClick={() => navigate("drawdowns")}
                    >
                      Trở về danh sách <ArrowRight size={16} />
                    </button>
                  </div>
                ))}
            </>
          )}
          <footer className={styles.footer}>
            <span>© 2026 Lumen. Designed for a clearer tomorrow.</span>
            <span>
              <ShieldCheck size={15} /> Simulator only · No real money
            </span>
          </footer>
        </main>
      </div>
    </div>
  );
}

function isDefinitiveFailure(cause: unknown): boolean {
  return cause instanceof ApiError && cause.status !== undefined &&
    cause.status >= 400 && cause.status < 500 && cause.status !== 408 && cause.status !== 429;
}

function messageOf(cause: unknown): string {
  return cause instanceof Error
    ? cause.message
    : "Có lỗi xảy ra. Vui lòng thử lại.";
}
