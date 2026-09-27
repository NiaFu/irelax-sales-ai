CREATE TABLE customers (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    preferred_channel VARCHAR(30) NOT NULL,
    sales_stage VARCHAR(30) NOT NULL,
    first_visit_date DATE,
    first_visit_location VARCHAR(120),
    feedback TEXT,
    budget_min NUMERIC(12,2),
    budget_max NUMERIC(12,2),
    notes TEXT,
    last_contact_at TIMESTAMPTZ,
    next_follow_up_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_customers_owner_phone UNIQUE (owner_id, phone)
);

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    channel VARCHAR(30) NOT NULL,
    external_conversation_id VARCHAR(255),
    last_activity_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_conversation_customer_channel UNIQUE (owner_id, customer_id, channel)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    direction VARCHAR(20) NOT NULL,
    sender VARCHAR(40) NOT NULL,
    receiver VARCHAR(40) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    external_message_id VARCHAR(100),
    error_code VARCHAR(40),
    sent_at TIMESTAMPTZ,
    received_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_messages_conversation_created ON messages(conversation_id, created_at);
CREATE INDEX idx_messages_external_id ON messages(external_message_id);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    name VARCHAR(180) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(120),
    price NUMERIC(12,2),
    warranty_years INTEGER,
    description TEXT,
    features TEXT,
    strengths TEXT,
    product_url VARCHAR(600),
    manual_url VARCHAR(600),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_products_owner_name UNIQUE (owner_id, name)
);

CREATE TABLE customer_product_interests (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    interest_level VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    confidence NUMERIC(4,3),
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_customer_product_source UNIQUE (owner_id, customer_id, product_id, source)
);

CREATE TABLE follow_up_tasks (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL,
    due_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason TEXT,
    priority VARCHAR(20) NOT NULL,
    ai_generated BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_followups_owner_due ON follow_up_tasks(owner_id, status, due_at);

CREATE TABLE customer_notes (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ai_suggestions (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    message_id UUID NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    draft_reply TEXT NOT NULL,
    intent VARCHAR(80),
    sentiment VARCHAR(40),
    detected_products TEXT,
    follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
    suggested_follow_up_at TIMESTAMPTZ,
    stage_suggestion VARCHAR(30),
    summary TEXT,
    model VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_ai_suggestions_message ON ai_suggestions(message_id, created_at);

CREATE TABLE sales_assistant_profiles (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    tone TEXT,
    rules TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_profile_owner UNIQUE (owner_id)
);

CREATE TABLE knowledge_entries (
    id UUID PRIMARY KEY,
    owner_id VARCHAR(128) NOT NULL,
    category VARCHAR(80) NOT NULL,
    title VARCHAR(180) NOT NULL,
    content TEXT NOT NULL,
    source_url VARCHAR(600),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
