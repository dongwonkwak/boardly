import { Button } from "@/components/ui/button";
import { Settings, LogOut } from "lucide-react";

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
  onLogout?: () => void;
}

export function ProfileCard({ 
  user, 
  onSettings = () => console.log('설정'),
  onLogout = () => console.log('로그아웃')
}: ProfileCardProps) {
  const displayName = user?.name || 
    `${user?.givenName || ''} ${user?.familyName || ''}`.trim() || 
    '사용자';
  
  const displayEmail = user?.email || 'user@example.com';
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
          설정
        </Button>
        <Button 
          variant="outline" 
          onClick={onLogout}
          className="flex-1 flex items-center justify-center px-3 py-2 text-sm"
        >
          <LogOut className="w-4 h-4 mr-2" />
          로그아웃
        </Button>
      </div>
    </div>
  );
} 