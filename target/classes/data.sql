CREATE TABLE IF NOT EXISTS weather (
    city TEXT PRIMARY KEY,
    temperature INT
);

INSERT INTO weather (city, temperature) VALUES ('London', 20), ('Paris', 15);