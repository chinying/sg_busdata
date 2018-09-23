CREATE TABLE IF NOT EXISTS BusStops (
  BusStopCode character(5) PRIMARY KEY,
  Description character varying(36),
  Latitude DECIMAL(20, 14),
  Longitude DECIMAL(20, 14),
  RoadName character varying(25)
);

CREATE TABLE IF NOT EXISTS BusServicesStop (
  serviceNo character(5),
  operator character(5),
  direction integer,
  stopSequence integer,
  busStopCode character(5) references BusStops(BusStopCode),
  distance DECIMAL(5, 3),
  weekdayFirstBus character(4),
  weekdayLastBus character(4),
  satFirstBus character(4),
  satLastBus character(4),
  sunFirstBus character(4),
  sunLastBus character(4),
  PRIMARY KEY(serviceNo, busStopCode, direction)
);