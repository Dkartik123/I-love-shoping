-- I Love Shopping - Initial Database Schema
-- PostgreSQL Migration Script V1

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- USER MANAGEMENT TABLES
-- =====================================================

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    email_verified BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    failed_login_attempts INTEGER DEFAULT 0,
    lock_time TIMESTAMP,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    provider VARCHAR(50) DEFAULT 'LOCAL',
    provider_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User roles junction table
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    replaced_by VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by_ip VARCHAR(50)
);

-- Password reset tokens
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Email verification tokens
CREATE TABLE email_verification_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User addresses
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_type VARCHAR(20) NOT NULL DEFAULT 'SHIPPING',
    is_default BOOLEAN DEFAULT FALSE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    company VARCHAR(200),
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- PRODUCT CATALOG TABLES
-- =====================================================

-- Categories table (hierarchical)
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Brands table
CREATE TABLE brands (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    logo_url VARCHAR(500),
    website VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(280) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    low_stock_threshold INTEGER DEFAULT 10,
    weight_kg DECIMAL(8,3),
    weight_lb DECIMAL(8,3),
    length_cm DECIMAL(8,2),
    width_cm DECIMAL(8,2),
    height_cm DECIMAL(8,2),
    length_in DECIMAL(8,2),
    width_in DECIMAL(8,2),
    height_in DECIMAL(8,2),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    brand_id UUID REFERENCES brands(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    is_digital BOOLEAN DEFAULT FALSE,
    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(255),
    average_rating DECIMAL(3,2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    sold_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product images table
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product attributes (for variants and filtering)
CREATE TABLE product_attributes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    attribute_type VARCHAR(50) DEFAULT 'TEXT',
    is_filterable BOOLEAN DEFAULT TRUE,
    is_required BOOLEAN DEFAULT FALSE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product attribute values
CREATE TABLE product_attribute_values (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    attribute_id UUID NOT NULL REFERENCES product_attributes(id) ON DELETE CASCADE,
    value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, attribute_id)
);

-- Product tags
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Product tags junction
CREATE TABLE product_tags (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, tag_id)
);

-- Product reviews
CREATE TABLE product_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    is_approved BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, user_id)
);

-- =====================================================
-- SEARCH AND SUGGESTIONS
-- =====================================================

-- Search history for suggestions
CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    search_term VARCHAR(255) NOT NULL,
    results_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Popular search terms
CREATE TABLE popular_searches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    search_term VARCHAR(255) NOT NULL UNIQUE,
    search_count INTEGER DEFAULT 1,
    last_searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES
-- =====================================================

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Product indexes
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_featured ON products(is_featured);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_rating ON products(average_rating);
CREATE INDEX idx_products_created ON products(created_at);

-- Full text search index
CREATE INDEX idx_products_search ON products USING gin(to_tsvector('english', name || ' ' || COALESCE(description, '')));

-- Category indexes
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);

-- Search indexes
CREATE INDEX idx_search_history_user ON search_history(user_id);
CREATE INDEX idx_search_history_term ON search_history(search_term);
CREATE INDEX idx_popular_searches_count ON popular_searches(search_count DESC);

-- =====================================================
-- INITIAL DATA
-- =====================================================

-- Insert default roles
INSERT INTO roles (name, description) VALUES
    ('ROLE_USER', 'Standard user role'),
    ('ROLE_ADMIN', 'Administrator role with full access'),
    ('ROLE_MODERATOR', 'Moderator role for content management');

-- Insert sample categories
INSERT INTO categories (name, slug, description, display_order) VALUES
    ('Electronics', 'electronics', 'Electronic devices and gadgets', 1),
    ('Computers & Laptops', 'computers-laptops', 'Desktop computers and laptops', 2),
    ('Smartphones', 'smartphones', 'Mobile phones and accessories', 3),
    ('Audio & Video', 'audio-video', 'Audio and video equipment', 4),
    ('Home & Furniture', 'home-furniture', 'Home decor and furniture', 5),
    ('Books & Media', 'books-media', 'Books, movies, and music', 6),
    ('Pet Supplies', 'pet-supplies', 'Products for pets', 7);

-- Insert sample brands
INSERT INTO brands (name, slug, description) VALUES
    ('Apple', 'apple', 'Think Different'),
    ('Samsung', 'samsung', 'Do What You Cant'),
    ('Sony', 'sony', 'Be Moved'),
    ('LG', 'lg', 'Lifes Good'),
    ('Dell', 'dell', 'Dell Technologies'),
    ('HP', 'hp', 'Keep Reinventing');

-- Insert sample product attributes
INSERT INTO product_attributes (name, display_name, attribute_type, is_filterable) VALUES
    ('color', 'Color', 'TEXT', TRUE),
    ('size', 'Size', 'TEXT', TRUE),
    ('material', 'Material', 'TEXT', TRUE),
    ('warranty', 'Warranty', 'TEXT', FALSE),
    ('connectivity', 'Connectivity', 'TEXT', TRUE);
