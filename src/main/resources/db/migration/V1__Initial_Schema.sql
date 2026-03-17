-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY NOT NULL,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_system BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);

-- Bank Statements Table
CREATE TABLE IF NOT EXISTS bank_statements (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filename VARCHAR(255) NOT NULL,
    bank_name VARCHAR(100),
    account_type VARCHAR(50),
    account_last_four VARCHAR(4),
    processing_status VARCHAR(50) DEFAULT 'pending',
    transaction_count INT DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    error_message TEXT,
    file_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_statements_user ON bank_statements(user_id);
CREATE INDEX IF NOT EXISTS idx_statements_status ON bank_statements(processing_status);

-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    statement_id UUID REFERENCES bank_statements(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id),
    merchant_name VARCHAR(255),
    amount DECIMAL(19,2) NOT NULL,
    transaction_date DATE NOT NULL,
    raw_description TEXT,
    transaction_type VARCHAR(50),
    is_manual BOOLEAN DEFAULT false,
    is_recurring BOOLEAN DEFAULT false,
    recurring_frequency VARCHAR(50),
    confidence_score DECIMAL(3,2),
    llm_reasoning TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_transactions_statement ON transactions(statement_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);

-- Manual Entries Table
CREATE TABLE IF NOT EXISTS manual_entries (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id),
    amount DECIMAL(19,2) NOT NULL,
    transaction_date DATE NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_manual_entries_user ON manual_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_manual_entries_category ON manual_entries(category_id);

-- Analytics Cache Table
CREATE TABLE IF NOT EXISTS analytics_cache (
    id UUID PRIMARY KEY NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cache_key VARCHAR(255) NOT NULL,
    cache_value JSONB,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_analytics_cache_user_key ON analytics_cache(user_id, cache_key);

-- Processing Queue Table
CREATE TABLE IF NOT EXISTS processing_queue (
    id UUID PRIMARY KEY NOT NULL,
    statement_id UUID NOT NULL REFERENCES bank_statements(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'pending',
    retry_count INT DEFAULT 0,
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_processing_queue_status ON processing_queue(status);