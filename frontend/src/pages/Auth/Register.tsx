import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import LoadingButton from "@/components/common/LoadingButton";
import { useTranslation } from "react-i18next";
import { useCurrentLanguage } from "@/store/languageStore";
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
	Kanban,
	Zap,
	Users,
	Info,
	ChevronDown
} from "lucide-react";

// 단계별 상태 타입
type Step = 'step1' | 'step2' | 'welcome';

// 각 단계별 폼 데이터 타입
interface Step1Data {
	email: string;
	password: string;
	passwordConfirm: string;
}

interface Step2Data {
	firstName: string;
	lastName: string;
	language: string;
}

export default function Register() {
	const { t } = useTranslation("common");
	const currentLanguage = useCurrentLanguage();
	const [currentStep, setCurrentStep] = useState<Step>('step1');
	const [step1Data, setStep1Data] = useState<Step1Data | null>(null);
	const [_step2Data, setStep2Data] = useState<Step2Data | null>(null);
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

	// Step 1 스키마
	const step1Schema = z
		.object({
			email: z.string().email(t("register.validation.email", "올바른 이메일 주소를 입력하세요")),
			password: z.string().min(8, t("register.validation.password", "비밀번호는 8자 이상이어야 합니다")),
			passwordConfirm: z.string(),
		})
		.refine((data: Step1Data) => data.password === data.passwordConfirm, {
			message: t("register.validation.passwordConfirm", "비밀번호가 일치하지 않습니다"),
			path: ["passwordConfirm"],
		});

	// Step 2 스키마
	const step2Schema = z.object({
		firstName: z.string().min(1, t("register.validation.firstName", "이름을 입력하세요")),
		lastName: z.string().min(1, t("register.validation.lastName", "성을 입력하세요")),
		language: z.string().min(1, t("register.validation.language", "언어를 선택하세요")),
	});

	const step1Form = useForm<Step1Data>({
		resolver: zodResolver(step1Schema),
	});

	const step2Form = useForm<Step2Data>({
		resolver: zodResolver(step2Schema),
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

	const onStep1Submit = async (data: Step1Data) => {
		setStep1Data(data);
		setCurrentStep('step2');
	};

	const onStep2Submit = async (data: Step2Data) => {
		if (!step1Data) return;
		
		setStep2Data(data);
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
				email: step1Data.email,
				password: step1Data.password,
				firstName: data.firstName,
				lastName: data.lastName,
			};

			await handle(registerUser(registerData), {
				201() {
					setCurrentStep('welcome');
				},
				409(error) {
					setFieldErrors((prev) => ({
						...prev,
						email: error.message || t("register.emailAlreadyExists", "이미 가입된 이메일입니다."),
					}));
					setCurrentStep('step1');
				},
				422(error) {
					const errorData = error.details;
					if (errorData) {
						setFieldErrors((prev) => ({
							...prev,
							...handleFieldErrors(errorData),
						}));
					} else {
						setError(t("register.validationError", "입력 정보를 확인해주세요."));
					}
					setCurrentStep('step1');
				},
				500(error) {
					setError(error.message || t("register.serverError", "서버 오류가 발생했습니다."));
					setCurrentStep('step1');
				},
			});
		} catch (e: unknown) {
			log.error("회원가입 오류:", e);
			const errorMessage = e instanceof Error ? e.message : t("register.error");
			setError(errorMessage);
			setCurrentStep('step1');
		} finally {
			setLoading(false);
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

	const handleBack = () => {
		if (currentStep === 'step2') {
			setCurrentStep('step1');
		} else {
			navigate("/");
		}
	};

	const handleSuccessModalConfirm = () => {
		navigate("/dashboard");
	};

	const renderStep1 = () => (
		<div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex">
			{/* 메인 컨텐츠 */}
			<div className="flex-1 flex items-center justify-center p-8">
				<div className="w-full max-w-md">
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
							{t("register.hasAccount", "이미 계정이 있으신가요?")} <button type="button" className="text-blue-600 font-medium">{t("register.login", "로그인")}</button>
						</div>
					</div>

					{/* 단계 표시기 */}
					<div className="flex items-center gap-4 mb-8">
						<div className="bg-blue-600 text-white w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium">1</div>
						<div className="flex-1 h-1 bg-slate-200 rounded"></div>
						<div className="bg-slate-200 text-slate-500 w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium">2</div>
					</div>

					{/* 메인 폼 */}
					<div className="bg-white rounded-2xl p-8 shadow-lg">
						<h1 className="text-3xl font-bold text-slate-900 text-center mb-2">
							{t("register.step1.title", "계정 만들기")}
						</h1>
						<p className="text-slate-600 text-center mb-8">
							{t("register.step1.subtitle", "몇 분 안에 첫 번째 보드를 만들어보세요")}
						</p>

						{/* 보안 안내 */}
						<div className="bg-gradient-to-r from-green-50 to-blue-50 border border-green-200 rounded-lg p-4 mb-6">
							<div className="flex items-start gap-3">
								<Shield className="w-4 h-4 text-green-600 mt-0.5" />
								<p className="text-green-800 text-sm font-medium">
									{t("register.step1.securityTip", "안전한 계정을 위해 강력한 비밀번호를 설정해주세요.")}
								</p>
							</div>
						</div>

						{error && (
							<div className="bg-red-50 text-red-600 px-4 py-3 rounded-lg text-sm mb-6">
								{error}
							</div>
						)}

						<form onSubmit={step1Form.handleSubmit(onStep1Submit)} className="space-y-6">
							{/* 이메일 */}
							<div>
								<label htmlFor="step1-email" className="block text-sm font-medium text-slate-700 mb-2">
									{t("register.email", "이메일 주소")} *
								</label>
								<div className="relative">
									<Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
									<input
										id="step1-email"
										type="email"
										placeholder={t("register.emailPlaceholder", "your@email.com")}
										{...step1Form.register("email")}
										className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
											step1Form.formState.errors.email || fieldErrors.email ? "border-red-300" : "border-slate-300"
										}`}
									/>
								</div>
								{step1Form.formState.errors.email && (
									<p className="text-red-500 text-xs mt-1">{step1Form.formState.errors.email.message}</p>
								)}
								{fieldErrors.email && (
									<p className="text-red-500 text-xs mt-1">{fieldErrors.email}</p>
								)}
								<p className="text-slate-500 text-xs mt-1">
									{t("register.emailHelp", "로그인과 중요한 알림을 받을 이메일 주소입니다")}
								</p>
							</div>

							{/* 비밀번호 */}
							<div>
								<label htmlFor="step1-password" className="block text-sm font-medium text-slate-700 mb-2">
									{t("register.password", "비밀번호")} *
								</label>
								<div className="relative">
									<Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
									<input
										id="step1-password"
										type="password"
										placeholder={t("register.passwordPlaceholder", "안전한 비밀번호를 입력하세요")}
										{...step1Form.register("password")}
										className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
											step1Form.formState.errors.password ? "border-red-300" : "border-slate-300"
										}`}
									/>
								</div>
								{step1Form.formState.errors.password && (
									<p className="text-red-500 text-xs mt-1">{step1Form.formState.errors.password.message}</p>
								)}
							</div>

							{/* 비밀번호 확인 */}
							<div>
								<label htmlFor="step1-passwordConfirm" className="block text-sm font-medium text-slate-700 mb-2">
									{t("register.passwordConfirm", "비밀번호 확인")} *
								</label>
								<div className="relative">
									<Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
									<input
										id="step1-passwordConfirm"
										type="password"
										placeholder={t("register.passwordConfirmPlaceholder", "비밀번호를 다시 입력하세요")}
										{...step1Form.register("passwordConfirm")}
										className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
											step1Form.formState.errors.passwordConfirm ? "border-red-300" : "border-slate-300"
										}`}
									/>
								</div>
								{step1Form.formState.errors.passwordConfirm && (
									<p className="text-red-500 text-xs mt-1">{step1Form.formState.errors.passwordConfirm.message}</p>
								)}
							</div>

							<button
								type="submit"
								className="w-full bg-slate-400 text-white py-3 rounded-lg font-medium hover:bg-slate-500 transition-colors"
							>
								{t("register.nextStep", "다음 단계")}
							</button>
						</form>

						<p className="text-center text-sm text-slate-500 mt-6">
							{t("register.terms1", "계정을 만들면")} <button type="button" className="text-blue-600">{t("register.termsOfService", "이용약관")}</button> {t("register.and", "및")} <button type="button" className="text-blue-600">{t("register.privacyPolicy", "개인정보처리방침")}</button> {t("register.terms2", "에 동의하는 것으로 간주됩니다.")}
						</p>
					</div>
				</div>
			</div>

			{/* 사이드바 */}
			<div className="hidden lg:block w-96 bg-white/60 backdrop-blur-sm p-8">
				<div className="bg-white rounded-2xl p-6 shadow-lg h-full">
					<h3 className="text-lg font-bold text-slate-900 text-center mb-6">
						{t("register.features.title", "Boardly와 함께하면")}
					</h3>
					<div className="space-y-4">
						<div className="flex items-start gap-3">
							<Zap className="w-4 h-4 text-blue-600 mt-1" />
							<p className="text-slate-600 text-sm">{t("register.features.dragDrop", "드래그 앤 드롭으로 빠른 작업 관리")}</p>
						</div>
						<div className="flex items-start gap-3">
							<Shield className="w-4 h-4 text-green-600 mt-1" />
							<p className="text-slate-600 text-sm">{t("register.features.security", "안전한 데이터 보호와 백업")}</p>
						</div>
						<div className="flex items-start gap-3">
							<Globe className="w-4 h-4 text-purple-600 mt-1" />
							<p className="text-slate-600 text-sm">{t("register.features.cloud", "어디서든 접속 가능한 클라우드 기반")}</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	);

	const renderStep2 = () => (
		<div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50 flex items-center justify-center p-8">
			<div className="w-full max-w-md">
				{/* 헤더 */}
				<div className="flex items-center justify-between mb-8">
					<button
						type="button"
						onClick={handleBack}
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
						{t("register.hasAccount", "이미 계정이 있으신가요?")} <button type="button" className="text-blue-600 font-medium">{t("register.login", "로그인")}</button>
					</div>
				</div>

				{/* 단계 표시기 */}
				<div className="flex items-center gap-4 mb-8">
					<div className="bg-blue-600 text-white w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium">1</div>
					<div className="flex-1 h-1 bg-blue-600 rounded"></div>
					<div className="bg-blue-600 text-white w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium">2</div>
				</div>

				{/* 메인 폼 */}
				<div className="bg-white rounded-2xl p-8 shadow-lg">
					<h1 className="text-3xl font-bold text-slate-900 text-center mb-2">
						{t("register.step2.title", "프로필 정보")}
					</h1>
					<p className="text-slate-600 text-center mb-6">
						{t("register.step2.subtitle", "마지막 단계입니다. 기본 정보를 입력해주세요")}
					</p>

					{/* 안내 메시지 */}
					<div className="bg-gradient-to-r from-blue-50 to-purple-50 border border-blue-200 rounded-lg p-4 mb-6">
						<div className="flex items-start gap-3">
							<Info className="w-4 h-4 text-blue-600 mt-0.5" />
							<p className="text-blue-800 text-sm font-medium">
								{t("register.step2.almostDone", "거의 다 왔어요! 마지막으로 기본 정보를 입력해주세요.")}
							</p>
						</div>
					</div>

					<form onSubmit={step2Form.handleSubmit(onStep2Submit)} className="space-y-6">
						{/* 성 */}
						<div>
							<label htmlFor="step2-lastName" className="block text-sm font-medium text-slate-700 mb-2">
								{t("register.lastName", "성 (Last Name)")} *
							</label>
							<div className="relative">
								<User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
								<input
									id="step2-lastName"
									type="text"
									placeholder={t("register.lastNamePlaceholder", "예: 김, 이, 박, Smith, Johnson")}
									{...step2Form.register("lastName")}
									className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
										step2Form.formState.errors.lastName ? "border-red-300" : "border-slate-300"
									}`}
								/>
							</div>
							{step2Form.formState.errors.lastName && (
								<p className="text-red-500 text-xs mt-1">{step2Form.formState.errors.lastName.message}</p>
							)}
							<p className="text-slate-500 text-xs mt-1">
								{t("register.nameHelp", "한글 또는 영문만 입력 가능합니다 (1-50자)")}
							</p>
						</div>

						{/* 이름 */}
						<div>
							<label htmlFor="step2-firstName" className="block text-sm font-medium text-slate-700 mb-2">
								{t("register.firstName", "이름 (First Name)")} *
							</label>
							<div className="relative">
								<User className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
								<input
									id="step2-firstName"
									type="text"
									placeholder={t("register.firstNamePlaceholder", "예: 개발, 기획, 학생, John, Jane")}
									{...step2Form.register("firstName")}
									className={`w-full pl-10 pr-4 py-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
										step2Form.formState.errors.firstName ? "border-red-300" : "border-slate-300"
									}`}
								/>
							</div>
							{step2Form.formState.errors.firstName && (
								<p className="text-red-500 text-xs mt-1">{step2Form.formState.errors.firstName.message}</p>
							)}
							<p className="text-slate-500 text-xs mt-1">
								{t("register.nameHelp", "한글 또는 영문만 입력 가능합니다 (1-50자)")}
							</p>
						</div>

						{/* 언어 설정 */}
						<div>
							<label htmlFor="step2-language" className="block text-sm font-medium text-slate-700 mb-2">
								{t("register.language", "기본 언어 설정")}
							</label>
							<div className="relative">
								<Globe className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" />
								<select
									id="step2-language"
									{...step2Form.register("language")}
									className="w-full pl-10 pr-10 py-3 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none"
								>
									<option value="ko">🇰🇷 한국어 (Korean)</option>
									<option value="en">🇺🇸 English</option>
								</select>
								<ChevronDown className="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400 pointer-events-none" />
							</div>
							<p className="text-slate-500 text-xs mt-1">
								{t("register.languageHelp", "언제든지 설정에서 변경할 수 있습니다")}
							</p>
						</div>

						{/* 개인정보 처리 안내 */}
						<div className="bg-slate-50 rounded-lg p-4">
							<h4 className="font-medium text-slate-900 mb-3">
								{t("register.privacy.title", "개인정보 수집 및 이용 안내")}
							</h4>
							<div className="space-y-2 text-sm text-slate-600">
								<p>• {t("register.privacy.items", "수집 항목: 이메일, 이름, 언어 설정")}</p>
								<p>• {t("register.privacy.purpose", "수집 목적: 서비스 제공, 계정 관리, 고객 지원")}</p>
								<p>• {t("register.privacy.retention", "보유 기간: 회원 탈퇴 시까지")}</p>
							</div>
							<p className="text-xs text-slate-500 mt-2">
								{t("register.privacy.consent", "가입을 완료하면 개인정보 처리에 동의하는 것으로 간주됩니다.")}
							</p>
						</div>

						<div className="flex gap-4">
							<button
								type="button"
								onClick={handleBack}
								className="flex-1 bg-white border border-slate-300 text-slate-700 py-3 rounded-lg font-medium hover:bg-slate-50 transition-colors"
							>
								{t("register.previous", "이전")}
							</button>
							<LoadingButton
								type="submit"
								loading={loading}
								className="flex-1 bg-slate-400 text-white py-3 rounded-lg font-medium hover:bg-slate-500 transition-colors"
							>
								{t("register.createAccount", "계정 만들기")}
							</LoadingButton>
						</div>
					</form>

					<p className="text-center text-sm text-slate-500 mt-6">
						{t("register.terms1", "계정을 만들면")} <button type="button" className="text-blue-600">{t("register.termsOfService", "이용약관")}</button> {t("register.and", "및")} <button type="button" className="text-blue-600">{t("register.privacyPolicy", "개인정보처리방침")}</button> {t("register.terms2", "에 동의하는 것으로 간주됩니다.")}
					</p>
				</div>
			</div>
		</div>
	);

	const renderWelcome = () => (
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

					<div className="space-y-4 mb-8">
						<button
							type="button"
							onClick={handleSuccessModalConfirm}
							className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white py-3 rounded-lg font-medium hover:from-blue-700 hover:to-purple-700 transition-colors"
						>
							{t("register.welcome.createBoard", "첫 번째 보드 만들기")}
						</button>
						<button
							type="button"
							onClick={() => navigate("/dashboard")}
							className="w-full bg-white border border-slate-300 text-slate-700 py-3 rounded-lg font-medium hover:bg-slate-50 transition-colors"
						>
							{t("register.welcome.exploreDashboard", "대시보드 둘러보기")}
						</button>
					</div>

					{/* 다음 단계 가이드 */}
					<div className="bg-white/80 backdrop-blur-sm rounded-xl p-6 mb-6">
						<h3 className="font-semibold text-slate-900 mb-4">
							{t("register.welcome.nextSteps", "다음 단계")}
						</h3>
						<div className="space-y-3 text-left">
							<div className="flex items-center gap-3">
								<div className="bg-blue-100 text-blue-600 w-6 h-6 rounded-xl flex items-center justify-center text-xs font-bold">1</div>
								<span className="text-sm text-slate-600">{t("register.welcome.step1", "첫 번째 보드 생성하기")}</span>
							</div>
							<div className="flex items-center gap-3">
								<div className="bg-purple-100 text-purple-600 w-6 h-6 rounded-xl flex items-center justify-center text-xs font-bold">2</div>
								<span className="text-sm text-slate-600">{t("register.welcome.step2", "리스트와 카드 추가하기")}</span>
							</div>
							<div className="flex items-center gap-3">
								<div className="bg-green-100 text-green-600 w-6 h-6 rounded-xl flex items-center justify-center text-xs font-bold">3</div>
								<span className="text-sm text-slate-600">{t("register.welcome.step3", "드래그 앤 드롭으로 작업 관리하기")}</span>
							</div>
						</div>
					</div>

					{/* 기능 소개 */}
					<div className="bg-white/60 backdrop-blur-sm rounded-xl p-6 mb-6">
						<h3 className="font-semibold text-slate-900 mb-4">
							{t("register.welcome.readyToStart", "시작할 준비가 되었습니다")}
						</h3>
						<div className="grid grid-cols-2 gap-4">
							<div className="text-center">
								<div className="bg-gradient-to-r from-blue-600 to-blue-700 w-10 h-10 rounded-lg flex items-center justify-center mx-auto mb-2">
									<Kanban className="w-5 h-5 text-white" />
								</div>
								<p className="text-xs text-slate-600">{t("register.welcome.kanban", "칸반 보드")}</p>
							</div>
							<div className="text-center">
								<div className="bg-gradient-to-r from-green-500 to-green-600 w-10 h-10 rounded-lg flex items-center justify-center mx-auto mb-2">
									<Zap className="w-5 h-5 text-white" />
								</div>
								<p className="text-xs text-slate-600">{t("register.welcome.quickMove", "빠른 이동")}</p>
							</div>
							<div className="text-center">
								<div className="bg-gradient-to-r from-purple-600 to-purple-700 w-10 h-10 rounded-lg flex items-center justify-center mx-auto mb-2">
									<Users className="w-5 h-5 text-white" />
								</div>
								<p className="text-xs text-slate-600">{t("register.welcome.teamWork", "팀 협업")}</p>
							</div>
							<div className="text-center">
								<div className="bg-gradient-to-r from-orange-500 to-orange-600 w-10 h-10 rounded-lg flex items-center justify-center mx-auto mb-2">
									<Shield className="w-5 h-5 text-white" />
								</div>
								<p className="text-xs text-slate-600">{t("register.welcome.security", "안전한 보안")}</p>
							</div>
						</div>
					</div>

					<p className="text-sm text-slate-500">
						{t("register.welcome.needHelp", "도움이 필요하시면")} <button type="button" className="text-blue-600">{t("register.welcome.helpCenter", "도움말 센터")}</button>{t("register.welcome.checkIt", "를 확인해보세요")}
					</p>
				</div>
			</div>
		</div>
	);

	if (currentStep === 'step1') return renderStep1();
	if (currentStep === 'step2') return renderStep2();
	if (currentStep === 'welcome') return renderWelcome();

	return null;
}
