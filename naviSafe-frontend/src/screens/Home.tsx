import { KakaoMap } from "../components/KakaoMap";

export const Home = () => {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center justify-center px-4 text-center">
      <h1 className="text-2xl sm:text-3xl md:text-4xl lg:text-5xl font-bold text-blue-500 mb-4">
        TailwindCSS 테스트 성공! 🎉
      </h1>
      <p className="text-gray-600 text-sm sm:text-base md:text-lg">
        반응형으로 잘 보이죠? 📱💻
      </p>

      <KakaoMap />
    </div>
  );
}
