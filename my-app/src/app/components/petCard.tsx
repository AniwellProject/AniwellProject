"use client";

import React from "react";

type Pet = {
  id: number;
  memberId: number;
  name: string;
  species: string;
  breed: string;
  gender: string; // "M" | "F"
  birthDate: string | Date;
  weight: number;
  photo?: string | null;
  createdAt?: string | Date;
  updatedAt?: string | Date;
};

type Props = {
  pet: Pet;
  bgImage?: string;
  logo?: string;
  className?: string;
  /** 카드 전체 배율 (0.5 ~ 1.0 등) */
  scale?: number;
  /** 카드 클릭 핸들러(선택) */
  onClick?: () => void;
};

function formatId(id: number) {
  const s = id.toString().padStart(6, "0");
  return `${s.slice(0, 3)} ${s.slice(3)}`;
}

function formatDate(d?: string | Date) {
  if (!d) return "";
  const date = typeof d === "string" ? new Date(d) : d;
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${dd}`;
}
function calcAge(b?: string | Date) {
  if (!b) return "";
  const birth = typeof b === "string" ? new Date(b) : b;
  const now = new Date();
  let y = now.getFullYear() - birth.getFullYear();
  const mDiff = now.getMonth() - birth.getMonth() || 0;
  if (mDiff < 0 || (mDiff === 0 && now.getDate() < birth.getDate())) y--;
  return `${y}y`;
}

export default function PetIdCardLite({
  pet,
  bgImage = "https://imgur.com/OJI4yzC.png",
  logo = "https://imgur.com/rcOcaL6.png",
  className = "",
  scale = 1,
  onClick,
}: Props) {
  const numberLike = formatId(pet.id);
  const birth = formatDate(pet.birthDate);
  const age = calcAge(pet.birthDate);
  const gender = (pet.gender || "").toUpperCase();
  const genderMark = gender.startsWith("F") ? "♀" : gender.startsWith("M") ? "♂" : "";

  return (
    <div
      className={`pet-card-wrap ${className}`}
      style={{ "--s": String(scale) } as React.CSSProperties}
    >
      <article className="pet-card" onClick={onClick}>
        {/* 배경 */}
        <div className="pet-card__bg" style={{ backgroundImage: `url(${bgImage})` }} />
        <div className="pet-card__tint" />
        <div className="pet-card__band" />

        {/* 로고 */}
        <img className="pet-card__logo" src={logo} alt="brand" />

        {/* 아바타 */}
        {pet.photo ? (
          <figure className="pet-card__avatar">
            <img src={pet.photo} alt={pet.name || "Unknown"} />
          </figure>
        ) : (
          <div className="pet-card__avatar pet-card__avatar--placeholder">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" aria-hidden>
              <circle cx="12" cy="8" r="3" stroke="currentColor" strokeWidth="1.5" />
              <path d="M5 19c1.5-3 4.5-5 7-5s5.5 2 7 5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
            </svg>
          </div>
        )}

        {/* 본문 */}
        <div className="pet-card__body">
          <div className="pet-card__id">{numberLike}</div>

          <div className="pet-card__section">
            <div className="pet-card__label">Name</div>
            <div className="pet-card__name" title={pet.name}>
              {pet.name}
              {genderMark && <span className="pet-card__gender">{genderMark}</span>}
            </div>

            <div className="pet-card__meta">
              <span className="pet-card__chip">{pet.species || "PET"}</span>
              {pet.breed && <span className="pet-card__chip pet-card__chip--dim">{pet.breed}</span>}
              {birth && (
                <span className="pet-card__chip">
                  Born {birth}
                  {age && ` (${age})`}
                </span>
              )}
              {Number.isFinite(pet.weight) && <span className="pet-card__chip">{pet.weight} kg</span>}
            </div>
          </div>
        </div>

        {/* 테두리 */}
        <div className="pet-card__ring" />
      </article>

      {/* 바닥 그림자 */}
      <div className="pet-card__shadow" />
    </div>
  );
}
