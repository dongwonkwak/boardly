import { useEffect } from "react";
import {
	BrowserRouter as Router,
	Routes,
	Route,
} from "react-router-dom";
import "./config/i18n";
import LandingPage from "@/pages/LandingPage";
import Register from "@/pages/Auth/Register";

function App() {
	useEffect(() => {
		// i18n is already initialized in the import
	}, []);

	return (
		<Router>
			<Routes>
				<Route path="/" element={<LandingPage />} />
				<Route path="/register" element={<Register />} />
			</Routes>
		</Router>
	);
}

export default App;
