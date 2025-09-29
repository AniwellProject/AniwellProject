/** @type {import("next").NextConfig} */
const nextConfig = {
  output: "standalone",
  // 아래 두 줄이 포인트
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: true },
};
module.exports = nextConfig;
