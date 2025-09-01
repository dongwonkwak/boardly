import { useEffect } from "react";
import { Route, BrowserRouter as Router, Routes } from "react-router-dom";
import "./config/i18n";
import Register from "@/pages/Auth/Register";
import BoardDetailPage from "@/pages/BoardDetail";
import Dashboard from "@/pages/Dashboard";
import LandingPage from "@/pages/LandingPage";
import { useLanguageStore } from "@/store/languageStore";
import Callback from "./pages/Auth/Callback";

function App() {
	const { initializeLanguage } = useLanguageStore();

	useEffect(() => {
		// 언어 스토어 초기화
		initializeLanguage();
	}, [initializeLanguage]);

	return (
		<Router>
			<Routes>
				<Route path="/" element={<LandingPage />} />
				<Route path="/register" element={<Register />} />
				<Route path="/dashboard" element={<Dashboard />} />
				<Route path="/boards/:boardId" element={<BoardDetailPage />} />
				<Route path="/callback" element={<Callback />} />
			</Routes>
		</Router>
	);
}

export default App;
