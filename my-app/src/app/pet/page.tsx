// app/test/pet-card-lite/page.tsx  (Next.js App Router 구조)
import PetIdCardLite from "@/components/petCard";

export default function Page() {
  const testPet = {
    id: 1,
    memberId: 1,
    name: "MOZZI",
    species: "DOG",
    breed: "Pomeranian",
    gender: "F",
    birthDate: "2021-05-15",
    weight: 2.8,
    photo: "https://imgur.com/OJI4yzC.png", // 펫 프로필 이미지
    createdAt: "2025-07-01",
    updatedAt: "2025-07-10"
  };

  return (
    <main className="min-h-dvh grid place-items-center bg-gradient-to-r from-sky-50 to-indigo-50 p-8">
      <PetIdCardLite pet={testPet} />
    </main>
  );
}
