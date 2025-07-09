import { useEffect } from "react";
import {
	BrowserRouter as Router,
	Routes,
	Route,
	useLocation,
} from "react-router-dom";
import "./config/i18n";
import Navbar from "@/components/layout/Navbar";
import Dashboard from "@/pages/Dashboard";
import Footer from "@/components/layout/Footer";
import Register from "@/pages/Auth/Register";

function Layout({ children }: { children: React.ReactNode }) {
	const location = useLocation();
	const isRegister = location.pathname === "/register";
	return (
		<div className="min-h-screen flex flex-col">
			{!isRegister && <Navbar />}
			{children}
			{!isRegister && <Footer />}
		</div>
	);
}

function App() {
	useEffect(() => {
		// i18n is already initialized in the import
	}, []);

	return (
		<Router>
			<Layout>
				<Routes>
					<Route path="/" element={<Dashboard />} />
					<Route path="/register" element={<Register />} />
				</Routes>
			</Layout>
		</Router>
	);
}

export default App;
