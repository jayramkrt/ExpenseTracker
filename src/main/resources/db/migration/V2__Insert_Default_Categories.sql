-- Insert default system categories
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Groceries', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Groceries');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Utilities', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Utilities');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Entertainment', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Entertainment');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Transportation', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Transportation');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Healthcare', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Healthcare');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Salary/Income', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Salary/Income');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Subscriptions', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Subscriptions');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Shopping', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Shopping');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Dining & Restaurants', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Dining & Restaurants');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Insurance', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Insurance');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Education', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Education');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Transfer', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Transfer');
INSERT INTO categories (id, name, is_system, is_active) 
SELECT gen_random_uuid(), 'Other', true, true WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Other');