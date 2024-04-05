import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

import rl_data from "../geodata/fullDownload.json";

const loadData = async () => {
  const loadResp: Promise<String> = await fetch("http://localhost:3232/data-request")
    .then((response) => response.json())
    .catch(console.error);

  if (loadResp != undefined) {
    console.log(loadResp)
    return loadResp;
    }
};


const propertyName = "grade";
export const geoLayer: FillLayer = {
  id: "geo_data",
  type: "fill",
  paint: {
    "fill-color": [
      "match",
      ["get", propertyName],
      "A",
      "#5bcc04",
      "B",
      "#04b8cc",
      "C",
      "#e9ed0e",
      "D",
      "#d11d1d",
      "#ccc",
    ],
    "fill-opacity": 0.2,
  },
};

function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}

export function overlayData(): GeoJSON.FeatureCollection | undefined {
  const rl_data = loadData()
  return isFeatureCollection(rl_data) ? rl_data : undefined;
}