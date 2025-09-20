import * as React from "react";

const badgeVariants = {
  default: "border-transparent bg-blue-100 text-blue-800",
  secondary: "border-transparent bg-gray-100 text-gray-800",
  destructive: "border-transparent bg-red-100 text-red-800",
  outline: "border-gray-200 text-gray-700",
  success: "border-transparent bg-green-100 text-green-800",
};

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: keyof typeof badgeVariants;
}

function Badge({ className = "", variant = "default", ...props }: BadgeProps) {
  const variantClasses = badgeVariants[variant];
  const classes = `inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors ${variantClasses} ${className}`;
  
  return (
    <div className={classes} {...props} />
  );
}

export { Badge };
