import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import type React from "react";

// ButtonProps 타입을 직접 정의
import type { ComponentProps } from "react";
type ButtonProps = ComponentProps<typeof Button>;

interface LoadingButtonProps extends ButtonProps {
  loading?: boolean;
  children: React.ReactNode;
}

export default function LoadingButton({ loading, children, ...props }: LoadingButtonProps) {
  return (
    <Button disabled={loading || props.disabled} {...props}>
      {loading && (
        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
      )}
      {children}
    </Button>
  );
} 