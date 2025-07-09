import { z } from "zod";

const envSchema = z.object({
  VITE_API_URL: z.string().url(),
  VITE_OAUTH_AUTHORIZATION_ENDPOINT: z.string().url(),
  VITE_OAUTH_CLIENT_ID: z.string(),
  VITE_OAUTH_CLIENT_SECRET: z.string(),
  VITE_OAUTH_RESPONSE_TYPE: z.string(),
  VITE_OAUTH_REDIRECT_URI: z.string().url(),
  VITE_OAUTH_POST_LOGOUT_REDIRECT_URI: z.string().url(),
  VITE_OAUTH_SCOPE: z.string(),
  VITE_OAUTH_CLIENT_AUTHENTICATION: z.enum(["client_secret_basic", "client_secret_post"]),
});

let env: z.infer<typeof envSchema>;

try {
  env = envSchema.parse(import.meta.env);
} catch (error) {
  console.error("❌ 환경 변수 검증 실패:");
  if (error instanceof z.ZodError) {
    error.errors.forEach((err) => {
      console.error(`  - ${err.path.join('.')}: ${err.message}`);
    });
  }
  console.error("프로그램을 종료합니다.");
  process.exit(1);
}

export default env;
