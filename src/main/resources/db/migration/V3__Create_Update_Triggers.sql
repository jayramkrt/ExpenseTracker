-- Create function to update 'updated_at' timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger for users table
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for categories table
CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for bank_statements table
CREATE TRIGGER update_bank_statements_updated_at BEFORE UPDATE ON bank_statements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for transactions table
CREATE TRIGGER update_transactions_updated_at BEFORE UPDATE ON transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for manual_entries table
CREATE TRIGGER update_manual_entries_updated_at BEFORE UPDATE ON manual_entries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for analytics_cache table
CREATE TRIGGER update_analytics_cache_updated_at BEFORE UPDATE ON analytics_cache
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create trigger for processing_queue table
CREATE TRIGGER update_processing_queue_updated_at BEFORE UPDATE ON processing_queue
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();