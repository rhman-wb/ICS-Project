# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an **Insurance Product Intelligent Audit System** (保险产品智能检核系统) - a comprehensive insurance audit platform built with a modern microservices architecture. The system provides intelligent audit capabilities for insurance products with role-based access control, real-time monitoring, and comprehensive audit trail functionality.

## Development Commands

### Backend (Spring Boot)

**Build & Run:**
```bash
cd backend
mvn clean compile                    # Compile the project
mvn spring-boot:run                   # Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # Explicit dev profile
```

**Testing:**
```bash
mvn test                              # Run unit tests
mvn verify                            # Run all tests including integration tests
mvn clean verify                      # Clean build and run all tests
```

**Code Quality:**
```bash
mvn clean compile                      # Check compilation
mvn sonar:sonar                       # SonarQube analysis (if configured)
```

**Coverage & Reporting:**
```bash
mvn clean verify                      # Runs JaCoCo coverage automatically
# Coverage reports generated in: target/site/jacoco-merged/
```

### Frontend (Vue 3 + TypeScript)

**Development:**
```bash
cd frontend
npm install                          # Install dependencies
npm run dev                          # Start development server (localhost:3000)
```

**Build & Production:**
```bash
npm run build                        # Build for production
npm run preview                      # Preview production build
```

**Testing:**
```bash
npm test                             # Run Vitest tests
npm run test:run                     # Run tests once
npm run test:coverage                # Run tests with coverage
npm run test:e2e                     # Run Playwright E2E tests
npm run test:e2e:ui                 # Run E2E tests with UI
```

**Code Quality:**
```bash
npm run type-check                   # TypeScript type checking
npm run lint                         # ESLint linting with auto-fix
npm run format                       # Prettier formatting
```

## Architecture Overview

### Backend Architecture

The backend follows **Hexagonal Architecture** (Ports and Adapters) with clean separation of concerns:

```
backend/src/main/java/com/insurance/audit/
├── common/                          # Shared utilities and configurations
│   ├── config/                      # Spring configurations (Security, CORS, etc.)
│   ├── controller/                  # Common controllers (Health, etc.)
│   ├── dto/                        # Data Transfer Objects
│   ├── entity/                     # Base entities
│   ├── service/                    # Common services
│   └── exception/                  # Exception handling
├── user/                           # User management module (example)
│   ├── application/service/         # Application services (business logic)
│   └── interfaces/dto/             # Interface DTOs
└── UserAuthSystemApplication.java   # Main application entry point
```

**Key Architecture Patterns:**
- **Domain-Driven Design (DDD)**: Clear separation between domain logic and infrastructure
- **Clean Architecture**: Business logic independent of frameworks
- **Multi-profile Configuration**: Different configurations for dev, test, prod
- **Comprehensive Testing**: Unit tests, integration tests, and TestContainers support

### Frontend Architecture

The frontend follows **Vue 3 Composition API** patterns with TypeScript:

```
frontend/src/
├── api/                           # API service layer
├── assets/                        # Static assets
├── components/                    # Reusable Vue components
├── composables/                   # Vue 3 composables (logic reuse)
├── config/                        # Application configuration
├── router/                        # Vue Router configuration
├── stores/                        # Pinia state management
├── types/                         # TypeScript type definitions
├── utils/                         # Utility functions
├── views/                         # Page-level components
└── App.vue, main.ts              # Application entry points
```

**Key Technologies:**
- **Vue 3** with Composition API and TypeScript
- **Ant Design Vue** for UI components
- **Pinia** for state management
- **Axios** for HTTP requests
- **Vue Router** for routing
- **Vite** for build tooling
- **Playwright** for E2E testing
- **Vitest** for unit testing

### Database & Infrastructure

**Database:**
- **MySQL 8.0** with MyBatis Plus ORM
- **Redis 7.0** for caching and session management
- **TestContainers** for integration testing
- **Database migrations** handled through initialization scripts

**Security:**
- **Spring Security** with JWT authentication
- **Role-based access control (RBAC)**
- **CORS configuration** for frontend integration
- **Multiple security profiles** for different environments

## Development Environment Setup

### Prerequisites

**Backend Requirements:**
- Java 17+
- Maven 3.6+
- MySQL 8.0 (or Docker for containerized development)
- Redis 7.0 (optional, can be disabled in dev)

**Frontend Requirements:**
- Node.js 20+ (as specified in package.json engines)
- npm or yarn

### Environment Configuration

**Database Setup:**
```bash
# Default development credentials
Database: insurance_audit
Username: insurance_user  
Password: 20150826
```

