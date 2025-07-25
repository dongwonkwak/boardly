import i18n from "i18next";
import { initReactI18next } from "react-i18next";

// Import translation files
import enCommon from "@/assets/locales/en/common.json";
import koCommon from "@/assets/locales/ko/common.json";
import enActivity from "@/assets/locales/en/activity.json";
import koActivity from "@/assets/locales/ko/activity.json";

const resources = {
	en: {
		common: enCommon,
		activity: enActivity,
	},
	ko: {
		common: koCommon,
		activity: koActivity,
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
