type RouteResetModalProps = {
    open: boolean;
    onCancel: () => void;
    onConfirm: () => void;
    };

export const RouteResetModal = ({
    open,
    onCancel,
    onConfirm,
}: RouteResetModalProps) => {
    if (!open) return null;

    return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div className="bg-white rounded-xl p-5 w-[80%] max-w-sm shadow-lg">

        <div className="text-center text-sm mb-4 leading-relaxed">
            기존 경로가 초기화됩니다.<br />
            네비게이션을 시작하시겠습니까?
        </div>

        <div className="flex gap-2">
            
            <button
            className="flex-1 bg-gray-200 py-2 rounded-lg"
            onClick={onCancel}
            >
            취소
            </button>

            <button
            className="flex-1 bg-blue-500 text-white py-2 rounded-lg"
            onClick={onConfirm}
            >
            확인
            </button>

        </div>

        </div>
    </div>
    );
};