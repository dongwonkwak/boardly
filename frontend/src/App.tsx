import { useEffect } from "react";
import {
	BrowserRouter as Router,
	Routes,
	Route,
} from "react-router-dom";
import "./config/i18n";
import LandingPage from "@/pages/LandingPage";
import Register from "@/pages/Auth/Register";
import Dashboard from "@/pages/Dashboard";
import Callback from "./pages/Auth/Callback";
import { useLanguageStore } from "@/store/languageStore";

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
				<Route path="/callback" element={<Callback />} />
			</Routes>
		</Router>
	);
}

export default App;
