import "mapbox-gl/dist/mapbox-gl.css";
import { useCallback, useEffect, useState } from "react";
import { getLoginCookie } from "../utils/cookie";
import Map, {
  Layer,
  MapLayerMouseEvent,
  Marker,
  PointLike,
  Source,
  ViewStateChangeEvent,
} from "react-map-gl";
import { geoLayer, overlayData } from "../utils/overlay";
import { addPin, getPins, clearUser } from "../utils/api";

const MAPBOX_API_KEY = process.env.MAPBOX_TOKEN;
if (!MAPBOX_API_KEY) {
  console.error("Mapbox API key not found. Please add it to your .env file.");
}

export interface LatLong {
  lat: number;
  long: number;
}

const USER_ID = getLoginCookie() || "";

const ProvidenceLatLong: LatLong = {
  lat: 41.824,
  long: -71.4128,
};
const initialZoom = 10;


export default function Mapbox() {
  const [viewState, setViewState] = useState({
    latitude: ProvidenceLatLong.lat,
    longitude: ProvidenceLatLong.long,
    zoom: initialZoom,
  });

  const [pins, setPins] = useState<LatLong[]>([])

  useEffect(() => {
    getPins().then((data) => {
      setPins(data.pins.map((str: string) => {
        let parts = str.split(":");
        let pinData: LatLong = {
          lat: parseFloat(parts[0]),
          long: parseFloat(parts[1])
      
        }; 
        return pinData}))
    });
  }, []);

  function onMapClick(e: MapLayerMouseEvent) {

    console.log(e.lngLat.lat);
    console.log(e.lngLat.lng);
    const latLng: LatLong = {
      lat: e.lngLat.lat,
      long: e.lngLat.lng
    }
    setPins([...pins, latLng])
    addPin(latLng)
  }

  const [overlay, setOverlay] = useState<GeoJSON.FeatureCollection | undefined>(
    undefined
  );

  useEffect(() => {
    setOverlay(overlayData());
  }, []);

  return (
    <div className="map">
      <h2 className="map-title">Map For User: {USER_ID}</h2>
      <Map
        mapboxAccessToken={MAPBOX_API_KEY}
        {...viewState}
        style={{ width: window.innerWidth, height: window.innerHeight }}
        mapStyle={"mapbox://styles/mapbox/streets-v12"}
        onMove={(ev: ViewStateChangeEvent) => setViewState(ev.viewState)}
        onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
      >
        {pins.map((pin) => (
          <Marker
            latitude={pin.lat}
            longitude={pin.long}
          >
          </Marker>
        ))}
        <div style={{ position: 'absolute', top: 15, right: 15, zIndex: 1 }}>
          <button onClick={async () => {
          setPins([]);
          await clearUser();
        }}>Clear Pins</button>
        </div>
        <Source id="geo_data" type="geojson" data={overlay}>
          <Layer {...geoLayer} />
        </Source>
      </Map>
    </div>
  );
}