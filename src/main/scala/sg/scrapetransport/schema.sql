CREATE TABLE IF NOT EXISTS BusStops (
  BusStopCode character(5) PRIMARY KEY,
  Description character varying(36),
  Latitude DECIMAL(20, 14),
  Longitude DECIMAL(20, 14),
  RoadName character varying(25)
);

CREATE TABLE IF NOT EXISTS BusServices (

);