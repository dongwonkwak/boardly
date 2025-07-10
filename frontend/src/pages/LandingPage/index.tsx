import { useTranslation } from "react-i18next";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { useOAuth } from "@/hooks/useAuth";
import Navbar from "@/components/layout/Navbar";
import Footer from "@/components/layout/Footer";

export default function LandingPage() {
	const { t } = useTranslation("common");
	const navigate = useNavigate();
	const { isAuthenticated, isLoading, login } = useOAuth();

	useEffect(() => {
		// 로딩 중이 아니고 인증된 상태라면 대시보드로 리디렉션
		if (!isLoading && isAuthenticated) {
			navigate("/dashboard");
		}
	}, [isAuthenticated, isLoading, navigate]);

	return (
		<div className="min-h-screen bg-background flex flex-col">
			{/* Navigation */}
			<Navbar onLogin={login} />
			
			{/* Main Content */}
			<main className="flex-1 flex items-center justify-center">
				<div className="container mx-auto px-4 py-12 grid grid-cols-1 md:grid-cols-2 gap-8 items-center">
					{/* Left: Text Content */}
					<div className="space-y-6">
						<div className="text-sm text-primary font-medium">
							{t("dashboard.feature_label")}
						</div>
						<h1 className="text-3xl md:text-4xl font-bold leading-tight">
							{t("dashboard.feature_title")}
						</h1>
						<p className="text-muted-foreground max-w-md">
							{t("dashboard.feature_desc")}
						</p>
						<div className="flex gap-4 items-center mt-6">
							<Button size="lg" onClick={login}>{t("dashboard.get_now")}</Button>
						</div>
					</div>
				</div>
			</main>
			
			{/* Footer */}
			<Footer />
		</div>
	);
}
