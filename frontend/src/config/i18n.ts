import i18n from "i18next";
import { initReactI18next } from "react-i18next";

// Import translation files
import enCommon from "@/assets/locales/en/common.json";
import koCommon from "@/assets/locales/ko/common.json";

const resources = {
	en: {
		common: enCommon,
	},
	ko: {
		common: koCommon,
	},
};

i18n.use(initReactI18next).init({
	resources,
	lng: "ko", // default language
	fallbackLng: "en",
	interpolation: {
		escapeValue: false,
	},
	defaultNS: "common",
});

export default i18n;
