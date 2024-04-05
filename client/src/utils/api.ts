import { LatLong } from "../components/Mapbox";
import { getLoginCookie } from "./cookie";

const HOST = "http://localhost:3232";

async function queryAPI(
  endpoint: string,
  query_params: Record<string, string>
) {
  // query_params is a dictionary of key-value pairs that gets added to the URL as query parameters
  // e.g. { foo: "bar", hell: "o" } becomes "?foo=bar&hell=o"
  const paramsString = new URLSearchParams(query_params).toString();
  const url = `${HOST}/${endpoint}?${paramsString}`;
  const response = await fetch(url);
  if (!response.ok) {
    console.error(response.status, response.statusText);
  }
  return response.json();
}

export async function addWord(word: string) {
  return await queryAPI("add-word", {
    uid: getLoginCookie() || "",
    word: word,
  });
}

// add a pin to firebase data
export async function addPin(loc: LatLong) {
  return await queryAPI("add-pin", {
    uid: getLoginCookie() || "",
    lat: loc.lat.toString(),
    long: loc.long.toString()
  });
}

export async function getWords() {
  return await queryAPI("list-words", {
    uid: getLoginCookie() || "",
  });
}

// request all user pins from firebase
export async function getPins() {
  return await queryAPI("get-pins", {
    uid: getLoginCookie() || "",
  });
}

// request all all Geo Data from api
export async function getGeoData(input: string) {
  return await queryAPI("data-request", {
    input: input || "",
  });
}

export async function clearUser(uid: string = getLoginCookie() || "") {
  return await queryAPI("clear-user", {
    uid: uid,
  });
}

