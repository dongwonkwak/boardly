import type React from "react";
import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { CheckCircle } from "lucide-react";
import { useTranslation } from "react-i18next";

interface SuccessModalProps {
	isOpen: boolean;
	onClose: () => void;
	title?: string;
	message?: string;
	confirmText?: string;
	onConfirm: () => void;
	allowBackdropClose?: boolean;
}

export default function SuccessModal({
	isOpen,
	onClose,
	title,
	message,
	confirmText,
	onConfirm,
	allowBackdropClose = true,
}: SuccessModalProps) {
	const { t } = useTranslation("common");

	// ESC 키로 모달 닫기
	useEffect(() => {
		const handleEscapeKey = (event: KeyboardEvent) => {
			if (event.key === "Escape" && allowBackdropClose) {
				onClose();
			}
		};

		if (isOpen) {
			document.addEventListener("keydown", handleEscapeKey);
			// 스크롤 방지
			document.body.style.overflow = "hidden";
		}

		return () => {
			document.removeEventListener("keydown", handleEscapeKey);
			document.body.style.overflow = "unset";
		};
	}, [isOpen, onClose, allowBackdropClose]);

	if (!isOpen) return null;

	const handleBackdropClick = (e: React.MouseEvent) => {
		if (e.target === e.currentTarget && allowBackdropClose) {
			onClose();
		}
	};

	const handleBackdropKeyDown = (e: React.KeyboardEvent) => {
		if (e.key === "Escape" && allowBackdropClose) {
			onClose();
		}
	};

	const handleConfirmClick = () => {
		onConfirm();
		onClose();
	};

	return (
		<div
			className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
			onClick={handleBackdropClick}
			onKeyDown={handleBackdropKeyDown}
			role="dialog"
			aria-modal="true"
			aria-labelledby="modal-title"
			aria-describedby="modal-description"
			tabIndex={-1}
		>
			<div className="relative w-full max-w-md bg-background rounded-xl shadow-2xl transform transition-all duration-300 ease-out animate-in fade-in-0 zoom-in-95">
				{/* 닫기 버튼 */}
				{allowBackdropClose && (
					<button
						type="button"
						onClick={onClose}
						className="absolute top-4 right-4 text-muted-foreground hover:text-foreground transition-colors"
						aria-label={t("modal.close", "닫기")}
					>
						<svg
							className="w-5 h-5"
							fill="none"
							stroke="currentColor"
							viewBox="0 0 24 24"
							aria-hidden="true"
						>
							<path
								strokeLinecap="round"
								strokeLinejoin="round"
								strokeWidth={2}
								d="M6 18L18 6M6 6l12 12"
							/>
						</svg>
					</button>
				)}

				{/* 모달 내용 */}
				<div className="p-6 pt-8 text-center space-y-6">
					{/* 성공 아이콘 */}
					<div className="mx-auto w-16 h-16 bg-green-100 dark:bg-green-900/20 rounded-full flex items-center justify-center">
						<CheckCircle className="w-8 h-8 text-green-600 dark:text-green-400" />
					</div>

					{/* 제목과 메시지 */}
					<div className="space-y-3">
						<h2
							id="modal-title"
							className="text-xl font-semibold text-foreground"
						>
							{title || t("modal.success.title", "성공!")}
						</h2>
						{message && (
							<p
								id="modal-description"
								className="text-muted-foreground text-sm leading-relaxed"
							>
								{message}
							</p>
						)}
					</div>

					{/* 확인 버튼 */}
					<div className="pt-2">
						<Button
							onClick={handleConfirmClick}
							className="w-full h-11 text-base font-medium"
							size="lg"
						>
							{confirmText || t("modal.success.confirm", "확인")}
						</Button>
					</div>
				</div>
			</div>
		</div>
	);
}
