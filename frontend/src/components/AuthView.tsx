import { useState } from "react";
import { ArrowRight, ShieldCheck } from "lucide-react";
import type { AuthSession } from "../lib/types";
import { api } from "../lib/api";
import styles from "../App.module.css";

interface Props {
  onAuthenticated: (session: AuthSession) => void;
}
export function AuthView({ onAuthenticated }: Props) {
  const [register, setRegister] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const session = register
        ? await api.register(name, email, password)
        : await api.login(email, password);
      onAuthenticated(session);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Không thể đăng nhập.");
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className={styles.authPage}>
      <div className={styles.authIntro}>
        <span className={styles.logoMark}>L</span>
        <span className={styles.authEyebrow}>LUMEN · CREDIT LINE</span>
        <h1>
          Một khoảng thở cho <em>kế hoạch của bạn.</em>
        </h1>
        <p>
          Mô phỏng hạn mức tín dụng, lịch trả góp và từng quyết định tài chính
          trong không gian riêng của bạn.
        </p>
        <span className={styles.authFoot}>
          <ShieldCheck size={16} /> Simulator only · No real money
        </span>
      </div>
      <form className={styles.authCard} onSubmit={submit}>
        <span className={styles.overline}>
          {register ? "CREATE AN ACCOUNT" : "WELCOME BACK"}
        </span>
        <h2>{register ? "Tạo tài khoản." : "Đăng nhập."}</h2>
        <p>
          {register
            ? "Bắt đầu với hồ sơ tín dụng riêng của bạn."
            : "Tiếp tục hành trình tài chính của bạn."}
        </p>
        {register && (
          <label>
            Họ và tên
            <input
              required
              maxLength={100}
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoComplete="name"
            />
          </label>
        )}
        <label>
          Email
          <input
            required
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
        </label>
        <label>
          Mật khẩu
          <input
            required
            type="password"
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete={register ? "new-password" : "current-password"}
          />
        </label>
        {error && (
          <div className={styles.authError} role="alert">
            {error}
          </div>
        )}
        <button className={styles.goldButton} disabled={busy} type="submit">
          {busy ? "Đang xử lý…" : register ? "Tạo tài khoản" : "Đăng nhập"}{" "}
          <ArrowRight size={16} />
        </button>
        <button
          type="button"
          className={styles.authSwitch}
          onClick={() => {
            setRegister(!register);
            setError("");
          }}
        >
          {register
            ? "Đã có tài khoản? Đăng nhập"
            : "Chưa có tài khoản? Đăng ký"}
        </button>
      </form>
    </div>
  );
}
