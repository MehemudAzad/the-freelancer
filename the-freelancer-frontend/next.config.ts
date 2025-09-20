import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  eslint: {
    // Ignore ESLint errors during builds
    ignoreDuringBuilds: true,
  },
  typescript: {
    // Ignore TypeScript errors during builds
    ignoreBuildErrors: true,
  },
  output: 'standalone',
  experimental: {
    turbo: {
      root: __dirname,
    },
  },
  outputFileTracingRoot: __dirname,
};

export default nextConfig;
