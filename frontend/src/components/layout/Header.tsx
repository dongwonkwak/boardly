import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useTranslation } from "react-i18next";
import LanguageSelector from "@/components/common/LanguageSelector";

interface HeaderProps {
  onLogin?: () => void;
  showNavigation?: boolean;
}

export default function Header({ onLogin, showNavigation = true }: HeaderProps) {
  const { t } = useTranslation("common");

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-200">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        {/* Logo */}
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-r from-blue-600 to-purple-600 flex items-center justify-center">
            <span className="text-white font-bold text-sm">B</span>
          </div>
          <Link to="/" className="text-xl font-bold text-gray-900 hover:opacity-80 transition-opacity">
            Boardly
          </Link>
        </div>

        {/* Navigation Menu */}
        {showNavigation && (
          <nav className="hidden md:flex items-center gap-8">
            <Link to="#features" className="text-gray-600 hover:text-gray-900 font-medium transition-colors">
              {t("header.features")}
            </Link>
            <Link to="#testimonials" className="text-gray-600 hover:text-gray-900 font-medium transition-colors">
              {t("header.testimonials")}
            </Link>
            <Link to="#pricing" className="text-gray-600 hover:text-gray-900 font-medium transition-colors">
              {t("header.pricing")}
            </Link>
            <Button 
              variant="ghost" 
              className="text-gray-600 hover:text-gray-900 font-medium"
              onClick={onLogin}
            >
              {t("header.login")}
            </Button>
          </nav>
        )}

        {/* Right Side Actions */}
        <div className="flex items-center gap-2 md:gap-4">
          {/* Language Selector - 모바일에서도 표시 */}
          <LanguageSelector />
          
          {/* CTA Button */}
          <Button 
            className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white font-medium px-4 md:px-6"
            onClick={onLogin}
          >
            <span className="hidden sm:inline">{t("header.freeStart")}</span>
            <span className="sm:hidden">{t("header.freeStartShort")}</span>
          </Button>
        </div>
      </div>
    </header>
  );
} 