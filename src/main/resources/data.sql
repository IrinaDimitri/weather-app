CREATE TABLE IF NOT EXISTS weather (
    city TEXT PRIMARY KEY,
    temperature INT
);

DELETE FROM weather;
INSERT INTO weather (city, temperature) VALUES 
  ('London', 20), 
  ('Paris', 15), 
  ('Moscow', 17);
