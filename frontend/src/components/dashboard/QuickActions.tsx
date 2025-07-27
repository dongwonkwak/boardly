import { Button } from "@/components/ui/button";
import { Plus, Users, Archive } from "lucide-react";
import { useTranslation } from "react-i18next";

interface QuickAction {
  id: string;
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  disabled?: boolean;
  color?: string;
}

interface QuickActionsProps {
  actions?: QuickAction[];
}

export function QuickActions({ actions = [] }: QuickActionsProps) {
  const { t } = useTranslation('common');
  
  const defaultActions: QuickAction[] = [
    {
      id: 'create-board',
      label: t('quickActions.createBoard'),
      icon: <Plus className="w-4 h-4 mr-3 text-blue-600" />,
      onClick: () => console.log(t('quickActions.createBoard')),
      color: 'text-blue-600'
    },
    {
      id: 'invite-team',
      label: t('quickActions.inviteTeam'),
      icon: <Users className="w-4 h-4 mr-3 text-green-600" />,
      onClick: () => console.log(t('quickActions.inviteTeam')),
      color: 'text-green-600'
    },
    {
      id: 'view-archived',
      label: t('quickActions.viewArchived'),
      icon: <Archive className="w-4 h-4 mr-3 text-gray-600" />,
      onClick: () => console.log(t('quickActions.viewArchived')),
      color: 'text-gray-600'
    }
  ];

  const displayActions = actions.length > 0 ? actions : defaultActions;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="font-semibold text-gray-900 mb-4">{t('quickActions.title')}</h3>
      <div className="space-y-3">
        {displayActions.map((action) => (
          <Button
            key={action.id}
            variant="ghost"
            onClick={action.onClick}
            disabled={action.disabled}
            className="w-full flex items-center px-3 py-2 text-left text-sm text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            {action.icon}
            {action.label}
          </Button>
        ))}
      </div>
    </div>
  );
} 