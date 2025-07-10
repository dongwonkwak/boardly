import { useEffect } from "react";
import {
	BrowserRouter as Router,
	Routes,
	Route,
} from "react-router-dom";
import "./config/i18n";
import { OAuthProvider } from "@/providers/OAuthProvider";
import LandingPage from "@/pages/LandingPage";
import Register from "@/pages/Auth/Register";
import Dashboard from "@/pages/Dashboard";
import Callback from "./pages/Auth/Callback";

function App() {
	useEffect(() => {
		// i18n is already initialized in the import
	}, []);

	return (
		<OAuthProvider>
			<Router>
				<Routes>
					<Route path="/" element={<LandingPage />} />
					<Route path="/register" element={<Register />} />
					<Route path="/dashboard" element={<Dashboard />} />
					<Route path="/callback" element={<Callback />} />
				</Routes>
			</Router>
		</OAuthProvider>
	);
}

export default App;
