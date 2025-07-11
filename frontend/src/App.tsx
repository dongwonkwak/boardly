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
import { useOAuth } from "@/hooks/useAuth";
import { useUserStore } from "@/store/userStore";

function App() {
	const { events, auth } = useOAuth();
	const { fetchUser } = useUserStore();

	useEffect(() => {
		// 사용자 로그인 시 자동으로 사용자 정보 가져오기
		const handleUserSignedIn = async () => {
			if (auth.user?.access_token) {
				try {
					await fetchUser(auth.user.access_token);
				} catch (error) {
					console.error('사용자 정보 가져오기 실패:', error);
				}
			}
		};

		// 이벤트 등록
		events.addUserSignedIn(handleUserSignedIn);

		// 정리
		return () => {
			events.removeUserSignedIn(handleUserSignedIn);
		};
	}, [events, auth.user?.access_token, fetchUser]);

	useEffect(() => {
		// i18n is already initialized in the import
	}, []);

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
