import { Fragment } from "react";
import { useOutbreakOccurState } from "../store/outbreakOccurStore";

interface DisasterLogListProps {
    open: boolean;
    onClose: () => void;
}

export const DisasterLogList = ({
    open,
    onClose,
}: DisasterLogListProps) => {
    const {outbreakOccurList} = useOutbreakOccurState();

    if (!open) return null;

    return (
        <Fragment>
            {/* 배경 */}
            <div
                className="fixed inset-0 z-50 bg-black/40"
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
                <div className="max-h-[450px] space-y-3 overflow-y-auto p-4">
                    {outbreakOccurList.length === 0 ? (
                        <div className="py-10 text-center text-sm text-gray-500">
                            현재 발생한 재난이 없습니다.
                        </div>
                    ) : (
                        outbreakOccurList.map((item) => (
                            <div
                                key={item.accId}
                                className="rounded-xl border border-gray-200 p-2 shadow-sm"
                            >
                                {/* 재난 종류 */}
                                <div className="mb-2 flex items-center gap-2">
                                    <span className="rounded-md bg-red-50 px-2 py-1 text-sm font-semibold text-red-600">
                                        {item.accTypeName}
                                    </span>

                                    <span className="text-sm font-medium text-gray-700">
                                        {item.accDetailTypeName}
                                    </span>
                                </div>

                                <div className="text-left">
                                    <div className="mb-1 text-xs font-medium text-gray-400">
                                        내용
                                    </div>

                                    <div className="text-sm leading-relaxed text-gray-700">
                                        {item.accInfo}
                                    </div>
                                </div>

                                {/* 종료시간 */}
                                <div className="mt-3 flex items-center justify-center">
                                    <span className="text-xs font-medium text-gray-400">
                                        종료시간
                                    </span>

                                    <span className="ml-2 text-sm text-gray-600">
                                        {formatDate(item.expClrDate) || "-"}
                                    </span>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </Fragment>
    );
};

const formatDate = (dateString: string) => {
    if (!dateString) return "-";

    return new Date(dateString).toLocaleString("ko-KR", {
        timeZone: "Asia/Seoul",
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
    });
};