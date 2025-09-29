"use client";

import Sidebar from "../components/side/Sidebar";
import CrewCard from "../components/CrewCard";
import Image from "next/image";
import { useEffect, useState } from "react";

type WalkCrew = {
  id: number; // 크루 ID
  title: string; // 크루 제목
  description: string; // 크루 설명
  districtId: number; // district 테이블의 id (FK)
  leaderId: number; // 작성자 ID
  createdAt: string; // 생성일시 (ISO 문자열로 가정)
  imageUrl: string;

  // JOIN 조회용 필드 (optional)
  nickname?: string; // 작성자 닉네임
  city?: string; // 시
  district?: string; // 구
  dong?: string; // 동
};

type Article = {
  id: number;
  title: string;
  body: string;
  createdAt: string;
};

const leaderCrew = [
  { id: 1, title: "궁동 아침크루", description: "매일 아침 7시에 모입니다." },
  { id: 2, title: "반려견 산책", description: "주말마다 유성천에서 모여요." },
];

export default function CrewPage() {
  const [crews, setCrews] = useState<WalkCrew[]>([]);
  const [loginedId, setLoginedId] = useState<number | null>(null);
  const [joinedCrews, setJoinedCrews] = useState<WalkCrew[]>([]);
  const [msg, setMsg] = useState<string | null>(null); // ✅

  useEffect(() => {
    fetch("http://localhost:8080/usr/member/myPage", {
      method: "GET",
      credentials: "include",
    })
      .then((res) => res.json())
      .then((data) => {
        if (data && data.id) {
          setLoginedId(data.id);
        }
      });
  }, []);

  const myLeaderCrews = crews.filter((crew) => crew.leaderId === loginedId);

  useEffect(() => {
  fetch("http://localhost:8080/usr/crewCafe/usr/crew/myCrewCafe", {
    method: "GET",
    credentials: "include",
  })
    .then((res) => {
      if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
      return res.json();
    })
    .then((data) => {
      if (data.resultCode === "S-1") {
        const { myCrews, joinedCrews } = data.data1;

        if (Array.isArray(myCrews)) {
          setCrews(myCrews);
        } else if (myCrews) {
          setCrews([myCrews]);
        }

        if (Array.isArray(joinedCrews)) {
          setJoinedCrews(joinedCrews);
        } else if (joinedCrews) {
          setJoinedCrews([joinedCrews]);
        }
      } else {
        setMsg(data.msg);
      }
    })
    .catch((err) => {
      console.error("❌ fetch 실패:", err);
      setMsg("서버와 통신 중 오류가 발생했습니다.");
    });
}, []);


  return (
    <div className="flex mx-auto jutify-center h-full ">
      <div className="p-6">
        <Sidebar />
      </div>
      <div className="p-6 mt-[-20px] bg-white rounded-lg w-full shadow min-w-3xl mx-auto flex justify-around">
        {/* 좌측: 참가한 크루 목록 */}
        <div className="flex-1 pr-6">
          <div className="">
            <h2 className="text-xl font-bold border-b pb-3">참가한 크루</h2>
          </div>

          <div className="space-y-2 overflow-y-auto h-[80%] mt-4">
            {joinedCrews.length > 0 ? (
              joinedCrews.map((crew) => (
                <CrewCard
                  key={crew.id}
                  url={crew.imageUrl}
                  name={crew.title}
                  intro={crew.description}
                  onClick={() => {
                    window.top.location.href = `http://localhost:8080/usr/crewCafe/cafeHome?crewId=${crew.id}`;
                  }}
                />
              ))
            ) : (
              <p className="text-sm text-gray-400">참가한 크루가 없습니다.</p>
            )}
          </div>
        </div>

        {/* 우측: 내가 만든 크루 */}
        <div className="w-[350px] h-[350px] rounded-xl overflow-hidden shadow ml-6 p-4 flex flex-col justify-between bg-gray-50">
          <h2 className="text-lg font-bold text-gray-800 mb-2">나의 크루</h2>
          <div className="space-y-3 overflow-y-auto">
            {crews.length > 0 ? (
              crews.map((crew) => (
                <div
                  key={crew.id}
                  className="bg-white rounded-lg p-3 shadow hover:shadow-md transition cursor-pointer"
                  onClick={() =>
                    (window.top.location.href = `http://localhost:8080/usr/crewCafe/cafeHome?crewId=${crew.id}`)
                  }
                >
                  <p className="font-semibold text-sm">{crew.title}</p>
                  <p className="text-xs text-gray-500 truncate">
                    {crew.description}
                  </p>
                </div>
              ))
            ) : (
              <p className="text-sm text-gray-400">
                내가 만든 크루가 없습니다.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
