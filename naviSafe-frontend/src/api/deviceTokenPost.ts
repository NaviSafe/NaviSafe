import axios from "axios";

export async function registerDeviceToken(token: string) {
    const response = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/fcm/register`,
        { deviceToken: token },
    {
        headers: {
        "Content-Type": "application/json"
        },
    },
    );
    return response.data;
}