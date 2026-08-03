import { Fragment } from "react";

interface DisasterLogListProps {
    open: boolean;
    onClose: () => void;
}

const mockDisasters = [
    {
        id: 1,
        name: "산불",
        startTime: "2026-07-06 14:10",
        endTime: "-",
    },
    {
        id: 2,
        name: "호우",
        startTime: "2026-07-06 12:30",
        endTime: "-",
    },
    {
        id: 3,
        name: "폭염",
        startTime: "2026-07-06 09:00",
        endTime: "-",
    },
    {
        id: 4,
        name: "지진",
        startTime: "2026-07-05 22:17",
        endTime: "2026-07-05 22:40",
    },
    {
        id: 5,
        name: "강풍",
        startTime: "2026-07-05 18:25",
        endTime: "2026-07-05 20:10",
    },
    {
        id: 6,
        name: "한파",
        startTime: "2026-01-14 05:00",
        endTime: "2026-01-14 16:00",
    },
    ];

    export const DisasterLogList = ({
    open,
    onClose,
    }: DisasterLogListProps) => {
    if (!open) return null;

    return (
        <Fragment>
        {/* 배경 */}
        <div
            className="fixed inset-0 bg-black/40 z-50"
            onClick={onClose}
        />

        {/* 팝업 */}
        <div className="fixed left-1/2 top-1/2 z-[60] w-[90%] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl bg-white shadow-xl">
            {/* Header */}
            <div className="flex items-center justify-between border-b px-5 py-4">
            <h2 className="text-lg font-bold">
                현재 발생 재난
            </h2>

            <button
                onClick={onClose}
                className="text-xl text-gray-500 hover:text-black"
            >
                ✕
            </button>
            </div>

            {/* Body */}
            <div className="max-h-[450px] overflow-y-auto p-4 space-y-3">
            {mockDisasters.map((item) => (
                <div
                key={item.id}
                className="rounded-xl border border-gray-200 p-4 shadow-sm"
                >
                <div className="mb-2 text-base font-semibold text-red-600">
                    {item.name}
                </div>

                <div className="text-sm text-gray-600">
                    <div>
                    <span className="font-medium">발생시간</span>
                    <span className="ml-2">{item.startTime}</span>
                    </div>

                    <div className="mt-1">
                    <span className="font-medium">종료시간</span>
                    <span className="ml-2">{item.endTime}</span>
                    </div>
                </div>
                </div>
            ))}
            </div>
        </div>
        </Fragment>
    );
};