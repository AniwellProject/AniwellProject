"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";

type Member = {
  id: number;
  regDate: string;
  updateDate: string;
  loginId: string;
  loginPw: string;
  name: string;
  nickname: string;
  cellphone: string;
  email: string;
  delStatus: boolean;
  delDate: string | null;
  authLevel: number;
  authName: string;
  photo: string;
  address: string;
  vetCertUrl: string;
  vetCertApproved: number | null;
  socialProvider?: string | null;
  socialId?: string | null;
};

export default function EditPage() {
  const [form, setForm] = useState({
    nickname: "",
    email: "",
    cellphone: "",
    password: "",
    confirmPassword: "",
    name: "",
    address: "",
  });

  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [currentStep, setCurrentStep] = useState(1);
  const [photoPreview, setPhotoPreview] = useState("/img/default-card.png");
  const [pwChangeActive, setPwChangeActive] = useState(false);
  const [pwMatchMsg, setPwMatchMsg] = useState("");
  const [member, setMember] = useState<Member | null>(null);

  // 소셜 여부
  const isSocial =
    !!member?.socialProvider && String(member.socialProvider).trim() !== "";

  // ───────── 재인증 상태
  const [initialEmail, setInitialEmail] = useState("");
  const [initialPhone, setInitialPhone] = useState("");

  // 이메일
  const [emailSending, setEmailSending] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [emailShowBox, setEmailShowBox] = useState(false);
  const [emailTxId, setEmailTxId] = useState<string | null>(null);
  const [emailCode, setEmailCode] = useState("");
  const [emailWarn, setEmailWarn] = useState("");
  const [emailCodeWarn, setEmailCodeWarn] = useState("");

  // 전화번호
  const [phoneSending, setPhoneSending] = useState(false);
  const [phoneVerified, setPhoneVerified] = useState(false);
  const [phoneShowBox, setPhoneShowBox] = useState(false);
  const [phoneCode, setPhoneCode] = useState("");
  const [phoneWarn, setPhoneWarn] = useState("");
  const [phoneCodeWarn, setPhoneCodeWarn] = useState("");
  const [cooldownRemain, setCooldownRemain] = useState<number>(0);
  const cooldownTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // ───────── 유틸
  const isValidEmail = (v: string) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());
  const normalizePhone = (v: string) => v.replace(/\D/g, "");
  const isValidPhone = (raw: string) =>
    /^01[016789]\d{7,8}$/.test(normalizePhone(raw));

  // 회원정보 로딩
  useEffect(() => {
    fetch("http://localhost:8080/api/member/getUsrInfo", {
      method: "GET",
      credentials: "include",
    })
      .then((res) => {
        if (!res.ok) throw new Error("회원정보 불러오기 실패");
        return res.json();
      })
      .then((data: Member) => {
        setMember(data);
        setForm({
          nickname: data.nickname || "",
          email: data.email || "",
          cellphone: data.cellphone || "",
          password: "",
          confirmPassword: "",
          name: data.name || "",
          address: data.address || "",
        });
        setPhotoPreview(data.photo ? data.photo : "/img/default-card.png");

        // 재인증 기준값
        setInitialEmail(data.email || "");
        setInitialPhone(data.cellphone || "");
        setEmailVerified(false);
        setPhoneVerified(false);
      })
      .catch((err) => {
        console.error("❌ 회원 정보 요청 실패", err);
        alert("회원 정보를 불러오는 데 실패했습니다.");
        router.push("/my-page");
      });
  }, [router]);

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => setPhotoPreview(reader.result as string);
      reader.readAsDataURL(file);
    }
  };

  // 제출(변경 시 재인증 강제)
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (
      !isSocial &&
      pwChangeActive &&
      (form.password.length < 4 || form.password !== form.confirmPassword)
    ) {
      alert("비밀번호 오류");
      return;
    }

    const emailChanged = form.email.trim() !== (initialEmail || "").trim();
    const phoneChanged = form.cellphone.trim() !== (initialPhone || "").trim();
    if (emailChanged && !emailVerified) {
      alert(
        "이메일 변경 시 인증이 필요합니다. '이메일 인증'으로 완료해주세요."
      );
      return;
    }
    if (phoneChanged && !phoneVerified) {
      alert(
        "전화번호 변경 시 인증이 필요합니다. '휴대폰 인증'으로 완료해주세요."
      );
      return;
    }

    const formData = new FormData();
    formData.append("name", form.name);
    formData.append("nickname", form.nickname);
    formData.append("email", form.email);
    formData.append("cellphone", form.cellphone);
    formData.append("address", form.address);
    if (!isSocial && pwChangeActive && form.password)
      formData.append("loginPw", form.password);
    if (fileInputRef.current?.files?.[0])
      formData.append("photoFile", fileInputRef.current.files[0]);

    try {
      const res = await fetch("http://localhost:8080/usr/member/doModify", {
        method: "POST",
        body: formData,
        credentials: "include",
      });
      if (res.ok) {
        alert("수정 완료!");
        router.push("/my-page");
      } else {
        alert("서버 오류");
      }
    } catch (err) {
      console.error(err);
      alert("전송 실패");
    }
  };

  // 비밀번호 일치 문구
  useEffect(() => {
    const { password, confirmPassword } = form;
    if (!password || !confirmPassword) return setPwMatchMsg("");
    setPwMatchMsg(
      password === confirmPassword
        ? "✅ 비밀번호가 일치합니다."
        : "❌ 비밀번호가 일치하지 않습니다."
    );
  }, [form.password, form.confirmPassword]);

  // 쿨다운 타이머
  useEffect(() => {
    if (cooldownRemain <= 0) {
      if (cooldownTimerRef.current) {
        clearInterval(cooldownTimerRef.current);
        cooldownTimerRef.current = null;
      }
      return;
    }
    if (!cooldownTimerRef.current) {
      cooldownTimerRef.current = setInterval(
        () => setCooldownRemain((s) => Math.max(0, s - 1)),
        1000
      );
    }
    return () => {
      if (cooldownTimerRef.current) {
        clearInterval(cooldownTimerRef.current);
        cooldownTimerRef.current = null;
      }
    };
  }, [cooldownRemain]);

  // 이메일 인증
  const onSendEmailCode = async () => {
    if (emailSending) return;
    const email = form.email.trim();
    if (!isValidEmail(email))
      return setEmailWarn("유효한 이메일 주소를 입력해주세요.");

    setEmailWarn("");
    setEmailCodeWarn("");
    setEmailSending(true);
    try {
      const res = await fetch("/api/verify/email/send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, purpose: "signup" }),
      });
      let json: any = null;
      try {
        json = await res.json();
      } catch {}
      if (!res.ok || !String(json?.resultCode || "").startsWith("S-")) {
        setEmailWarn(json?.msg || "인증번호 전송에 실패했습니다.");
        return;
      }
      const txId =
        json?.txId ??
        json?.data?.txId ??
        json?.data1?.txId ??
        (typeof json?.data1 === "string" ? json?.data1 : null);
      setEmailTxId(txId ?? null);
      setEmailShowBox(true); // 바로 밑 칸 열기
      setEmailWarn("인증번호를 이메일로 전송했습니다.");
    } catch {
      setEmailWarn("네트워크 오류로 전송에 실패했습니다.");
    } finally {
      setEmailSending(false);
    }
  };

  const onVerifyEmailCode = async () => {
    if (!emailTxId) return setEmailCodeWarn("먼저 인증번호를 요청해주세요.");
    if (!emailCode.trim()) return setEmailCodeWarn("인증번호를 입력해주세요.");

    try {
      const res = await fetch("/api/verify/email/check", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          txId: emailTxId,
          code: emailCode.trim(),
          purpose: "signup",
        }),
      });
      const json = await res.json();
      if (!res.ok || !String(json?.resultCode || "").startsWith("S-")) {
        setEmailCodeWarn(json?.msg || "인증번호가 일치하지 않습니다.");
        return;
      }
      setEmailShowBox(false);
      setEmailVerified(true);
      setEmailWarn("");
      setEmailCodeWarn("");
      alert("이메일 인증이 완료되었습니다.");
    } catch {
      setEmailCodeWarn("네트워크 오류로 인증에 실패했습니다.");
    }
  };

  // 이메일 변경 시 바로 밑 칸 자동 표시
  useEffect(() => {
    if (form.email !== initialEmail) {
      setEmailVerified(false);
      setEmailTxId(null);
      setEmailShowBox(true);
      setEmailWarn("");
      setEmailCodeWarn("");
      setEmailCode("");
    } else {
      setEmailShowBox(false);
    }
  }, [form.email, initialEmail]);

  // 전화번호 인증
  const onSendPhoneCode = async () => {
    if (phoneSending) return;
    const raw = form.cellphone;
    if (!raw.trim()) return setPhoneWarn("전화번호를 입력하세요.");
    if (!isValidPhone(raw))
      return setPhoneWarn(
        "전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000"
      );

    setPhoneWarn("");
    setPhoneCodeWarn("");
    setPhoneSending(true);
    try {
      const res = await fetch("/api/verify/sms/send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ phone: normalizePhone(raw) }),
      });
      const json = await res.json();
      if (res.ok && json?.resultCode === "S-OK") {
        setPhoneShowBox(true);
        setCooldownRemain(Number(json?.data?.cooldownSec ?? 60));
        setPhoneWarn("인증번호를 전송했습니다.");
      } else if (json?.resultCode === "F-COOLDOWN") {
        setPhoneShowBox(true);
        setCooldownRemain(Number(json?.data?.retryAfterSec ?? 60));
        setPhoneWarn(json?.msg || "재전송 대기 중입니다.");
      } else {
        setPhoneWarn(json?.msg || "인증번호 전송에 실패했습니다.");
      }
    } catch {
      setPhoneWarn("네트워크 오류로 전송에 실패했습니다.");
    } finally {
      setPhoneSending(false);
    }
  };

  const onVerifyPhoneCode = async () => {
    const raw = form.cellphone;
    if (!raw.trim()) return setPhoneWarn("전화번호를 입력하세요.");
    if (!isValidPhone(raw))
      return setPhoneWarn(
        "전화번호 형식이 올바르지 않습니다. 예: 000-0000-0000"
      );
    if (!phoneCode.trim()) return setPhoneCodeWarn("인증코드를 입력하세요.");

    try {
      const res = await fetch("/api/verify/sms/confirm", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          phone: normalizePhone(raw),
          code: phoneCode.trim(),
        }),
      });
      const json = await res.json();
      if (res.ok && json?.resultCode === "S-OK") {
        setPhoneShowBox(false);
        setPhoneVerified(true);
        setPhoneWarn("");
        setPhoneCodeWarn("");
      } else {
        setPhoneCodeWarn(json?.msg || "인증번호가 일치하지 않습니다.");
      }
    } catch {
      setPhoneCodeWarn("네트워크 오류로 인증에 실패했습니다.");
    }
  };

  // 전화번호 변경 시 바로 밑 칸 자동 표시
  useEffect(() => {
    if (form.cellphone !== initialPhone) {
      setPhoneVerified(false);
      setPhoneShowBox(true);
      setPhoneWarn("");
      setPhoneCodeWarn("");
      setPhoneCode("");
      setCooldownRemain(0);
      if (cooldownTimerRef.current) {
        clearInterval(cooldownTimerRef.current);
        cooldownTimerRef.current = null;
      }
    } else {
      setPhoneShowBox(false);
    }
  }, [form.cellphone, initialPhone]);

  if (!member) return <div>로딩 중...</div>;

  // 공통: 스샷 느낌의 파란 pill 버튼 클래스
  const pillBtn =
    "px-4 h-10 rounded-full bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed";

  return (
    <div className="bg-white p-6 rounded-xl shadow-md w-full h-full">
      <form
        onSubmit={handleSubmit}
        encType="multipart/form-data"
        className="grid grid-cols-3 gap-8 relative h-full"
      >
        {/* 프로필 */}
        <div className="flex flex-col items-center col-span-1 border-r border-gray-300 pr-6">
          <h1 className="text-2xl font-bold mb-6">회원정보 수정</h1>
          <img
            className="w-[120px] h-[120px] object-cover rounded-full border-4 border-gray-200 shadow mb-3"
            src={photoPreview}
            alt="프로필 사진"
          />
          <label
            htmlFor="photoInput"
            className="cursor-pointer text-sm text-gray-600 hover:underline"
          >
            📷 사진 변경하기
          </label>
          <input
            type="file"
            id="photoInput"
            accept="image/*"
            onChange={handlePhotoChange}
            className="hidden"
            ref={fileInputRef}
          />
        </div>

        {/* 기본 정보 */}
        <div className="space-y-5 col-span-2 grid-cols-2">
          {currentStep === 1 && (
            <>
              <div className="flex items-center gap-4">
                <div className="w-[30%] font-semibold text-gray-700">
                  아이디
                </div>
                <div className="w-[80%] p-2 bg-gray-100 rounded-md shadow-inner text-sm">
                  {member.loginId}
                </div>
              </div>

              <div className="flex items-center gap-4">
                <div className="w-[30%] font-semibold text-gray-700">
                  이름
                </div>
                <div className="w-[80%] p-2 bg-gray-100 rounded-md shadow-inner text-sm">
                  {member.loginId}
                </div>
              </div>
              {!isSocial && (
                <>
                  <div className="flex items-center gap-4">
                    <div className="w-[30%] font-semibold text-gray-700">
                      비밀번호
                    </div>
                    <button
                      type="button"
                      className="btn btn-outline btn-sm w-[80%]"
                      onClick={() => setPwChangeActive(!pwChangeActive)}
                    >
                      {pwChangeActive ? "비밀번호 변경 취소" : "비밀번호 변경"}
                    </button>
                  </div>

                  {pwChangeActive && (
                    <>
                      <div className="flex items-center gap-4">
                        <div className="w-[30%] font-semibold text-gray-700">
                          새 비밀번호
                        </div>
                        <input
                          type="password"
                          value={form.password}
                          onChange={(e) =>
                            handleChange("password", e.target.value)
                          }
                          className="p-2 input input-sm w-[80%] shadow rounded-md border"
                        />
                      </div>
                      <div className="flex items-center gap-4">
                        <div className="w-[30%] font-semibold text-gray-700">
                          비밀번호 확인
                        </div>
                        <input
                          type="password"
                          value={form.confirmPassword}
                          onChange={(e) =>
                            handleChange("confirmPassword", e.target.value)
                          }
                          className="p-2 input input-sm w-[80%] shadow rounded-md border"
                        />
                      </div>
                      <div className="text-sm text-gray-600 pl-[30%]">
                        {pwMatchMsg}
                      </div>
                    </>
                  )}
                </>
              )}

              <div className="flex justify-end mt-4">
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => setCurrentStep(2)}
                >
                  다음 →
                </button>
              </div>
            </>
          )}

          {currentStep === 2 && (
            <>
              <div className="grid grid-cols-2 gap-6">
                {/* 닉네임 */}
                <div className="flex items-center gap-4">
                  <div className="w-[30%] font-semibold text-gray-700">
                    닉네임
                  </div>
                  <input
                    type="text"
                    value={form.nickname}
                    onChange={(e) => handleChange("nickname", e.target.value)}
                    className="p-2 input input-sm w-full shadow rounded-md border"
                  />
                </div>

                {/* 이메일: 같은 셀 내부 세로 스택 */}
                <div className="flex flex-col gap-1">
                  <div className="flex items-center gap-4">
                    <div className="w-[30%] font-semibold text-gray-700">
                      이메일
                    </div>
                    <div className="flex items-center gap-2 w-full min-w-0">
                      <input
                        id="email"
                        type="email"
                        value={form.email}
                        onChange={(e) => handleChange("email", e.target.value)}
                        className="p-2 input input-sm flex-1 min-w-0 shadow rounded-md border"
                        disabled={emailVerified}
                      />
                      <button
                        id="emailSendBtn"
                        type="button"
                        className={pillBtn}
                        onClick={onSendEmailCode}
                        disabled={emailVerified || emailSending}
                      >
                        {emailVerified ? "인증 완료" : "이메일 인증"}
                      </button>
                    </div>
                  </div>

                  {/* 안내문 & 인증박스(바로 밑) */}
                  <p className="pl-[30%] text-xs text-gray-500 flex items-center gap-1">
                    <span aria-hidden>ⓘ</span> 이메일 변경 시 인증이 필요합니다.
                  </p>

                  {emailShowBox && !emailVerified && (
                    <div className="pl-[30%] flex items-center gap-2">
                      <input
                        id="emailVerificationCode"
                        type="text"
                        value={emailCode}
                        onChange={(e) => {
                          setEmailCode(e.target.value);
                          setEmailCodeWarn("");
                        }}
                        placeholder="인증번호 입력"
                        className="p-2 input input-sm w-[240px] shadow rounded-md border"
                      />
                      <button
                        id="emailCheckBtn"
                        type="button"
                        className="px-4 h-9 rounded-full border hover:bg-gray-50"
                        onClick={onVerifyEmailCode}
                      >
                        확인
                      </button>
                    </div>
                  )}
                  {emailWarn && (
                    <p
                      id="emailWarning"
                      className="pl-[30%] text-xs text-red-600"
                    >
                      {emailWarn}
                    </p>
                  )}
                  {emailCodeWarn && (
                    <p
                      id="emailCodeWarning"
                      className="pl-[30%] text-xs text-red-600"
                    >
                      {emailCodeWarn}
                    </p>
                  )}
                </div>

                {/* 주소 */}
                <div className="flex items-center gap-4">
                  <div className="w-[30%] font-semibold text-gray-700">
                    주소
                  </div>
                  <input
                    type="text"
                    value={form.address}
                    onChange={(e) => handleChange("address", e.target.value)}
                    className="p-2 input input-sm w-full shadow rounded-md border"
                  />
                </div>
                {/* 전화번호 */}
                <div className="flex flex-col gap-1">
                  <div className="flex items-center gap-4">
                    <div className="w-[30%] font-semibold text-gray-700">
                      휴대폰 번호
                    </div>
                    <div className="flex items-center gap-2 w-full min-w-0">
                      <input
                        id="cellphone"
                        type="text"
                        value={form.cellphone}
                        onChange={(e) =>
                          handleChange("cellphone", e.target.value)
                        }
                        className="p-2 input input-sm flex-1 min-w-0 shadow rounded-md border"
                        disabled={phoneVerified}
                      />
                      <button
                        id="phoneSendBtn"
                        type="button"
                        className={pillBtn}
                        onClick={onSendPhoneCode}
                        disabled={
                          phoneVerified || phoneSending || cooldownRemain > 0
                        }
                      >
                        {phoneVerified ? "인증 완료" : "휴대폰 인증"}
                      </button>
                    </div>
                  </div>

                  <p className="pl-[30%] text-xs text-gray-500 flex items-center gap-1">
                    <span aria-hidden>ⓘ</span> 휴대폰 번호 변경 시 인증이
                    필요합니다.
                  </p>

                  {phoneShowBox && !phoneVerified && (
                    <div className="pl-[30%] flex items-center gap-2">
                      <input
                        id="verificationCode"
                        type="text"
                        value={phoneCode}
                        onChange={(e) => {
                          setPhoneCode(e.target.value);
                          setPhoneCodeWarn("");
                        }}
                        placeholder="인증번호 입력"
                        className="p-2 input input-sm w-[240px] shadow rounded-md border"
                      />
                      <button
                        type="button"
                        className="px-4 h-9 rounded-full border hover:bg-gray-50"
                        onClick={onVerifyPhoneCode}
                      >
                        확인
                      </button>
                    </div>
                  )}
                  {phoneWarn && (
                    <p
                      id="cellphoneWarning"
                      className="pl-[30%] text-xs text-red-600"
                    >
                      {phoneWarn}
                    </p>
                  )}
                  {phoneCodeWarn && (
                    <p
                      id="codeWarning"
                      className="pl-[30%] text-xs text-red-600"
                    >
                      {phoneCodeWarn}
                    </p>
                  )}
                </div>
              </div>

              {/* 버튼 */}
              <div className="flex justify-between mt-6">
                <button
                  type="button"
                  className="btn"
                  onClick={() => setCurrentStep(1)}
                >
                  ← 이전
                </button>
                <button type="submit" className="btn btn-primary">
                  수정하기
                </button>
              </div>
            </>
          )}
        </div>
      </form>
    </div>
  );
}
