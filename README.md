# 🛒 I Love Shopping

A comprehensive B2C e-commerce platform built with Java Spring Boot and React TypeScript.

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Entity Relationship Diagram](#-entity-relationship-diagram)
- [Quick Start](#-quick-start)
- [Development Setup](#-development-setup)
- [API Documentation](#-api-documentation)
- [Configuration](#-configuration)
- [Testing](#-testing)
- [Deployment](#-deployment)

## ✨ Features

### Authentication & Authorization
- 🔐 **Email/Password Authentication** with JWT (access + refresh tokens)
- 🌐 **OAuth2 Social Login** (Google, Facebook)
- 🤖 **Google reCAPTCHA** integration for bot protection
- 🔑 **Two-Factor Authentication (2FA)** with TOTP (Google Authenticator/Authy)
- 📧 **Email Verification** and **Password Reset** flows
- 🔄 **Token Rotation** for enhanced security

### Product Catalog
- 📦 **Comprehensive Product Model** with categories, brands, attributes
- 🔍 **Faceted Search** with multiple filter criteria
- 💡 **Dynamic Search Suggestions** with caching
- 📊 **Sorting Options** (price, rating, newest, best-selling)
- 🏷️ **Product Tags** and **Attributes**
- ⭐ **Product Reviews & Ratings**

### Database
- 🗄️ **PostgreSQL** with ACID compliance
- 📈 **Optimized Indexes** for search performance
- 🔄 **Flyway Migrations** for schema versioning

### DevOps
- 🐳 **Docker & Docker Compose** for containerization
- 🚀 **One-command Deployment** with `start.sh` / `start.bat`
- ❤️ **Health Checks** for all services

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming Language |
| Spring Boot | 3.2.0 | Application Framework |
| Spring Security | 6.x | Authentication & Authorization |
| Spring Data JPA | 3.2.0 | Database ORM |
| PostgreSQL | 16 | Relational Database |
| Redis | 7 | Caching |
| Flyway | 9.x | Database Migrations |
| jjwt | 0.12.3 | JWT Token Processing |
| SpringDoc OpenAPI | 2.3.0 | API Documentation |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.2 | UI Library |
| TypeScript | 5.3 | Type Safety |
| Vite | 5.0 | Build Tool |
| TailwindCSS | 3.4 | Styling |
| React Router | 6.21 | Client-side Routing |
| TanStack Query | 5.17 | Server State Management |
| Zustand | 4.4 | Client State Management |
| React Hook Form | 7.49 | Form Management |
| Zod | 3.22 | Schema Validation |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| Docker | Containerization |
| Docker Compose | Multi-container Orchestration |
| Nginx | Reverse Proxy & Static Serving |

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Browser                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Nginx (Port 80/443)                          │
│              Static Files │ Reverse Proxy                       │
└─────────────────────────────────────────────────────────────────┘
                                │
         ┌──────────────────────┴──────────────────────┐
         ▼                                              ▼
┌─────────────────┐                        ┌─────────────────────┐
│   React SPA     │                        │  Spring Boot API    │
│   (Frontend)    │                        │    (Port 8080)      │
│                 │                        │                     │
│ • Authentication│                        │ • REST Endpoints    │
│ • Product List  │                        │ • JWT Auth          │
│ • Search        │                        │ • OAuth2            │
│ • User Profile  │                        │ • Business Logic    │
└─────────────────┘                        └─────────────────────┘
                                                    │
                           ┌────────────────────────┴─────────┐
                           ▼                                   ▼
                  ┌─────────────────┐              ┌─────────────────┐
                  │   PostgreSQL    │              │     Redis       │
                  │   (Port 5432)   │              │   (Port 6379)   │
                  │                 │              │                 │
                  │ • Users         │              │ • Session Cache │
                  │ • Products      │              │ • Search Cache  │
                  │ • Categories    │              │ • Rate Limiting │
                  │ • Orders        │              │                 │
                  └─────────────────┘              └─────────────────┘
```

## 📊 Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              ENTITY RELATIONSHIP DIAGRAM                             │
└─────────────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────┐
                                    │    ROLES    │
                                    ├─────────────┤
                                    │ id (PK)     │
                                    │ name        │
                                    └──────┬──────┘
                                           │
                                           │ M:N
                                           ▼
┌──────────────────┐              ┌─────────────────┐              ┌──────────────────┐
│    ADDRESSES     │              │      USERS      │              │  REFRESH_TOKENS  │
├──────────────────┤              ├─────────────────┤              ├──────────────────┤
│ id (PK)          │──────────────│ id (PK)         │──────────────│ id (PK)          │
│ user_id (FK)     │      1:N     │ email           │      1:N     │ token            │
│ type             │              │ password        │              │ user_id (FK)     │
│ first_name       │              │ first_name      │              │ expires_at       │
│ last_name        │              │ last_name       │              │ revoked          │
│ street           │              │ phone           │              │ replaced_by_token│
│ city             │              │ avatar_url      │              │ device_info      │
│ state            │              │ auth_provider   │              └──────────────────┘
│ postal_code      │              │ provider_id     │
│ country          │              │ email_verified  │
│ is_default       │              │ 2fa_enabled     │
└──────────────────┘              │ 2fa_secret      │
                                  │ created_at      │
                                  │ updated_at      │
                                  └────────┬────────┘
                                           │
                                           │ 1:N
                                           ▼
                                  ┌─────────────────┐
                                  │ PRODUCT_REVIEWS │
                                  ├─────────────────┤
                                  │ id (PK)         │
                                  │ product_id (FK) │◄──────────────────────────────┐
                                  │ user_id (FK)    │                               │
                                  │ rating          │                               │
                                  │ title           │                               │
                                  │ content         │                               │
                                  │ verified_purchase                               │
                                  │ helpful_count   │                               │
                                  └─────────────────┘                               │
                                                                                    │
     ┌─────────────┐              ┌─────────────────┐              ┌───────────────┴─┐
     │   BRANDS    │              │    PRODUCTS     │              │ PRODUCT_IMAGES  │
     ├─────────────┤              ├─────────────────┤              ├─────────────────┤
     │ id (PK)     │──────────────│ id (PK)         │──────────────│ id (PK)         │
     │ name        │      1:N     │ sku             │      1:N     │ product_id (FK) │
     │ slug        │              │ name            │              │ image_url       │
     │ description │              │ slug            │              │ alt_text        │
     │ logo_url    │              │ description     │              │ is_primary      │
     │ website_url │              │ short_description              │ display_order   │
     │ is_active   │              │ price           │              └─────────────────┘
     └─────────────┘              │ compare_at_price│
                                  │ cost_price      │
     ┌─────────────┐              │ category_id (FK)│◄────────────┐
     │ CATEGORIES  │              │ brand_id (FK)   │             │
     ├─────────────┤              │ stock_quantity  │             │
     │ id (PK)     │──────────────│ low_stock_thresh│             │
     │ name        │      1:N     │ weight_kg/lb    │             │
     │ slug        │              │ dimensions      │             │
     │ description │              │ is_active       │             │
     │ image_url   │              │ is_featured     │             │
     │ parent_id   │◄─────┐       │ is_digital      │             │
     │ display_order      │       │ avg_rating      │             │
     │ is_active   │      │       │ review_count    │             │
     └──────┬──────┘      │       │ view_count      │             │
            │             │       │ sold_count      │             │
            └─────────────┘       │ meta_title      │             │
            (self-reference)      │ meta_description│             │
                                  │ created_at      │             │
                                  │ updated_at      │             │
                                  └────────┬────────┘             │
                                           │                      │
                           ┌───────────────┼───────────────┐      │
                           │               │               │      │
                           ▼               ▼               ▼      │
              ┌─────────────────┐ ┌─────────────┐ ┌──────────────┐│
              │ PRODUCT_ATTRIB  │ │    TAGS     │ │PRODUCT_ATTRIB││
              │    _VALUES      │ │             │ │    UTES      ││
              ├─────────────────┤ ├─────────────┤ ├──────────────┤│
              │ id (PK)         │ │ id (PK)     │ │ id (PK)      ││
              │ product_id (FK) │ │ name        │ │ name         ││
              │ attribute_id(FK)│ │ slug        │ │ display_name ││
              │ value           │ └──────┬──────┘ │ category_id  │┘
              └─────────────────┘        │        │ is_filterable│
                                         │ M:N    │ is_searchable│
                                         ▼        │ display_order│
                                  ┌─────────────┐ └──────────────┘
                                  │PRODUCT_TAGS │
                                  ├─────────────┤
                                  │product_id   │
                                  │tag_id       │
                                  └─────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│ LEGEND:  (PK) = Primary Key   (FK) = Foreign Key                                    │
│          1:N = One to Many    M:N = Many to Many    ──▶ = Relationship              │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### Key Entity Descriptions

| Entity | Description |
|--------|-------------|
| **Users** | Customer accounts with authentication data, OAuth providers, and 2FA settings |
| **Roles** | User roles (ROLE_USER, ROLE_ADMIN) for authorization |
| **Refresh_Tokens** | JWT refresh tokens with rotation tracking |
| **Addresses** | User shipping/billing addresses |
| **Products** | Core product catalog with pricing, inventory, and SEO metadata |
| **Categories** | Hierarchical product categories (self-referencing) |
| **Brands** | Product manufacturers/brands |
| **Product_Images** | Multiple images per product with primary flag |
| **Product_Attributes** | Dynamic attributes for faceted search (color, size, etc.) |
| **Tags** | Product tags for flexible categorization |
| **Product_Reviews** | Customer reviews with ratings |

## 🚀 Quick Start

### Prerequisites
- Docker Desktop installed and running
- Git

### One-Command Startup

**Linux/Mac:**
```bash
git clone https://github.com/yourusername/i-love-shopping.git
cd i-love-shopping
chmod +x start.sh
./start.sh
```

**Windows:**
```powershell
git clone https://github.com/yourusername/i-love-shopping.git
cd i-love-shopping
.\start.bat
```

The application will be available at:
- **Frontend:** http://localhost
- **Backend API:** http://localhost:8080
- **API Docs:** http://localhost:8080/swagger-ui.html

## 💻 Development Setup

### Backend Development

```bash
cd backend

# Install dependencies and run
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Build JAR
./mvnw clean package
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev

# Run tests
npm test

# Build for production
npm run build
```

### Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
# Database
POSTGRES_PASSWORD=your-secure-password

# JWT
JWT_SECRET=your-32-character-minimum-secret

# OAuth2 (optional)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-secret
FACEBOOK_CLIENT_ID=your-facebook-app-id
FACEBOOK_CLIENT_SECRET=your-facebook-secret

# reCAPTCHA
RECAPTCHA_SECRET=your-recaptcha-secret

# Email
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## 📚 API Documentation

Once the backend is running, access the Swagger UI at:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

### Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Register new user |
| POST | `/auth/login` | Login with email/password |
| POST | `/auth/refresh` | Refresh access token |
| POST | `/auth/logout` | Logout and revoke tokens |
| POST | `/auth/2fa/enable` | Enable 2FA |
| GET | `/products` | List products (paginated) |
| GET | `/products/search` | Search with filters |
| GET | `/products/suggestions` | Get search suggestions |
| GET | `/categories/tree` | Get category hierarchy |

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test                    # Run all tests
./mvnw test -Dtest=*Test       # Run unit tests
./mvnw verify                  # Run integration tests
```

### Frontend Tests
```bash
cd frontend
npm test                       # Run all tests
npm run test:coverage          # With coverage
```

## 🔒 Security Features

1. **JWT Token Security**
   - Short-lived access tokens (15 minutes)
   - Long-lived refresh tokens (7 days) with rotation
   - Token revocation on logout

2. **Password Security**
   - BCrypt hashing
   - Strong password requirements
   - Rate-limited login attempts

3. **Input Validation**
   - Server-side validation with Bean Validation
   - Client-side validation with Zod
   - SQL injection prevention (JPA/Hibernate)
   - XSS prevention (React's default escaping)

4. **Two-Factor Authentication**
   - TOTP-based (RFC 6238)
   - Compatible with Google Authenticator, Authy

## 📦 Deployment

### Docker Compose (Production)
```bash
docker-compose -f docker-compose.yml up -d
```

### Manual Deployment
1. Build backend JAR: `cd backend && ./mvnw clean package`
2. Build frontend: `cd frontend && npm run build`
3. Deploy to your infrastructure

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

Made with ❤️ for the love of shopping!
