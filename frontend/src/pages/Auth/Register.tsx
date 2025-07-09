import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import LoadingButton from "@/components/common/LoadingButton";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { registerUser, type FieldViolation } from "@/services/api/client";
import { handle } from "@oazapfts/runtime";
import log from "@/utils/logger";
import SuccessModal from "@/components/common/SuccessModal";

export default function Register() {
	const { t } = useTranslation("common");
	const [error, setError] = useState("");
	const [fieldErrors, setFieldErrors] = useState({
		email: "",
		lastName: "",
		firstName: "",
		password: "",
		passwordConfirm: "",
	});
	const [loading, setLoading] = useState(false);
	const [showSuccessModal, setShowSuccessModal] = useState(false);
	const navigate = useNavigate();

	// 브라우저 뒤로가기(이탈) 감지
	useEffect(() => {
		const handler = (e: BeforeUnloadEvent) => {
			if (loading) {
				e.preventDefault();
				e.returnValue = t(
					"register.backConfirm",
					"회원가입 처리 중입니다. 정말 나가시겠습니까?",
				);
				return t(
					"register.backConfirm",
					"회원가입 처리 중입니다. 정말 나가시겠습니까?",
				);
			}
		};
		window.addEventListener("beforeunload", handler);
		return () => window.removeEventListener("beforeunload", handler);
	}, [loading, t]);

	// react-router popstate(뒤로가기) 감지
	useEffect(() => {
		const handler = (_: PopStateEvent) => {
			if (loading) {
				const confirmMsg = t(
					"register.backConfirm",
					"회원가입 처리 중입니다. 정말 나가시겠습니까?",
				);
				if (!window.confirm(confirmMsg)) {
					navigate(1); // 앞으로 다시 이동
				}
			}
		};
		window.addEventListener("popstate", handler);
		return () => window.removeEventListener("popstate", handler);
	}, [loading, t, navigate]);

	// zod 스키마를 i18n 메시지로 작성
	const schema = z
		.object({
			email: z.string().email(t("register.validation.email")),
			lastName: z.string().min(1, t("register.validation.lastName")),
			firstName: z.string().min(1, t("register.validation.firstName")),
			password: z.string().min(8, t("register.validation.password")),
			passwordConfirm: z.string(),
		})
		.refine((data) => data.password === data.passwordConfirm, {
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
		setFieldErrors({
			email: "",
			lastName: "",
			firstName: "",
			password: "",
			passwordConfirm: "",
		});
		setLoading(true);
		try {
			// 회원가입 API 호출
			await handle(registerUser(data), {
				201() {
					// 회원가입 성공 - 모달 표시
					setShowSuccessModal(true);
				},
				409(error) {
					// 이미 가입된 이메일
					setFieldErrors((prev) => ({
						...prev,
						email:
							error.message ||
							t("register.emailAlreadyExists", "이미 가입된 이메일입니다."),
					}));
				},
				422(error) {
					// 유효성 오류 - 서버에서 필드별 에러를 받은 경우
					const errorData = error.details;

					if (errorData) {
						setFieldErrors((prev) => ({
							...prev,
							...handleFieldErrors(errorData),
						}));
					} else {
						setError(
							t("register.validationError", "입력 정보를 확인해주세요."),
						);
					}
				},
				500(error) {
					// 서버 오류
					setError(
						error.message ||
							t("register.serverError", "서버 오류가 발생했습니다."),
					);
				},
			});
		} catch (e: unknown) {
			log.error("회원가입 오류:", e);
			const errorMessage = e instanceof Error ? e.message : t("register.error");
			setError(errorMessage);
		} finally {
			setLoading(false);
		}
	};

	const handleSuccessModalConfirm = () => {
		navigate("/");
	};

	// 뒤로가기 버튼 핸들러
	const handleBack = () => {
		if (loading) {
			const confirmMsg = t(
				"register.backConfirm",
				"회원가입 처리 중입니다. 정말 나가시겠습니까?",
			);
			if (window.confirm(confirmMsg)) {
				navigate("/");
			}
		} else {
			navigate("/");
		}
	};

	const handleFieldErrors = (errorData: FieldViolation[]) => {
		const newErrors: Record<string, string> = {};
		errorData.forEach((err) => {
			if (err.field) {
				newErrors[err.field] = err.message || "";
			}
		});
		return newErrors;
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
			<div className="w-full max-w-md bg-background p-8 rounded-xl shadow-lg space-y-8">
				<div className="text-center mb-2">
					<h2 className="text-3xl font-extrabold mb-2">
						{t("register.title")}
					</h2>
					<p className="text-muted-foreground text-base">
						{t("register.subtitle", "새 계정을 만들어보세요")}
					</p>
				</div>
				{error && (
					<div className="bg-destructive/10 text-destructive px-4 py-2 rounded text-sm">
						{error}
					</div>
				)}
				<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
					<div className="space-y-2">
						<label htmlFor="lastName" className="block text-sm font-medium">
							{t("register.lastName")}
						</label>
						<input
							id="lastName"
							type="text"
							placeholder={t("register.lastNamePlaceholder", "성을 입력하세요")}
							{...formRegister("lastName")}
							className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary ${
								errors.lastName || fieldErrors.lastName
									? "border-destructive"
									: ""
							}`}
							autoComplete="family-name"
						/>
						{errors.lastName && (
							<p className="text-destructive text-xs">
								{errors.lastName.message}
							</p>
						)}
						{fieldErrors.lastName && (
							<p className="text-destructive text-xs">{fieldErrors.lastName}</p>
						)}
					</div>
					<div className="space-y-2">
						<label htmlFor="firstName" className="block text-sm font-medium">
							{t("register.firstName")}
						</label>
						<input
							id="firstName"
							type="text"
							placeholder={t(
								"register.firstNamePlaceholder",
								"이름을 입력하세요",
							)}
							{...formRegister("firstName")}
							className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary ${
								errors.firstName || fieldErrors.firstName
									? "border-destructive"
									: ""
							}`}
							autoComplete="given-name"
						/>
						{errors.firstName && (
							<p className="text-destructive text-xs">
								{errors.firstName.message}
							</p>
						)}
						{fieldErrors.firstName && (
							<p className="text-destructive text-xs">
								{fieldErrors.firstName}
							</p>
						)}
					</div>
				</div>
				<div className="space-y-2">
					<label htmlFor="email" className="block text-sm font-medium">
						{t("register.email")}
					</label>
					<input
						id="email"
						type="email"
						placeholder={t("register.emailPlaceholder", "이메일을 입력하세요")}
						{...formRegister("email")}
						className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary ${
							errors.email || fieldErrors.email ? "border-destructive" : ""
						}`}
						autoComplete="email"
					/>
					{errors.email && (
						<p className="text-destructive text-xs">{errors.email.message}</p>
					)}
					{fieldErrors.email && (
						<p className="text-destructive text-xs">{fieldErrors.email}</p>
					)}
				</div>
				<div className="space-y-2">
					<label htmlFor="password" className="block text-sm font-medium">
						{t("register.password")}
					</label>
					<input
						id="password"
						type="password"
						placeholder={t(
							"register.passwordPlaceholder",
							"비밀번호를 입력하세요",
						)}
						{...formRegister("password")}
						className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary ${
							errors.password || fieldErrors.password
								? "border-destructive"
								: ""
						}`}
						autoComplete="new-password"
					/>
					{errors.password && (
						<p className="text-destructive text-xs">
							{errors.password.message}
						</p>
					)}
					{fieldErrors.password && (
						<p className="text-destructive text-xs">{fieldErrors.password}</p>
					)}
				</div>
				<div className="space-y-2">
					<label
						htmlFor="passwordConfirm"
						className="block text-sm font-medium"
					>
						{t("register.passwordConfirm")}
					</label>
					<input
						id="passwordConfirm"
						type="password"
						placeholder={t(
							"register.passwordConfirmPlaceholder",
							"비밀번호를 다시 입력하세요",
						)}
						{...formRegister("passwordConfirm")}
						className={`w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:border-primary ${
							errors.passwordConfirm || fieldErrors.passwordConfirm
								? "border-destructive"
								: ""
						}`}
						autoComplete="new-password"
					/>
					{errors.passwordConfirm && (
						<p className="text-destructive text-xs">
							{errors.passwordConfirm.message}
						</p>
					)}
					{fieldErrors.passwordConfirm && (
						<p className="text-destructive text-xs">
							{fieldErrors.passwordConfirm}
						</p>
					)}
				</div>
				<form onSubmit={handleSubmit(onSubmit)} className="w-full">
					<LoadingButton
						type="submit"
						loading={loading}
						className="w-full mt-4 h-12 text-base font-semibold"
					>
						{t("register.submit")}
					</LoadingButton>
				</form>
			</div>

			{/* 성공 모달 */}
			<SuccessModal
				isOpen={showSuccessModal}
				onClose={() => setShowSuccessModal(false)}
				title={t("register.success.title")}
				message={t("register.success.message")}
				confirmText={t("register.success.homeButton")}
				onConfirm={handleSuccessModalConfirm}
				allowBackdropClose={false}
			/>
		</div>
	);
}
