import { useSelectedShelter } from "../store/selectedShelterStore";

export const BottomBar = () => {
    const { selectedShelter } = useSelectedShelter();

    if (!selectedShelter) return null;

    const fontSize =
        selectedShelter.name.length > 10
        ? "12px"
        : selectedShelter.name.length > 5
        ? "14px"
        : "16px";

    const handleNavigate = () => {
        console.log(selectedShelter);
        // TODO: 나중에 경로 찾기 기능 연결
    };

    return (
        <div className="absolute bottom-0 left-0 w-full bg-white p-4 flex justify-between items-center shadow-lg z-50">
        <span className="font-medium" style={{ fontSize }}>
            {selectedShelter.name}
        </span>

        <button
            className="bg-blue-500 text-white px-4 py-2 rounded-lg"
            onClick={handleNavigate}
        >
            여기로 이동
        </button>
        </div>
    );
};