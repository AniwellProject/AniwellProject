"use client";

import Sidebar from "../components/side/Sidebar";
import { useEffect, useState } from "react";
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

  // ✅ 추가: 소셜 로그인 여부 판단용
  socialProvider?: string | null;
  socialId?: string | null;
};

export default function MyPage() {
  const router = useRouter();
  const [member, setMember] = useState<Member | null>(null);

  const handleEditClick = () => {
    const isSocial =
      !!member?.socialProvider && String(member.socialProvider).trim() !== "";
    if (isSocial) {
      router.push("/my-page/edit");      // 소셜: 비번 확인 생략
    } else {
      router.push("/my-page/checkPw");   // 일반: 기존 흐름 유지
    }
  };

  useEffect(() => {
    fetch("http://localhost:8080/api/member/getUsrInfo", {
      credentials: "include",
    })
      .then((res) => {
        if (!res.ok) throw new Error("로그인 필요");
        return res.json();
      })
      .then((data) => {
        console.log("✅ 불러온 회원정보", data);
        setMember(data);
      })
      .catch((err) => {
        alert("로그인이 필요합니다.");
        console.error(err);
      });
  }, []);

  if (!member) return <p>로딩 중...</p>;

  return (
    <div className="flex mx-auto justify-center">
      <div className="p-6">
        <Sidebar />
      </div>
      <div className="p-6 mt-[-10px] bg-white rounded-lg shadow w-full min-w-3xl mx-auto">
        <div className="mb-6">
          <h2 className="text-xl w-full font-bold border-b pb-">회원정보</h2>
        </div>
        <div className= "grid grid-cols-3 gap-10 justify-around mt-10">
          {/* 프로필 이미지 */}
          <div className="flex justify-center">
            <div className="w-40 h-40 rounded-full overflow-hidden bg-yellow-200">
              <img
                src={
                  member.photo && member.photo.trim() !== ""
                    ? member.photo
                    : "https://i.imgur.com/OJI4yzC.png"
                }
                alt="프로필"
                className="w-full h-full object-cover"
              />
            </div>
          </div>

          {/* 회원 정보 텍스트 */}
          <div className="text-sm space-y-2 w-full">
            <p>
              <strong>ID:</strong> {member.loginId}
            </p>
            {member.email && (
              <p>
                <strong>이메일:</strong> {member.email}
              </p>
            )}
            {member.authName && (
              <p>
                <strong>등급:</strong> {member.authName}
              </p>
            )}
          </div>
          <div className="text-sm space-y-2 w-full">
            <p>
              <strong>이름:</strong> {member.name}
            </p>
            <p>
              <strong>닉네임:</strong> {member.nickname}
            </p>
            <p>
              <strong>주소:</strong> {member.address}
            </p>
          </div>
        </div>

        <div className="flex justify-end mt-6">
          <button
            onClick={handleEditClick}
            className="bg-yellow-200 hover:bg-yellow-300 text-black px-4 py-2 rounded-md shadow"
          >
            회원정보 수정
          </button>
        </div>
      </div>
    </div>
  );
}
