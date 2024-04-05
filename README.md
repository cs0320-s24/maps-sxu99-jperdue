> **GETTING STARTED:** The Maps gearup code is a great starting point for both the backend and frontend. You might also want to grab code snippets from your final REPL project.

# Project Details

This maps website is designed using Java, React, and uses MapBox to display a map and redlining data. The backend, built with java, provides API endpoints which allow users to request data sources and filter them by their coordinates or by keywords by using the data-request endpoint. It also provides API requests for adding and removing pins from users maps, as displayed in the front end, while storing these pins in a firestore database. The frontend, built with React and Mapbox, allows users to securely log in through firebase and access their map. The map will display historical redlining data upon load, as well as any saved pins by the user. The search bar allows them to search the map data for specific keywords, and the clear pins button allows them to clear any pins they have on the map.

Testing was done using JUnit and Playwright.

# Design Choices

A prominent design choice was loading and handling GeoJson data in one handler. We believed this was best as it allowed us to easily process the data given the multiple different types of requests the data-request endpoint would receive. We also made the data-request end point without any query-params return the entire data, allowing for easier integration with the front end.

Addittionally, having the data loaded in as a GeoJsonData class rather than string format allowed us to parse the data much easier than had we been working with a stringified Json. For pins, we used a bundled latLong interface on the front end that was then split into two separate lat and long objects for requesting the backend API, as we wanted to store lat and long as separate data points in the fire store data base. This way, they were easily accessible upon request, and there was no need to parse any strings, but only request the data from different sections of a Json.

For the map itself, we created use effects to allow users to click on the map to add pins, and remove them with the clear pins button. Also, the search bar can have queries entered by pressing the enter button, make the website more keyboard friendly.

For authentication and data storage, we used firebase. Each user has their own ID and login cookie which then can be used through the backend API to store or change any new pins/modifications to their user data.

# Errors/Bugs

# Tests

The App.spec.ts file contains end-to-end tests for an application using Playwright. Before each test, a spoofed "uid" cookie is added to the browser context and any existing data for this user is cleared from the database. The tests cover various user interactions and assertions about the page. The first test checks that the gearup screen is visible on page load and that the user ID is displayed correctly. The second test verifies that a user can add a word to their favorites list and that the word remains in the list even after a page reload. The third test asserts that the map is visible on the page. The fourth and fifth tests check that the search input and button are visible and functional, respectively. The sixth test ensures that the Clear Pins button is visible, and the final test verifies that the Clear Pins button works correctly. Each test navigates to the application's URL, interacts with the page, and then makes assertions about the page against expected outcomes.

# How to

To use the backend API, one can reuqest the data-request endpoint but using /data-request, with no endpoint for all the data, ?keyword= for a filter by keyword, and ?minLat=XXXmaxLat=XXXminLong=XXXmaxLong=XXX as different options to obtain their desired data.

To use the frontend application, a user can log in with their google account, and move to the maps page where the redlining data will originally be displayed. From there, they may click and drag to move to different parts of the map, zoom in or out, and click on locations to add a pin to that spot on the map. They can clear their pins by clicking the clear pins button. They can also search by using the search bar and searching the data by a keyword, and can enter their query by pressing enter or the button next to the search bar. This change the map to display only regions of the data with their desired keyword.

# Collaboration

_(state all of your sources of collaboration past your project partner. Please refer to the course's collaboration policy for any further questions.)_
