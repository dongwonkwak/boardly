import { Button } from "@/components/ui/button";
import { Plus, Users, Archive } from "lucide-react";

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
  const defaultActions: QuickAction[] = [
    {
      id: 'create-board',
      label: '새 보드 만들기',
      icon: <Plus className="w-4 h-4 mr-3 text-blue-600" />,
      onClick: () => console.log('새 보드 만들기'),
      color: 'text-blue-600'
    },
    {
      id: 'invite-team',
      label: '팀원 초대하기',
      icon: <Users className="w-4 h-4 mr-3 text-green-600" />,
      onClick: () => console.log('팀원 초대하기'),
      color: 'text-green-600'
    },
    {
      id: 'view-archived',
      label: '보관된 보드 보기',
      icon: <Archive className="w-4 h-4 mr-3 text-gray-600" />,
      onClick: () => console.log('보관된 보드 보기'),
      color: 'text-gray-600'
    }
  ];

  const displayActions = actions.length > 0 ? actions : defaultActions;

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="font-semibold text-gray-900 mb-4">빠른 작업</h3>
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