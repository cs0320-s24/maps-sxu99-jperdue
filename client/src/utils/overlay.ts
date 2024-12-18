import { FeatureCollection } from "geojson";
import { FillLayer } from "react-map-gl";

// import rl_data from "../geodata/fullDownload.json";
import { getGeoData } from "./api";

const propertyName = "holc_grade";
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

let response : {result: string, data: FeatureCollection}
let rl_data : any

function isFeatureCollection(json: any): json is FeatureCollection {
  return json.type === "FeatureCollection";
}

export async function overlayData(input: string): Promise<GeoJSON.FeatureCollection | undefined> {
  response = await getGeoData(input)
  rl_data = response["data"]
  return isFeatureCollection(rl_data) ? rl_data : undefined;
}