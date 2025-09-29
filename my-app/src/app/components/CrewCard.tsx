type Props = {
  name: string;
  intro?: string;
  url: string;
  onClick?: () => void;
};

export default function CrewCard({ name, intro, url, onClick }: Props) {
  return (
    <div
      className="flex items-center gap-4 bg-yellow-100 px-4 py-3 rounded-xl shadow hover:shadow-lg transition cursor-pointer"
      onClick={onClick}
    >
      {/* 원형 이미지 */}
      <div className="w-12 h-12 rounded-full overflow-hidden bg-yellow-300 flex-shrink-0">
        <img
          src={url || "https://i.imgur.com/OJI4yzC.png"}
          alt="크루 이미지"
          className="w-full h-full object-cover"
        />
      </div>

      {/* 크루 소개 */}
      <div className="text-sm">
        <p className="font-semibold">{name}</p>
        {intro && <p className="text-gray-600">{intro}</p>}
      </div>
    </div>
  );
}
