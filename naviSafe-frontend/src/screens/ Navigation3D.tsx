import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import { Canvas } from "@react-three/fiber";
import * as THREE from "three";

interface RoadGeometry {
    id: number;
    geom: string;
}

interface BuildingGeometry {
    height: number;
    geom: string;
}

interface GeoJsonMultiPolygon {
    type: "MultiPolygon";
    coordinates: [number, number][][][];
}

interface GeoJsonMultiLineString {
    type: "MultiLineString";
    coordinates: [number, number][][];
}

const longitude = 126.96320522724326;
const latitude = 37.559747577258186;

export const Navigation3D = () => {
  const [buildings, setBuildings] = useState<BuildingGeometry[]>([]);
  const [roads, setRoads] = useState<RoadGeometry[]>([]);

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

    const fetchRoads = async () => {
        try {
          const response = await axios.post<RoadGeometry[]>(
            `${import.meta.env.VITE_API_BASE_URL}/api/naviSafe/navigation3D/roads`,
            {
              longitude,
              latitude,
            }
          );
      
          console.log("3D Roads:", response.data);
      
          setRoads(response.data);
        } catch (error) {
          console.error("3D 도로 조회 실패:", error);
        }
      };

    fetchBuildings();
    fetchRoads();
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

        {roads.map((road) => (
        <Road
            key={road.id}
            road={road}
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

interface RoadProps {
    road: RoadGeometry;
  }
  
const Road = ({ road }: RoadProps) => {
const geometries = useMemo(() => {
    const geoJson: GeoJsonMultiLineString = JSON.parse(road.geom);

    return geoJson.coordinates.flatMap((lineString) => {
    const geometries: THREE.BufferGeometry[] = [];

    for (let i = 0; i < lineString.length - 1; i++) {
        const [lon1, lat1] = lineString[i];
        const [lon2, lat2] = lineString[i + 1];

        const x1 = longitudeToMeter(lon1, longitude);
        const z1 = -latitudeToMeter(lat1, latitude);

        const x2 = longitudeToMeter(lon2, longitude);
        const z2 = -latitudeToMeter(lat2, latitude);

        const dx = x2 - x1;
        const dz = z2 - z1;

        const length = Math.sqrt(dx * dx + dz * dz);

        if (length === 0) {
        continue;
        }

        // 도로 폭 4m
        const roadWidth = 4;

        // 도로 진행 방향의 수직 방향
        const offsetX = (-dz / length) * (roadWidth / 2);
        const offsetZ = (dx / length) * (roadWidth / 2);

        const geometry = new THREE.BufferGeometry();

        const vertices = new Float32Array([
        x1 + offsetX, 0.5, z1 + offsetZ,
        x1 - offsetX, 0.5, z1 - offsetZ,
        x2 - offsetX, 0.5, z2 - offsetZ,

        x1 + offsetX, 0.5, z1 + offsetZ,
        x2 - offsetX, 0.5, z2 - offsetZ,
        x2 + offsetX, 0.5, z2 + offsetZ,
        ]);

        geometry.setAttribute(
        "position",
        new THREE.BufferAttribute(vertices, 3)
        );

        geometry.computeVertexNormals();

        geometries.push(geometry);
    }

    return geometries;
    });
}, [road]);

return (
    <>
    {geometries.map((geometry, index) => (
        <mesh key={index} geometry={geometry}>
        <meshStandardMaterial 
        color={0x666666}
        side={THREE.DoubleSide} />
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
