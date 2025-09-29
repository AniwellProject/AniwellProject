"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function CheckPwPage() {
  const [password, setPassword] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [checking, setChecking] = useState(true);
  const router = useRouter();

  // ✅ 접속 시 자동 우회 로직
  useEffect(() => {
    (async () => {
      try {
        // 1) 내 정보에서 socialProvider 확인
        const meRes = await fetch("http://localhost:8080/api/member/getUsrInfo", {
          credentials: "include",
        });
        if (meRes.ok) {
          const me = await meRes.json();
          const isSocial =
            !!me?.socialProvider && String(me.socialProvider).trim() !== "";
          if (isSocial) {
            router.replace("/my-page/edit");
            return;
          }
        }

        // 2) 보수: 빈 비번으로 체크 → SOCIAL_OK면 우회
        const res = await fetch("http://localhost:8080/usr/member/doCheckPw", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({ loginPw: "" }),
          credentials: "include",
        });
        const txt = await res.text();
        if (txt.includes("SOCIAL_OK")) {
          router.replace("/my-page/edit");
          return;
        }
      } catch {
        // 무시하고 폼 노출
      } finally {
        setChecking(false);
      }
    })();
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const res = await fetch("http://localhost:8080/usr/member/doCheckPw", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ loginPw: password }),
        credentials: "include",
      });

      const text = await res.text();
      if (text.includes("OK")) {
        router.push("/my-page/edit");
      } else {
        setErrorMsg(text);
      }
    } catch (err) {
      console.error(err);
      setErrorMsg("요청 중 오류 발생");
    }
  };

  if (checking) {
    return (
      <section className="mt-12 text-lg px-4">
        <div className="mx-auto max-w-xl bg-white p-6 rounded-xl shadow-md text-center">
          확인 중…
        </div>
      </section>
    );
  }

  return (
    <section className="mt-12 text-lg px-4">
      <div className="mx-auto max-w-xl bg-white p-6 rounded-xl shadow-md">
        <h1 className="text-2xl font-bold mb-6 text-center">비밀번호 확인</h1>

        <form onSubmit={handleSubmit}>
          <table className="w-full table-auto text-sm">
            <tbody>
              <tr className="border-t">
                <th className="text-left px-4 py-2 w-1/3">비밀번호</th>
                <td className="px-4 py-2">
                  <input
                    name="loginPw"
                    type="password"
                    placeholder="비밀번호 입력"
                    className="input input-sm w-full"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    autoComplete="off"
                  />
                </td>
              </tr>
              <tr className="border-t">
                <td colSpan={2} className="text-center py-4">
                  <button type="submit" className="btn btn-primary">
                    확인
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </form>

        {errorMsg && (
          <div className="text-center mt-4 text-red-500">{errorMsg}</div>
        )}

        <div className="text-center mt-4">
          <button className="btn" type="button" onClick={() => router.back()}>
            ← 뒤로가기
          </button>
        </div>
      </div>
    </section>
  );
}
