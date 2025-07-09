import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import LoadingButton from "@/components/common/LoadingButton";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

export default function Register() {
  const { t } = useTranslation("common");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // 브라우저 뒤로가기(이탈) 감지
  useEffect(() => {
    const handler = (e: BeforeUnloadEvent) => {
      if (loading) {
        e.preventDefault();
        e.returnValue = t("register.backConfirm", "회원가입 처리 중입니다. 정말 나가시겠습니까?");
        return t("register.backConfirm", "회원가입 처리 중입니다. 정말 나가시겠습니까?");
      }
    };
    window.addEventListener("beforeunload", handler);
    return () => window.removeEventListener("beforeunload", handler);
  }, [loading, t]);

  // react-router popstate(뒤로가기) 감지
  useEffect(() => {
    const handler = (_: PopStateEvent) => {
      if (loading) {
        const confirmMsg = t("register.backConfirm", "회원가입 처리 중입니다. 정말 나가시겠습니까?");
        if (!window.confirm(confirmMsg)) {
          navigate(1); // 앞으로 다시 이동
        }
      }
    };
    window.addEventListener("popstate", handler);
    return () => window.removeEventListener("popstate", handler);
  }, [loading, t, navigate]);

  // zod 스키마를 i18n 메시지로 작성
  const schema = z.object({
    email: z.string().email(t("register.validation.email")),
    lastName: z.string().min(1, t("register.validation.lastName")),
    firstName: z.string().min(1, t("register.validation.firstName")),
    password: z.string().min(8, t("register.validation.password")),
    passwordConfirm: z.string(),
  }).refine((data) => data.password === data.passwordConfirm, {
    message: t("register.validation.passwordConfirm"),
    path: ["passwordConfirm"],
  });

  type FormData = z.infer<typeof schema>;

  const {
    register: formRegister,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setError("");
    setLoading(true);
    try {
      // TODO: 실제 회원가입 API 호출
      await new Promise((resolve) => setTimeout(resolve, 1000));
      // 성공 시 처리 (예: 라우팅)
    } catch (e: any) {
      setError(e?.message || t("register.error"));
    } finally {
      setLoading(false);
    }
  };

  // 뒤로가기 버튼 핸들러
  const handleBack = () => {
    if (loading) {
      const confirmMsg = t("register.backConfirm", "회원가입 처리 중입니다. 정말 나가시겠습니까?");
      if (window.confirm(confirmMsg)) {
        navigate("/");
      }
    } else {
      navigate("/");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted relative">
      {/* 왼쪽 상단 뒤로가기 버튼 */}
      <button
        type="button"
        onClick={handleBack}
        className="absolute left-4 top-4 flex items-center gap-1 text-muted-foreground hover:text-primary transition-colors text-lg font-medium"
        aria-label={t("register.back", "뒤로가기")}
      >
        <span className="text-2xl">←</span>
        <span className="hidden sm:inline">{t("register.back", "홈으로")}</span>
      </button>
      <div
        className="w-full max-w-md bg-background p-8 rounded-xl shadow-lg space-y-8"
      >
        <div className="text-center mb-2">
          <h2 className="text-3xl font-extrabold mb-2">{t("register.title")}</h2>
          <p className="text-muted-foreground text-base">{t("register.subtitle", "새 계정을 만들어보세요")}</p>
        </div>
        {error && (
          <div className="bg-destructive/10 text-destructive px-4 py-2 rounded text-sm">
            {error}
          </div>
        )}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <label htmlFor="lastName" className="block text-sm font-medium">{t("register.lastName")}</label>
            <input
              id="lastName"
              type="text"
              placeholder={t("register.lastNamePlaceholder", "성을 입력하세요")}
              {...formRegister("lastName")}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary"
              autoComplete="family-name"
            />
            {errors.lastName && <p className="text-destructive text-xs">{errors.lastName.message}</p>}
          </div>
          <div className="space-y-2">
            <label htmlFor="firstName" className="block text-sm font-medium">{t("register.firstName")}</label>
            <input
              id="firstName"
              type="text"
              placeholder={t("register.firstNamePlaceholder", "이름을 입력하세요")}
              {...formRegister("firstName")}
              className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary"
              autoComplete="given-name"
            />
            {errors.firstName && <p className="text-destructive text-xs">{errors.firstName.message}</p>}
          </div>
        </div>
        <div className="space-y-2">
          <label htmlFor="email" className="block text-sm font-medium">{t("register.email")}</label>
          <input
            id="email"
            type="email"
            placeholder={t("register.emailPlaceholder", "이메일을 입력하세요")}
            {...formRegister("email")}
            className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary"
            autoComplete="email"
          />
          {errors.email && <p className="text-destructive text-xs">{errors.email.message}</p>}
        </div>
        <div className="space-y-2">
          <label htmlFor="password" className="block text-sm font-medium">{t("register.password")}</label>
          <input
            id="password"
            type="password"
            placeholder={t("register.passwordPlaceholder", "비밀번호를 입력하세요")}
            {...formRegister("password")}
            className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary"
            autoComplete="new-password"
          />
          {errors.password && <p className="text-destructive text-xs">{errors.password.message}</p>}
        </div>
        <div className="space-y-2">
          <label htmlFor="passwordConfirm" className="block text-sm font-medium">{t("register.passwordConfirm")}</label>
          <input
            id="passwordConfirm"
            type="password"
            placeholder={t("register.passwordConfirmPlaceholder", "비밀번호를 다시 입력하세요")}
            {...formRegister("passwordConfirm")}
            className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary"
            autoComplete="new-password"
          />
          {errors.passwordConfirm && <p className="text-destructive text-xs">{errors.passwordConfirm.message}</p>}
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="w-full">
          <LoadingButton type="submit" loading={loading} className="w-full mt-4 h-12 text-base font-semibold">
            {t("register.submit")}
          </LoadingButton>
        </form>
      </div>
    </div>
  );
} 