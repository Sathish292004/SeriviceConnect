# ServiceConnect — Project Features

## 1. Customer Features

### Account
- Customer registration and login
- Email verification
- JWT-based authentication
- Customer profile management
- Account settings
- Address management

### Service Discovery
- Browse available services
- Search and filter services
- Browse service providers
- View provider profiles
- View provider business information
- View provider working hours
- View provider portfolio/photos
- View provider services and pricing
- Check provider availability

### Booking
- Request a service
- Select service and provider
- Select preferred date/time
- View booking status
- Cancel booking where permitted
- Track booking lifecycle:
  - Pending
  - Accepted
  - Rejected
  - Completed
  - Cancelled

### Payment
- Service payment checkout
- Payment status/transaction verification
- Payment-related booking flow

### Reviews
- Submit a review after a completed service
- Give rating
- View provider reviews
- One review per booking

### Support
- Create support tickets
- View own tickets
- Send messages in ticket conversations
- Track ticket status
- Access Help Center articles


## 2. Provider Features

### Provider Account
- Provider registration/login
- Provider onboarding
- Provider profile management
- Business information management
- Operating hours management
- Account settings

### Services / Catalog
- Create services
- Edit services
- Delete services
- Set service price
- Set service duration
- Activate/deactivate services
- Manage service categories

### Portfolio
- Upload provider photos directly from the computer
- JPG / PNG / WEBP support
- Maximum 5 MB per image
- Preview uploaded image
- View portfolio gallery
- Delete portfolio photos

### Booking Management
- View incoming service requests
- Accept booking
- Reject booking
- Complete booking
- View booking details
- Manage service availability

### Provider Profile

Customers can see:
- Provider name/business
- Description
- Location
- Working hours
- Services
- Prices
- Portfolio photos
- Ratings/reviews
- Availability


## 3. Support Agent Features

### Ticket Management
- View assigned support tickets
- Open ticket details
- Reply to customers
- View conversation history
- Update ticket status
- Resolve support tickets

### Access Control
- Support Agent has access to support-related functions
- Cannot access full Admin functionality


## 4. Admin Features

### Dashboard
- View platform KPIs
- Monitor platform activity

### Provider Management

Admin can:
- View providers
- View provider profiles
- Manage provider status
- Approve/manage provider accounts
- Control provider availability/status

### Ticket Management
- View all support tickets
- Assign/manage tickets
- Monitor customer issues
- Manage ticket status
- View conversations

### Help Center Management
- Create help articles
- Edit articles
- Delete articles
- Manage categories
- Manage published help content

### Audit Logs
- View system audit logs
- Monitor administrative activities

### Access Control
- Admin has administrative privileges
- Support Agent is restricted from Admin-only functions


## 5. Help Center

- Browse help articles
- Search help content
- Browse by category
- Customer-facing Help Center
- Admin can manage articles and categories


## 6. Authentication & Security

- JWT authentication
- Role-based access control

### Roles
- Customer
- Provider
- Support Agent
- Admin

- Protected pages based on role
- Protected backend APIs
- Password hashing
- Access/refresh token system


## 7. Main Business Flow

### Customer → Provider → Service

```text
Customer
   ↓
Browse Services
   ↓
Select Provider
   ↓
View Provider Profile
   ↓
Select Service
   ↓
Request Booking
   ↓
Provider Accepts
   ↓
Service Completed
   ↓
Payment
   ↓
Customer Reviews Provider
```

```text
Customer
   ↓
Create Support Ticket
   ↓
Support Agent
   ↓
Reply / Handle Issue
   ↓
Resolve Ticket
```

```text
Admin
 ├── Manage Providers
 ├── Manage Support Tickets
 ├── Manage Help Center
 ├── View Audit Logs
 └── View Platform Dashboard
```
