import { useSelectedShelter } from "../store/selectedShelterStore";
import { useNavigate } from "react-router-dom";

export const BottomPathFindingBar = () => {
    const { selectedShelter } = useSelectedShelter();
    const navigate = useNavigate();


    if (selectedShelter) return null;

    const openStartDestinationAddressPage = () => {
        navigate("/src-dest");
    }

    return (
        <div className="absolute bottom-0 left-0 w-full bg-transparent p-4 flex flex-row-reverse justify-between items-center shadow-lg z-50">
            <button
                className="bg-blue-500 text-white px-4 py-2 rounded-lg"
                onClick={openStartDestinationAddressPage}
            >
                네비찾기
            </button>
        </div>
    );
};