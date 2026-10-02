import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import { Canvas } from "@react-three/fiber";
import * as THREE from "three";

interface BuildingGeometry {
  height: number;
  geom: string;
}

interface GeoJsonMultiPolygon {
  type: "MultiPolygon";
  coordinates: [number, number][][][];
}

const longitude = 126.96320522724326;
const latitude = 37.559747577258186;

export const Navigation3D = () => {
  const [buildings, setBuildings] = useState<BuildingGeometry[]>([]);

  useEffect(() => {
    const fetchBuildings = async () => {
      try {
        const response = await axios.post<BuildingGeometry[]>(
          `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/navigation3D/buildings`,
          {
            longitude,
            latitude,
          }
        );

        console.log("3D Buildings:", response.data);

        setBuildings(response.data);
      } catch (error) {
        console.error("3D 건물 조회 실패:", error);
      }
    };

    fetchBuildings();
  }, []);

  return (
    <div className="w-full h-screen">
      <Canvas camera={{ position: [0, 300, 300], fov: 60 }}>
        <ambientLight intensity={1} />

        <directionalLight
          position={[10, 20, 10]}
          intensity={2}
        />

        {buildings.map((building, index) => (
          <Building
            key={index}
            building={building}
          />
        ))}

        <gridHelper args={[200, 20]} />
      </Canvas>
    </div>
  );
};

interface BuildingProps {
  building: BuildingGeometry;
}

const Building = ({ building }: BuildingProps) => {
  const geometries = useMemo(() => {
    const geoJson: GeoJsonMultiPolygon = JSON.parse(building.geom);

    return geoJson.coordinates.map((polygon) => {
      const ring = polygon[0];

      const shape = new THREE.Shape();

      ring.forEach(([lon, lat], index) => {
        const x = longitudeToMeter(lon, longitude);
        const z = latitudeToMeter(lat, latitude);

        if (index === 0) {
          shape.moveTo(x, z);
        } else {
          shape.lineTo(x, z);
        }
      });

      return new THREE.ExtrudeGeometry(shape, {
        depth: building.height || 1,
        bevelEnabled: false,
      });
    });
  }, [building]);

  return (
    <>
      {geometries.map((geometry, index) => (
        <mesh
          key={index}
          geometry={geometry}
          rotation-x={-Math.PI / 2}
        >
          <meshStandardMaterial />
        </mesh>
      ))}
    </>
  );
};

const longitudeToMeter = (
  targetLongitude: number,
  originLongitude: number
) => {
  const metersPerDegree = 111320;

  return (
    (targetLongitude - originLongitude) *
    metersPerDegree *
    Math.cos((latitude * Math.PI) / 180)
  );
};

const latitudeToMeter = (
  targetLatitude: number,
  originLatitude: number
) => {
  const metersPerDegree = 111320;

  return (
    (targetLatitude - originLatitude) *
    metersPerDegree
  );
};
