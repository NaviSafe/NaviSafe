import { Fragment, useState } from "react";

import { useOutbreakOccurState } from "../store/outbreakOccurStore";
import { useOutbreakFilterStore } from "../store/outbreakFilterStore";

interface DisasterLogListProps {
    open: boolean;
    onClose: () => void;
}

const OUTBREAK_TYPES = [
    "교통사고",
    "차량고장",
    "보행사고",
    "공사",
    "낙하물",
    "버스사고",
    "지하철사고",
    "화재",
    "기상/재난",
    "집회및행사",
    "기타",
    "제보",
    "단순정보",
];

export const DisasterLogList = ({
    open,
    onClose,
}: DisasterLogListProps) => {
    const {outbreakOccurList} = useOutbreakOccurState();

    const {
        excludeAccTypeNames,
        toggleExcludeAccTypeName,
        clearExcludeAccTypeNames,
    } = useOutbreakFilterStore();

    const [openFilter, setOpenFilter] =
        useState(false);

    if (!open) return null;

    return (
        <Fragment>
            {/* 배경 */}
            <div
                className="fixed inset-0 z-50 bg-black/40"
                onClick={onClose}
            />

            {/* 모달 */}
            <div className="fixed left-1/2 top-1/2 z-[60] w-[90%] max-w-md -translate-x-1/2 -translate-y-1/2 rounded-2xl bg-white shadow-xl">

                {/* Header */}
                <div className="relative border-b px-5 py-4">
                    <div className="flex items-center justify-between">
                        <h2 className="text-lg font-bold">
                            현재 발생한 돌발상황
                        </h2>

                        <button
                            onClick={onClose}
                            className="text-xl text-gray-500 hover:text-black"
                        >
                            ✕
                        </button>
                    </div>

                    {/* 제외 유형 설정 */}
                    <div className="relative mt-3">
                        <button
                            onClick={() =>
                                setOpenFilter(
                                    (prev) => !prev
                                )
                            }
                            className="flex w-full items-center justify-between rounded-xl border border-gray-200 bg-gray-50 px-3 py-2 text-sm"
                        >
                            <span className="font-medium text-gray-700">
                                제외 유형 설정

                                {excludeAccTypeNames.length >
                                    0 && (
                                    <span className="ml-2 text-blue-500">
                                        {
                                            excludeAccTypeNames.length
                                        }
                                        개 제외
                                    </span>
                                )}
                            </span>

                            <span className="text-gray-500">
                                {openFilter
                                    ? "▲"
                                    : "▼"}
                            </span>
                        </button>

                        {/* 유형 선택 Overlay */}
                        {openFilter && (
                            <div className="absolute left-0 right-0 top-full z-[70] mt-2 rounded-xl border border-gray-200 bg-white p-2 shadow-xl">
                                <div className="flex items-center justify-between px-3 py-2">
                                    <span className="text-xs text-gray-400">
                                        표시하지 않을 돌발상황을 선택하세요
                                    </span>

                                    {excludeAccTypeNames.length >
                                        0 && (
                                        <button
                                            onClick={
                                                clearExcludeAccTypeNames
                                            }
                                            className="text-xs text-blue-500 hover:underline"
                                        >
                                            전체 해제
                                        </button>
                                    )}
                                </div>

                                <div className="max-h-52 overflow-y-auto">
                                    {OUTBREAK_TYPES.map(
                                        (type) => {
                                            const checked =
                                                excludeAccTypeNames.includes(
                                                    type
                                                );

                                            return (
                                                <label
                                                    key={
                                                        type
                                                    }
                                                    className="flex cursor-pointer items-center rounded-lg px-3 py-2 hover:bg-gray-50"
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={
                                                            checked
                                                        }
                                                        onChange={() =>
                                                            toggleExcludeAccTypeName(
                                                                type
                                                            )
                                                        }
                                                        className="mr-3 h-4 w-4"
                                                    />

                                                    <span className="text-sm text-gray-700">
                                                        {
                                                            type
                                                        }
                                                    </span>
                                                </label>
                                            );
                                        }
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* 돌발상황 목록 */}
                <div className="max-h-[450px] space-y-3 overflow-y-auto p-4">
                    {outbreakOccurList.length ===
                    0 ? (
                        <div className="py-10 text-center text-sm text-gray-500">
                            현재 발생한 돌발상황이 없습니다.
                        </div>
                    ) : (
                        outbreakOccurList.map(
                            (item) => (
                                <div
                                    key={
                                        item.accId
                                    }
                                    className="rounded-xl border border-gray-200 p-2 shadow-sm"
                                >
                                    <div className="mb-2 flex items-center gap-2">
                                        <span className="rounded-md bg-red-50 px-2 py-1 text-sm font-semibold text-red-600">
                                            {
                                                item.accTypeName
                                            }
                                        </span>

                                        <span className="text-sm font-medium text-gray-700">
                                            {
                                                item.accDetailTypeName
                                            }
                                        </span>
                                    </div>

                                    <div className="text-left">
                                        <div className="mb-1 text-xs font-medium text-gray-400">
                                            내용
                                        </div>

                                        <div className="text-sm leading-relaxed text-gray-700">
                                            {
                                                item.accInfo
                                            }
                                        </div>
                                    </div>

                                    <div className="mt-3 flex items-center justify-center">
                                        <span className="text-xs font-medium text-gray-400">
                                            종료시간
                                        </span>

                                        <span className="ml-2 text-sm text-gray-600">
                                            {formatDate(
                                                item.expClrDate
                                            ) || "-"}
                                        </span>
                                    </div>
                                </div>
                            )
                        )
                    )}
                </div>
            </div>
        </Fragment>
    );
};

const formatDate = (
    dateString: string
) => {
    if (!dateString) return "-";

    return new Date(
        dateString
    ).toLocaleString("ko-KR", {
        timeZone: "Asia/Seoul",
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false,
    });
};