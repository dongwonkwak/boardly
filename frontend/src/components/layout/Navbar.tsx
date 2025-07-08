import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function Navbar() {
  const { t } = useTranslation('common');

  return (
    <nav className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4 h-16 flex items-center justify-between">
        {/* Logo */}
        <div className="flex items-center">
          <Link to="/" className="hover:opacity-80 transition-opacity">
            <h1 className="text-2xl font-bold text-primary cursor-pointer">
              {t('nav.logo')}
            </h1>
          </Link>
        </div>

        {/* Auth Buttons */}
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" className="cursor-pointer">
            {t('nav.login')}
          </Button>
          <Button size="sm" className="cursor-pointer">
            {t('nav.register')}
          </Button>
        </div>
      </div>
    </nav>
  );
} 