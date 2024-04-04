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

// LatLong object for pins
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
  // starting view
  const [viewState, setViewState] = useState({
    latitude: ProvidenceLatLong.lat,
    longitude: ProvidenceLatLong.long,
    zoom: initialZoom,
  });

  // variable for account pins
  const [pins, setPins] = useState<LatLong[]>([])

  // load account pins
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

  // on click, add pins to pins variable and load to firebase
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

  const [query, setQuery] = useState<string>("")

  function handleSearch() {
    console.log(query)
    setQuery("")
  }

  useEffect(() => {
    function handleKeyPress(event: KeyboardEvent) {
        if (event.key === "Enter") {
          handleSearch();
          console.log("enter pressed")
        }
      }
      document.addEventListener("keypress", handleKeyPress);

      return () => {
        document.removeEventListener("keypress", handleKeyPress);
      }
    }, [query, handleSearch]);

  useEffect(() => {
    setOverlay(overlayData());
  }, []);

  return (
    <div className="map">
      <h2 className="map-title">Map For User: {USER_ID}</h2>
      <div className="search-container">
        <input
          type="text"
          placeholder="Enter an Area to Search for"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button onClick={handleSearch}>Search</button>
      </div>
      <Map
        mapboxAccessToken={MAPBOX_API_KEY}
        {...viewState}
        style={{ width: window.innerWidth, height: window.innerHeight }}
        mapStyle={"mapbox://styles/mapbox/streets-v12"}
        onMove={(ev: ViewStateChangeEvent) => setViewState(ev.viewState)}
        onClick={(ev: MapLayerMouseEvent) => onMapClick(ev)}
      >
        {pins.map((pin) => (
          <Marker // add a marker for every pin in the pins variable
            latitude={pin.lat}
            longitude={pin.long}
          >
          </Marker>
        ))}
        <div style={{ position: 'absolute', top: 15, right: 15, zIndex: 1 }}>
          <button onClick={async () => { // a button to clear pins, also clear data from firebase
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