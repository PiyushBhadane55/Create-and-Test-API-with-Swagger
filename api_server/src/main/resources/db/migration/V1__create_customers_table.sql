-- V1: Create customers table & insert seed data
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50)
);

INSERT INTO customers (name, email, phone) VALUES
('Alice Smith', 'alice@example.com', '+1-555-0101'),
('Bob Jones', 'bob@example.com', '+1-555-0102');
