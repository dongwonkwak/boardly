import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import LoadingButton from "@/components/common/LoadingButton";
import { useTranslation } from "react-i18next";
import { useCurrentLanguage, useLanguageStore } from "@/store/languageStore";
import { useNavigate } from "react-router-dom";
import { registerUser, type FieldViolation } from "@/services/api/client";
import { handle } from "@oazapfts/runtime";
import log from "@/utils/logger";

import { 
	ArrowLeft, 
	Mail, 
	Lock, 
	User, 
	Globe, 
	Shield, 
	CheckCircle,
} from "lucide-react";

// 통합된 폼 데이터 타입
interface RegisterFormData {
	email: string;
	lastName: string;
	firstName: string;
	password: string;
	passwordConfirm: string;
	language: string;
}

export default function Register() {
	const { t } = useTranslation("common");
	const currentLanguage = useCurrentLanguage();
	const { changeLanguage } = useLanguageStore();
	const [showWelcome, setShowWelcome] = useState(false);
	const [error, setError] = useState("");
	const [fieldErrors, setFieldErrors] = useState({
		email: "",
		lastName: "",
		firstName: "",
		password: "",
		passwordConfirm: "",
	});
	const [loading, setLoading] = useState(false);
	const navigate = useNavigate();

	// 통합된 폼 스키마
	const registerSchema = z
		.object({
			email: z.string().email(t("register.validation.email", "올바른 이메일 주소를 입력하세요")),
			lastName: z.string().min(1, t("register.validation.lastName", "성을 입력하세요")),
			firstName: z.string().min(1, t("register.validation.firstName", "이름을 입력하세요")),
			password: z.string().min(8, t("register.validation.password", "비밀번호는 8자 이상이어야 합니다")),
			passwordConfirm: z.string(),
			language: z.string().min(1, t("register.validation.language", "언어를 선택하세요")),
		})
		.refine((data: RegisterFormData) => data.password === data.passwordConfirm, {
			message: t("register.validation.passwordConfirm", "비밀번호가 일치하지 않습니다"),
			path: ["passwordConfirm"],
		});

	const form = useForm<RegisterFormData>({
		resolver: zodResolver(registerSchema),
		defaultValues: {
			language: currentLanguage
		}
	});

	// 브라우저 뒤로가기 방지
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

	const onSubmit = async (data: RegisterFormData) => {
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
			const registerData = {
				email: data.email,
				password: data.password,
				firstName: data.firstName,
				lastName: data.lastName,
			};

			await handle(registerUser(registerData), {
				201() {
					setShowWelcome(true);
				},
				409(error: { message?: string }) {
					setFieldErrors((prev) => ({
						...prev,
						email: error.message || t("register.emailAlreadyExists", "이미 가입된 이메일입니다."),
					}));
				},
				400(error: { details?: FieldViolation[]; message?: string }) {
					const errorData = error.details;
					if (errorData && errorData.length > 0) {
						setFieldErrors((prev) => ({
							...prev,
							...handleFieldErrors(errorData),
						}));
					} else {
						setError(error.message || t("register.validationError", "입력 정보를 확인해주세요."));
					}
				},
				500(error: { message?: string }) {
					setError(error.message || t("register.serverError", "서버 오류가 발생했습니다."));
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

	const handleFieldErrors = (errorData: FieldViolation[]) => {
		const newErrors: Record<string, string> = {};
		errorData.forEach((err) => {
			if (err.field && err.message) {
				newErrors[err.field] = err.message;
			}
		});
		return newErrors;
	};

	const handleSuccessModalConfirm = () => {
		navigate("/dashboard");
	};

	if (showWelcome) {
		return (
			<div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center p-8">
				<div className="w-full max-w-lg">
					<div className="bg-white rounded-2xl p-8 shadow-lg text-center">
						{/* 성공 아이콘 */}
						<div className="relative mb-6">
							<div className="bg-gradient-to-r from-green-500 to-green-600 w-20 h-20 rounded-full flex items-center justify-center mx-auto">
								<CheckCircle className="w-10 h-10 text-white" />
							</div>
							<div className="absolute -top-2 -right-2 bg-yellow-400 w-6 h-6 rounded-full flex items-center justify-center">
								<span className="text-sm">🎉</span>
							</div>
						</div>

						<h1 className="text-3xl font-bold text-slate-900 mb-4">
							{t("register.welcome.title", "환영합니다!")}
						</h1>
						<p className="text-slate-600 mb-8">
							{t("register.welcome.message", "Boardly 가입이 완료되었습니다.\n이제 프로젝트 관리를 더 스마트하게 시작해보세요!")}
						</p>

						<button
							type="button"
							onClick={handleSuccessModalConfirm}
							className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 rounded-lg font-medium hover:from-blue-700 hover:to-purple-700 transition-colors mb-6"
						>
							{t("register.welcome.createBoard", "첫 번째 보드 만들기")}
						</button>

						{/* 기능 소개 */}
						<div className="bg-slate-50/50 backdrop-blur-sm rounded-xl p-6">
							<h3 className="font-semibold text-slate-900 mb-4 flex items-center justify-center gap-2">
								<span>✨</span> {t("register.welcome.readyToStart", "Boardly와 함께 시작하세요")}
							</h3>
							<div className="space-y-3 text-left">
								<div className="flex items-center gap-3">
									<CheckCircle className="w-4 h-4 text-green-600" />
									<span className="text-sm text-slate-600">{t("register.welcome.feature1", "무제한 개인 보드 생성")}</span>
								</div>
								<div className="flex items-center gap-3">
									<CheckCircle className="w-4 h-4 text-green-600" />
									<span className="text-sm text-slate-600">{t("register.welcome.feature2", "직관적인 드래그 앤 드롭")}</span>
								</div>
								<div className="flex items-center gap-3">
									<CheckCircle className="w-4 h-4 text-green-600" />
									<span className="text-sm text-slate-600">{t("register.welcome.feature3", "실시간 동기화 및 자동 저장")}</span>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}

	return (
		<div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex">
			{/* 메인 컨텐츠 */}
			<div className="flex-1 flex items-center justify-center p-8">
				<div className="w-full max-w-lg">
					{/* 헤더 */}
					<div className="flex items-center justify-between mb-8">
						<button
							type="button"
							onClick={() => navigate("/")}
							className="flex items-center text-slate-500 hover:text-slate-700"
						>
							<ArrowLeft className="w-4 h-4" />
						</button>
						<div className="flex items-center gap-2">
							<div className="bg-gradient-to-r from-blue-600 to-purple-600 p-2 rounded-lg">
								<span className="text-white font-bold text-sm">B</span>
							</div>
							<span className="font-bold text-xl text-slate-800">Boardly</span>
						</div>
						<div className="text-sm text-slate-500">
							{t("register.hasAccount", "이미 계정이 있으신가요?")} {" "}
							<button 
								type="button" 
								onClick={() => navigate("/login")}
								className="text-blue-600 font-medium hover:underline"
							>
								{t("register.login", "로그인")}
							</button>
						</div>
					</div>

					{/* 메인 폼 */}
					<div className="bg-white rounded-2xl p-8 shadow-lg">
						<h1 className="text-3xl font-bold text-slate-900 text-center mb-2">
							{t("register.title", "계정 만들기")}
						</h1>
						<p className="text-slate-600 text-center mb-6">
							{t("register.subtitle", "몇 분 안에 첫 번째 보드를 만들어보세요")}
						</p>

						{/* 보안 안내 */}
						<div className="bg-gradient-to-r from-green-50 to-blue-50 border border-green-200 rounded-lg p-4 mb-6">
							<div className="flex items-start gap-3">
								<Shield className="w-4 h-4 text-green-600 mt-0.5" />
								<p className="text-green-800 text-sm font-medium">
									{t("register.securityTip", "안전한 계정을 위해 강력한 비밀번호를 설정해주세요.")}
								</p>
							</div>
						</div>

						{error && (
							<div className="bg-red-50 text-red-600 px-4 py-3 rounded-lg text-sm mb-6">
								{error}
							</div>
						)}

						<form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
							{/* 이메일 */}
							<div>
								<label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-2">
									{t("register.email", "이메일 주소")} *
								</label>
								<div className="relative">
									<Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
									<input
										id="email"
										type="email"
										placeholder={t("register.emailPlaceholder", "your@email.com")}
										{...form.register("email")}
										className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
											form.formState.errors.email || fieldErrors.email ? "border-red-300" : "border-slate-300"
										}`}
									/>
								</div>
								{form.formState.errors.email && (
									<p className="text-red-500 text-xs mt-1">{form.formState.errors.email.message}</p>
								)}
								{fieldErrors.email && (
									<p className="text-red-500 text-xs mt-1">{fieldErrors.email}</p>
								)}
								<p className="text-slate-500 text-xs mt-1">
									{t("register.emailHelp", "로그인과 중요한 알림을 받을 이메일 주소입니다")}
								</p>
							</div>

							{/* 이름과 성 (2열 레이아웃) */}
							<div className="grid grid-cols-2 gap-4">
								{/* 이름 */}
								<div>
									<label htmlFor="firstName" className="block text-sm font-medium text-slate-700 mb-2">
										{t("register.firstName", "이름 (First Name)")} *
									</label>
									<div className="relative">
										<User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
										<input
											id="firstName"
											type="text"
											placeholder={t("register.firstNamePlaceholder", "예: 개발, 기획, John")}
											{...form.register("firstName")}
											className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
												form.formState.errors.firstName || fieldErrors.firstName ? "border-red-300" : "border-slate-300"
											}`}
										/>
									</div>
									{form.formState.errors.firstName && (
										<p className="text-red-500 text-xs mt-1">{form.formState.errors.firstName.message}</p>
									)}
									{fieldErrors.firstName && (
										<p className="text-red-500 text-xs mt-1">{fieldErrors.firstName}</p>
									)}
									<p className="text-slate-500 text-xs mt-1">
										{t("register.nameHelp", "한글 또는 영문만 (1-50자)")}
									</p>
								</div>

								{/* 성 */}
								<div>
									<label htmlFor="lastName" className="block text-sm font-medium text-slate-700 mb-2">
										{t("register.lastName", "성 (Last Name)")} *
									</label>
									<div className="relative">
										<User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
										<input
											id="lastName"
											type="text"
											placeholder={t("register.lastNamePlaceholder", "예: 김, 이, Smith")}
											{...form.register("lastName")}
											className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
												form.formState.errors.lastName || fieldErrors.lastName ? "border-red-300" : "border-slate-300"
											}`}
										/>
									</div>
									{form.formState.errors.lastName && (
										<p className="text-red-500 text-xs mt-1">{form.formState.errors.lastName.message}</p>
									)}
									{fieldErrors.lastName && (
										<p className="text-red-500 text-xs mt-1">{fieldErrors.lastName}</p>
									)}
									<p className="text-slate-500 text-xs mt-1">
										{t("register.nameHelp", "한글 또는 영문만 (1-50자)")}
									</p>
								</div>
							</div>

							{/* 비밀번호와 비밀번호 확인 (2열 레이아웃) */}
							<div className="grid grid-cols-2 gap-4">
								{/* 비밀번호 */}
								<div>
									<label htmlFor="password" className="block text-sm font-medium text-slate-700 mb-2">
										{t("register.password", "비밀번호")} *
									</label>
									<div className="relative">
										<Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
										<input
											id="password"
											type="password"
											placeholder={t("register.passwordPlaceholder", "안전한 비밀번호")}
											{...form.register("password")}
											className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
												form.formState.errors.password || fieldErrors.password ? "border-red-300" : "border-slate-300"
											}`}
										/>
									</div>
									{form.formState.errors.password && (
										<p className="text-red-500 text-xs mt-1">{form.formState.errors.password.message}</p>
									)}
									{fieldErrors.password && (
										<p className="text-red-500 text-xs mt-1">{fieldErrors.password}</p>
									)}
								</div>

								{/* 비밀번호 확인 */}
								<div>
									<label htmlFor="passwordConfirm" className="block text-sm font-medium text-slate-700 mb-2">
										{t("register.passwordConfirm", "비밀번호 확인")} *
									</label>
									<div className="relative">
										<Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
										<input
											id="passwordConfirm"
											type="password"
											placeholder={t("register.passwordConfirmPlaceholder", "비밀번호 재입력")}
											{...form.register("passwordConfirm")}
											className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
												form.formState.errors.passwordConfirm || fieldErrors.passwordConfirm ? "border-red-300" : "border-slate-300"
											}`}
										/>
									</div>
									{form.formState.errors.passwordConfirm && (
										<p className="text-red-500 text-xs mt-1">{form.formState.errors.passwordConfirm.message}</p>
									)}
									{fieldErrors.passwordConfirm && (
										<p className="text-red-500 text-xs mt-1">{fieldErrors.passwordConfirm}</p>
									)}
								</div>
							</div>

							{/* 언어 설정 */}
							<div>
								<label htmlFor="language" className="block text-sm font-medium text-slate-700 mb-2">
									{t("register.language", "기본 언어 설정")}
								</label>
								<div className="relative">
									<Globe className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
									<select
										id="language"
										{...form.register("language")}
										onChange={(e) => {
											form.setValue("language", e.target.value);
											changeLanguage(e.target.value as "ko" | "en");
										}}
										className="w-full pl-10 pr-4 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
									>
										<option value="ko">🇰🇷 한국어</option>
										<option value="en">🇺🇸 English</option>
									</select>
								</div>
								<p className="text-slate-500 text-xs mt-1">
									{t("register.languageHelp", "언제든지 설정에서 변경할 수 있습니다")}
								</p>
							</div>

							{/* 버튼들 */}
							<div className="flex gap-4">
								<button
									type="button"
									onClick={() => navigate("/")}
									className="flex-1 h-11 bg-slate-100 border border-slate-300 text-slate-700 rounded-lg font-medium hover:bg-slate-200 transition-colors flex items-center justify-center gap-2"
								>
									<ArrowLeft className="w-4 h-4" />
									{t("register.previous", "이전")}
								</button>
								<LoadingButton
									type="submit"
									loading={loading}
									className="flex-1 h-11 bg-slate-400 text-white rounded-lg font-medium hover:bg-slate-500 transition-colors disabled:bg-slate-300"
								>
									{t("register.createAccount", "회원가입")}
								</LoadingButton>
							</div>
						</form>

						{/* 개인정보 보호 안내 */}
						<div className="bg-slate-50 rounded-lg p-4 mt-6">
							<h4 className="font-medium text-slate-900 mb-3">
								{t("register.privacy.title", "개인정보 보호 안내")}
							</h4>
							<div className="space-y-1 text-sm text-slate-600">
								<p><strong>{t("register.privacy.purpose", "수집 목적:")}</strong> {t("register.privacy.purposeText", "계정 생성, 서비스 제공, 고객 지원")}</p>
								<p><strong>{t("register.privacy.retention", "보관 기간:")}</strong> {t("register.privacy.retentionText", "계정 삭제 시까지 (법정 보관 의무 제외)")}</p>
								<p><strong>{t("register.privacy.thirdParty", "제3자 제공:")}</strong> {t("register.privacy.thirdPartyText", "법령에 의한 경우를 제외하고 제공하지 않습니다")}</p>
							</div>
							<p className="text-xs text-slate-500 mt-2">
								{t("register.privacy.consent", "회원가입 시 개인정보 처리방침에 동의한 것으로 간주됩니다.")}
							</p>
						</div>

						{/* 약관 동의 */}
						<p className="text-center text-sm text-slate-500 mt-4">
							{t("register.terms1", "회원가입을 진행하시면 Boardly의")} {" "}
							<button type="button" className="text-blue-600 hover:underline">{t("register.termsOfService", "이용약관")}</button>
							{t("register.and", "과")} {" "}
							<button type="button" className="text-blue-600 hover:underline">{t("register.privacyPolicy", "개인정보 처리방침")}</button> 
							{t("register.terms2", "에 동")}
							<br />
							{t("register.terms3", "의하는 것으로 간주됩니다.")}
						</p>
					</div>
				</div>
			</div>

			{/* 사이드바 */}
			<div className="hidden lg:block w-96 bg-white/60 backdrop-blur-sm p-8">
				<div className="bg-white/80 backdrop-blur-sm rounded-2xl p-6 shadow-lg h-full">
					{/* 하단 오버레이 스타일 */}
					<div className="bg-white/50 backdrop-blur-sm rounded-xl p-6 h-full flex flex-col">
						<h3 className="text-lg font-bold text-slate-900 text-center mb-6 flex items-center justify-center gap-2">
							<span>✨</span> {t("register.features.title", "Boardly와 함께 시작하세요")}
						</h3>
						<div className="space-y-4 flex-1">
							<div className="flex items-start gap-3">
								<CheckCircle className="w-4 h-4 text-slate-600 mt-1" />
								<p className="text-slate-600 text-sm">{t("register.features.boards", "무제한 개인 보드 생성")}</p>
							</div>
							<div className="flex items-start gap-3">
								<CheckCircle className="w-4 h-4 text-slate-600 mt-1" />
								<p className="text-slate-600 text-sm">{t("register.features.dragDrop", "직관적인 드래그 앤 드롭")}</p>
							</div>
							<div className="flex items-start gap-3">
								<CheckCircle className="w-4 h-4 text-slate-600 mt-1" />
								<p className="text-slate-600 text-sm">{t("register.features.sync", "실시간 동기화 및 자동 저장")}</p>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	);
}
