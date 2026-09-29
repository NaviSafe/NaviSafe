import { useEffect } from "react";
import { useCurrentLocation } from "../hooks/useCurrentLocation";

export const LocationPollingProvider = () => {
  const { getLocation } = useCurrentLocation();

  useEffect(() => {
    getLocation(); // 초기 1회

    const interval = setInterval(() => {
      getLocation();
    }, 2000);

    return () => clearInterval(interval);
  }, []);

  return null;
};