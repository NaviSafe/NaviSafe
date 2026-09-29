import { useSelectedShelter } from "../store/selectedShelterStore";
import { useNavigate } from "react-router-dom";
import { useRouteStore } from "../store/routeStore";
import { Fragment, useState } from "react";

import { RouteResetModal } from "./RouteResetModal";


export const BottomPathFindingBar = () => {
    const { selectedShelter } = useSelectedShelter();
    const navigate = useNavigate();
    const { routeCoords, distance, resetRoute } = useRouteStore();
    const [open, setOpen] = useState<boolean>(false);


    if (selectedShelter) return null;

    const hasRoute = routeCoords.length > 0 || distance > 0;

    const handleClick = () => {
        if (hasRoute) {
          setOpen(true); // 기존 경로 있으면 모달
        } else {
          navigate("/src-dest"); // 없으면 바로 이동
        }
    };

    const handleCancel = () => {
        setOpen(false);
    };

    const handleConfirm = () => {
        resetRoute();
        setOpen(false);
        navigate("/src-dest");
    };

    return (
        <Fragment>
        <div className="absolute bottom-0 left-0 w-full bg-transparent p-4 flex flex-row-reverse justify-between items-center shadow-lg z-50">
            <button
                className="bg-blue-500 text-white px-4 py-2 rounded-lg"
                onClick={handleClick}
            >
                네비찾기
            </button>
        </div>
        <RouteResetModal
            open={open}
            onCancel={handleCancel}
            onConfirm={handleConfirm}
        />
        </Fragment>
    );
};