import { Button } from "@/components/ui/button";
import { Settings, LogOut } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useOAuth } from "@/hooks/useAuth";
import { useNavigate } from "react-router-dom";

interface UserProfile {
  name?: string;
  givenName?: string;
  familyName?: string;
  email?: string;
  avatar?: string;
}

interface ProfileCardProps {
  user?: UserProfile;
  onSettings?: () => void;
}

export function ProfileCard({ 
  user, 
  onSettings = () => console.log('설정')
}: ProfileCardProps) {
  const { t } = useTranslation('common');
  const { logout } = useOAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logout();
      navigate('/');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };
  
  const displayName = user?.name || 
    `${user?.givenName || ''} ${user?.familyName || ''}`.trim() || 
    t('profile.defaultUser');
  
  const displayEmail = user?.email || t('profile.defaultEmail');
  const avatarInitial = displayName.charAt(0);

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div className="flex items-center space-x-4 mb-4">
        <div className="w-12 h-12 bg-gradient-to-r from-blue-600 to-purple-600 rounded-full flex items-center justify-center">
          <span className="text-white font-bold">
            {avatarInitial}
          </span>
        </div>
        <div>
          <h3 className="font-semibold text-gray-900">{displayName}</h3>
          <p className="text-sm text-gray-600">{displayEmail}</p>
        </div>
      </div>
      <div className="flex space-x-2">
        <Button 
          variant="outline" 
          onClick={onSettings}
          className="flex-1 flex items-center justify-center px-3 py-2 text-sm"
        >
          <Settings className="w-4 h-4 mr-2" />
          {t('profile.settings')}
        </Button>
        <Button 
          variant="outline" 
          onClick={handleLogout}
          className="flex-1 flex items-center justify-center px-3 py-2 text-sm"
        >
          <LogOut className="w-4 h-4 mr-2" />
          {t('profile.logout')}
        </Button>
      </div>
    </div>
  );
} 