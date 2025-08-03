import { Plus } from "lucide-react";
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";
import { Activity } from "@/components/dashboard/Activity";
import { BoardSection } from "@/components/dashboard/BoardSection";
import { CreateBoardModal } from "@/components/dashboard/CreateBoardModal";
import { DashboardHeader } from "@/components/dashboard/DashboardHeader";
import { PageHeader } from "@/components/dashboard/DashboardPageHeader";
import { ProfileCard } from "@/components/dashboard/ProfileCard";
import { QuickActions } from "@/components/dashboard/QuickActions";
import { StatsCards } from "@/components/dashboard/StatsCards";
import { useApi } from "@/hooks/useApi";
import { useOAuth } from "@/hooks/useAuth";
import { useDashboard } from "@/hooks/useDashboard";
import { useDashboardStore } from "@/store/dashboardStore";

type ViewMode = "grid" | "list";

export default function Dashboard() {
	const { t } = useTranslation("common");
	const { user: oauthUser } = useOAuth();
	const { authenticated } = useApi();
	const { loadDashboardData, handleCreateBoardSuccess } = useDashboard();
	const [viewMode, setViewMode] = useState<ViewMode>("grid");
	const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

	// 스토어에서 데이터 가져오기
	const {
		boards,
		isLoadingBoards,
		stats,
		activities,
		searchQuery,
		setSearchQuery,
	} = useDashboardStore();

	// 페이지 로드 시 대시보드 데이터 가져오기
	useEffect(() => {
		if (authenticated) {
			loadDashboardData();
		}
	}, [authenticated, loadDashboardData]);

	const openCreateModal = () => {
		setIsCreateModalOpen(true);
	};

	const closeCreateModal = () => {
		setIsCreateModalOpen(false);
	};

	const handleToggleStar = async (boardId: string) => {
		if (!authenticated) return;

		try {
			// 현재 보드의 즐겨찾기 상태 확인
			const currentBoard = boards.find((board) => board.id === boardId);
			if (!currentBoard) return;

			// 즐겨찾기 상태에 따라 API 호출
			if (currentBoard.isStarred) {
				await (authenticated.unstarBoard as any)(boardId);
			} else {
				await (authenticated.starBoard as any)(boardId);
			}

			// 대시보드 데이터 새로고침
			loadDashboardData();
		} catch (error) {
			console.error("Failed to toggle star:", error);
		}
	};

	return (
		<ProtectedRoute>
			<ErrorBoundary>
				<div className="min-h-screen bg-gradient-to-br from-slate-50 to-blue-50">
					{/* Header */}
					<DashboardHeader
						searchQuery={searchQuery}
						setSearchQuery={setSearchQuery}
						viewMode={viewMode}
						setViewMode={setViewMode}
					/>

					<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
						<div className="flex flex-col lg:flex-row gap-8">
							{/* Main Content */}
							<div className="flex-1">
								{/* Page Header */}
								<PageHeader
									title={t("dashboard.pageTitle")}
									description={t("dashboard.pageDescription")}
									buttonText={t("dashboard.createBoardButton")}
									onButtonClick={openCreateModal}
									buttonDisabled={!authenticated}
								/>

								{/* Stats Cards */}
								<StatsCards
									totalBoards={stats.totalBoards}
									totalCards={stats.totalCards}
									favorites={stats.starredBoards}
									archived={stats.archivedBoards}
								/>

								{/* Boards Section */}
								<BoardSection
									boards={boards}
									viewMode={viewMode}
									searchQuery={searchQuery}
									isLoadingBoards={isLoadingBoards}
									onOpenCreateModal={openCreateModal}
									authenticated={!!authenticated}
									onToggleStar={handleToggleStar}
								/>
							</div>

							{/* Sidebar */}
							<div className="w-full lg:w-80 space-y-6">
								<ProfileCard
									user={{
										name: oauthUser?.profile?.name,
										givenName: oauthUser?.profile?.given_name,
										familyName: oauthUser?.profile?.family_name,
										email: oauthUser?.profile?.email,
									}}
								/>

								<Activity activities={activities as any} />

								<QuickActions
									actions={[
										{
											id: "create-board",
											label: t("dashboard.createBoardButton"),
											icon: <Plus className="w-4 h-4 mr-3 text-blue-600" />,
											onClick: openCreateModal,
											disabled: !authenticated,
										},
									]}
								/>
							</div>
						</div>
					</div>
				</div>

				{/* Create Board Modal */}
				<CreateBoardModal
					isOpen={isCreateModalOpen}
					onClose={closeCreateModal}
					createBoard={authenticated?.createBoard as any}
					onSuccess={() => {
						// 보드 생성 성공 후 처리
						handleCreateBoardSuccess();
						closeCreateModal();
					}}
				/>
			</ErrorBoundary>
		</ProtectedRoute>
	);
}
