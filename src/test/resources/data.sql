-- Seed predefined tags for tests (explicit IDs to satisfy NOT NULL ID)
INSERT INTO tags (id, name) VALUES (1001, 'Italian');
INSERT INTO tags (id, name) VALUES (1002, 'Quick');
INSERT INTO tags (id, name) VALUES (1003, 'Easy');
INSERT INTO tags (id, name) VALUES (1004, 'Healthy');
INSERT INTO tags (id, name) VALUES (1005, 'Baked');
INSERT INTO tags (id, name) VALUES (1006, 'Dessert');
INSERT INTO tags (id, name) VALUES (1007, 'Mexican');
INSERT INTO tags (id, name) VALUES (1008, 'Asian');
INSERT INTO tags (id, name) VALUES (1009, 'French');
INSERT INTO tags (id, name) VALUES (1010, 'Indian');
INSERT INTO tags (id, name) VALUES (1011, 'Mediterranean');
INSERT INTO tags (id, name) VALUES (1012, 'American');
INSERT INTO tags (id, name) VALUES (1013, 'Breakfast');
INSERT INTO tags (id, name) VALUES (1014, 'Lunch');
INSERT INTO tags (id, name) VALUES (1015, 'Dinner');
INSERT INTO tags (id, name) VALUES (1016, 'Vegetarian');
INSERT INTO tags (id, name) VALUES (1017, 'Vegan');
INSERT INTO tags (id, name) VALUES (1018, 'Gluten-Free');

-- Ensure Hibernate sequence does not collide with our explicit IDs
-- H2 may not have a default sequence; ignore if missing

