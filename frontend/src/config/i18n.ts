import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import enActivity from "@/assets/locales/en/activity.json";
import enBoard from "@/assets/locales/en/board.json";
// Import translation files
import enCommon from "@/assets/locales/en/common.json";
import koActivity from "@/assets/locales/ko/activity.json";
import koBoard from "@/assets/locales/ko/board.json";
import koCommon from "@/assets/locales/ko/common.json";

const resources = {
	en: {
		common: enCommon,
		activity: enActivity,
		board: enBoard,
	},
	ko: {
		common: koCommon,
		activity: koActivity,
		board: koBoard,
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