**Environment Variables:**
- Backend configuration in `backend/src/main/resources/application-dev.yml`
- Frontend configuration in `frontend/.env.development`
- JWT secret must be at least 64 characters long

### Service Startup Order

1. **Start MySQL** (if not using Docker):
   ```bash
   sudo systemctl start mysql  # Linux
   brew services start mysql   # macOS
   ```

2. **Start Redis** (optional):
   ```bash
   sudo systemctl start redis  # Linux
   brew services start redis   # macOS
   ```

3. **Start Backend**:
   ```bash
   cd backend && mvn spring-boot:run
   ```

4. **Start Frontend**:
   ```bash
   cd frontend && npm run dev
   ```

## Testing Strategy

### Backend Testing

**Test Structure:**
```
backend/src/test/java/com/insurance/audit/
├── user/application/service/     # Service layer tests
└── user/interfaces/dto/         # DTO tests
```

**Testing Framework:**
- **JUnit 5** for unit testing
- **Mockito** for mocking
- **AssertJ** for assertions
- **TestContainers** for integration tests with real MySQL/Redis
- **JaCoCo** for code coverage (85% line coverage required)

### Frontend Testing

**Test Structure:**
```
frontend/src/test/
├── components/                   # Component tests
├── composables/                  # Composable tests
├── utils/                        # Utility tests
└── e2e/                          # End-to-end tests (Playwright)
```

**Testing Framework:**
- **Vitest** for unit and component testing
- **Vue Test Utils** for component testing
- **Playwright** for E2E testing
- **jsdom** for DOM testing

## Configuration Profiles

### Backend Profiles

- **dev**: Development environment with simplified security
- **test**: Test environment with H2 database
- **prod**: Production environment with full security
- **minimal**: Minimal configuration for debugging
- **debug**: Debug configuration with enhanced logging
- **bare**: Bare configuration without security

### Frontend Environments

- **development**: Local development with hot reload
- **production**: Optimized production build

## Key Integration Points

### API Integration

**Backend API Structure:**
- Base path: `/api`
- Health check: `/api/v1/health`
- Authentication: JWT-based with Bearer tokens
- OpenAPI documentation available via Swagger

**Frontend API Integration:**
- API services in `frontend/src/api/`
- Axios interceptors for authentication
- Type-safe API calls with TypeScript

### Security Integration

**Authentication Flow:**
1. Frontend sends credentials to `/api/auth/login`
2. Backend validates and returns JWT token
3. Frontend stores token and includes in subsequent requests
4. Backend validates JWT on protected endpoints

## Build & Deployment

### Docker Deployment

The project includes comprehensive Docker configuration for production deployment:

```bash
# Build and run all services
docker-compose up -d

# Individual service management
docker-compose up -d mysql redis    # Infrastructure
docker-compose up -d backend         # Application
docker-compose up -d frontend nginx  # Frontend & Proxy
```

### Production Considerations

- **SSL/TLS**: Configure certificates in nginx/ssl/
- **Database**: Use production MySQL with proper backup strategy
- **Monitoring**: Prometheus and Grafana integration available
- **Logging**: Structured logging with log rotation
- **Security**: Enhanced security configurations for production

## Code Quality Standards

### Backend Standards

- **Java 17** features and best practices
- **Spring Boot 3.2.1** conventions
- **MyBatis Plus** for database operations
- **Lombok** for reducing boilerplate
- **Comprehensive test coverage** (85% minimum)
- **Clean code** with clear separation of concerns

### Frontend Standards

- **TypeScript** strict mode enabled
- **Vue 3 Composition API** patterns
- **ESLint + Prettier** for code formatting
- **Component-driven** development
- **Accessibility** considerations with Ant Design
- **Responsive design** requirements

## Common Development Workflows

### Adding New Features

1. **Backend Changes**:
   - Create domain entities and DTOs
   - Implement application services
   - Add REST controllers
   - Write comprehensive tests
   - Update OpenAPI documentation

2. **Frontend Changes**:
   - Create TypeScript interfaces for DTOs
   - Implement API services
   - Create Vue components
   - Update routing and state management
   - Add E2E tests for user flows

### Debugging Common Issues

**Database Connection Issues**:
- Check MySQL service status
- Verify credentials in application-dev.yml
- Ensure database schema is initialized

**CORS Issues**:
- Verify CORS configuration in CorsConfig
- Check frontend proxy settings in vite.config.ts

**Authentication Issues**:
- Verify JWT secret length (minimum 64 characters)
- Check security profile configuration
- Validate token format and expiration

This comprehensive setup provides a solid foundation for developing insurance audit system features with proper testing, security, and deployment capabilities.