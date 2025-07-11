import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { OAuthProvider } from "./providers/OAuthProvider.tsx";

const rootElement = document.getElementById("root");
if (!rootElement) throw new Error("Root element not found");

createRoot(rootElement).render(
	<StrictMode>
		<OAuthProvider>
			<App />
		</OAuthProvider>
	</StrictMode>,
);
