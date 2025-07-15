import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import { useOAuth } from "@/hooks/useAuth";
import Header from "@/components/layout/Header";
import HeroSection from "@/components/landing/HeroSection";
import FeaturesSection from "@/components/landing/FeaturesSection";
import TestimonialsSection from "@/components/landing/TestimonialsSection";
import PricingSection from "@/components/landing/PricingSection";
import Footer from "@/components/layout/Footer";

export default function LandingPage() {
	const navigate = useNavigate();
	const { isAuthenticated, isLoading, login } = useOAuth();

	useEffect(() => {
		// 로딩 중이 아니고 인증된 상태라면 대시보드로 리디렉션
		if (!isLoading && isAuthenticated) {
			navigate("/dashboard");
		}
	}, [isAuthenticated, isLoading, navigate]);

	const handleDemo = () => {
		// 데모 보기 기능 - 추후 구현
		console.log("데모 보기 클릭");
	};

	return (
		<div className="min-h-screen bg-white">
			{/* Header */}
			<Header onLogin={login} showNavigation={true} />
			
			{/* Hero Section */}
			<HeroSection onLogin={login} onDemo={handleDemo} />
			
			{/* Features Section */}
			<FeaturesSection />
			
			{/* Testimonials Section */}
			<TestimonialsSection />
			
			{/* Pricing Section */}
			<PricingSection onLogin={login} />
			
			{/* Footer */}
			<Footer />
		</div>
	);
}
