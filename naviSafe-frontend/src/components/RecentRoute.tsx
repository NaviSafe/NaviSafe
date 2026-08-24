import { MdHistory } from "react-icons/md";

interface RecentRouteProps {
    from: string;
    to: string;
}

export const RecentRoute = ({
    from,
    to,
}: RecentRouteProps) => {
    return (
        <div
            className="
                min-h-[58px]
                px-4
                py-2
                flex
                items-center
                gap-3
                border-b
                border-gray-100
                cursor-pointer
                hover:bg-gray-50
            "
        >
            {/* 최근 경로 아이콘 */}
            <div
                className="
                    flex-shrink-0
                    w-7
                    h-7
                    rounded-full
                    bg-gray-300
                    flex
                    items-center
                    justify-center
                    text-white
                "
            >
                <MdHistory size={17} />
            </div>

            {/* 출발지 → 도착지 */}
            <div
                className="
                    flex-1
                    text-sm
                    text-gray-700
                    leading-5
                "
            >
                <span>{from}</span>

                <span className="mx-1 font-semibold text-gray-500">
                    →
                </span>

                <span>{to}</span>
            </div>
        </div>
    );
};