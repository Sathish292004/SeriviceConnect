# ServiceConnect — Complete Feature Documentation

## 1. Platform Overview

ServiceConnect is an on-demand local services marketplace platform connecting customers with verified trade and home service professionals. 

- **Customers** can discover certified service providers, explore catalog offerings, request bookings during provider working hours, complete simulated payments, author reviews after completed services, and submit support inquiries.
- **Providers** can onboard their business, publish catalog services with transparent pricing and durations, schedule weekly operating availability, upload portfolio photos directly from their computer, and manage booking requests from pending to completion.
- **Support Agents** manage an assigned customer support ticket queue, exchange real-time messages with customers, and resolve reported issues.
- **Administrators** maintain platform oversight through a centralized dashboard, approve or suspend provider accounts, oversee all support tickets across the platform, author Help Center articles, and monitor administrative audit logs.

---

## 2. Customer Features

### Account & Profile
- **Registration**: Customer can register a new account with email address, password, first name, last name, and phone number.
- **Login & Logout**: Customer can log in using email and password, receive a secure session, and log out on demand.
- **Email Verification**: Customer can request an email verification code and verify their email address.
- **Password Reset**: Customer can request a password reset code via email and reset their password.
- **Customer Profile**: Customer can view and update personal profile details including first name, last name, phone number, address, city, state, postal code, bio, and avatar photo URL.
- **Onboarding Status**: Customer can track whether their profile setup and onboarding steps are complete.

### Service Discovery
- **Public Catalog Exploration**: Customer can browse all active service offerings published by providers.
- **Search Services**: Customer can search services by keyword and filter offerings by category (such as Cleaning, Plumbing, Electrical, Carpentry, Painting).
- **Service Details**: Customer can view service title, description, category, fixed price, and estimated duration in minutes.
- **Provider Directory**: Customer can browse the directory of approved service providers with business names, ratings, total review count, city, state, and geographic location pins on an interactive map.

### Provider Profiles
Customer can open any provider profile and inspect:
- Provider business name and verified status badge.
- Detailed provider description and background information.
- Provider contact location, city, state, and service area.
- Weekly working hours and operating shift schedule (Monday through Sunday).
- Portfolio gallery displaying photos uploaded by the provider.
- Complete list of trade services offered by this provider with prices and durations.
- Customer ratings breakdown (1 to 5 stars) and previous customer reviews.
- Active availability slots for booking.

### Booking
- **Request a Service**: Customer can select a service and provider, choose an appointment date and time slot within the provider's active working hours, enter optional job notes, and submit a booking request.
- **Idempotent Submission**: Customer booking requests are protected against accidental duplicate submissions.
- **Price Lock**: The service price is captured as an immutable price snapshot at the moment of booking.
- **View Bookings**: Customer can view all past and upcoming bookings filtered by status tabs (All, Pending, Accepted, Completed, Cancelled).
- **Booking Details**: Customer can open an individual booking to view scheduled start and end times, assigned provider details, service notes, and status updates.
- **Cancel Booking**: Customer can cancel a booking while it is in `Pending` or `Accepted` status.
- **Track Booking Lifecycle**: Customer tracks real-time status transitions:
  - **Pending**: Booking requested, awaiting provider acceptance.
  - **Accepted**: Provider accepted the appointment slot.
  - **Rejected**: Provider was unable to accept the request.
  - **Completed**: Provider finished the service job.
  - **Cancelled**: Booking was cancelled by customer or provider.

### Payment
- **Service Checkout**: Customer can initiate payment for an accepted booking.
- **Payment Breakdown**: Customer reviews service price snapshot and currency details.
- **Simulated Payment Processing**: Customer selects a payment method, submits transaction details, and receives instant payment confirmation.
- **Payment Status Tracking**: Customer can view payment states (`Pending`, `Success`, `Failed`) and retry failed payments.

### Reviews
- **Author Review**: After a service is marked `Completed`, customer can author a review for the provider.
- **Star Rating**: Customer assigns a rating from 1 to 5 stars.
- **Feedback Comments**: Customer can write detailed feedback describing the service experience.
- **One Review per Booking**: The platform strictly enforces that each completed booking can receive exactly one review.
- **View Past Reviews**: Customer can view their historical reviews across providers.

### Support
- **Create Support Ticket**: Customer can open a support ticket by specifying title, problem description, category (Technical, Billing, General, Account), and priority (Low, Medium, High, Urgent).
- **View Own Tickets**: Customer can view all submitted support tickets with ticket numbers, current status, priority badges, and creation dates.
- **Ticket Messaging Conversation**: Customer can open any ticket and participate in a threaded conversation with support agents, sending follow-up replies in real time.
- **Track Resolution**: Customer can monitor when a ticket changes from `Open` to `In Progress` to `Resolved`.

### Help Center
- **Search Articles**: Customer can search published help articles by keyword.
- **Browse Categories**: Customer can browse articles categorized under Bookings, Payments, Account, Safety, and Policies.
- **Read Guides**: Customer can view full guide content and instructions.

### Settings
- **Account Preferences**: Customer can manage communication preferences, notification settings, and display options.
- **Security Settings**: Customer can change password (verifying current password), view active sessions, terminate all logged-in sessions, or request account deletion.

---

## 3. Provider Features

### Account & Onboarding
- **Provider Registration**: Provider can register a dedicated business account with business name, owner name, email, phone number, and password.
- **Provider Onboarding**: Provider completes guided onboarding to configure business details, bio, physical address, and service area coordinates.
- **Onboarding Status Tracker**: Provider can view onboarding completion status before receiving bookings.

### Business Profile
- **Profile Management**: Provider can edit business name, professional bio, trade specialties, address, city, state, and postal code.
- **Live Geolocation**: Provider can set and update latitude and longitude coordinates for map discovery.
- **View Public Appearance**: Provider can preview how their profile appears to searching customers.

### Services / Catalog
- **Create Services**: Provider can add new services to their offerings catalog with title, description, category, price in USD, and estimated duration in minutes.
- **Edit Services**: Provider can update service titles, descriptions, pricing, and durations at any time.
- **Activate / Deactivate Services**: Provider can toggle services active or inactive to temporarily hide them from public search without deleting them.
- **Delete Services**: Provider can permanently delete services from their catalog.
- **Categorize Services**: Provider assigns offerings to standard trade categories (e.g. Cleaning, Plumbing, Electrical).

### Availability
- **Weekly Schedule Setup**: Provider can configure working shifts for each day of the week (Monday through Sunday).
- **Set Working Hours**: Provider defines specific shift start times and end times per day.
- **Toggle Shift Active**: Provider can activate or deactivate availability on specific days.
- **Update / Delete Shifts**: Provider can modify or remove operating windows as business hours change.

### Portfolio
- **Direct Computer Upload**: Provider can upload portfolio photos directly from their computer without pasting external image URLs.
- **Image Format Support**: Accepts `JPG`, `PNG`, and `WEBP` image files.
- **File Size Validation**: Supports photos up to 5 MB per image with instant client-side format and size checks.
- **Upload Preview**: Provider sees an instant image preview with filename and formatted byte size before confirming upload.
- **Portfolio Gallery Grid**: Uploaded photos are stored on the server and immediately displayed in the provider's gallery grid.
- **Delete Portfolio Photos**: Provider can remove any uploaded photo with a single click, which permanently deletes the file from server storage and the database.
- **Customer Visibility**: Uploaded portfolio photos appear immediately on the provider's public profile for customers to view.

### Booking Management
- **Incoming Request Queue**: Provider can view all incoming booking requests submitted by customers.
- **Filter by Status**: Provider can filter booking requests by status (`Pending`, `Accepted`, `Completed`, `Rejected`, `Cancelled`).
- **Inspect Booking Details**: Provider views customer name, requested date/time slot, booked service, price snapshot, and job notes.
- **Accept Booking**: Provider can accept a pending request, reserving the slot and updating the booking status to `Accepted`.
- **Reject Booking**: Provider can decline a booking request if unable to perform the work, updating the status to `Rejected`.
- **Complete Booking**: Provider marks an accepted booking as `Completed` after finishing the trade service, which unlocks the review submission for the customer.

### Reviews
- **View Customer Feedback**: Provider can view all reviews, star ratings, and written comments left by customers for completed services.
- **Reputation Tracking**: Provider can monitor their overall average rating score and total review count.

### Settings
- **Provider Account Settings**: Provider can update account preferences, manage credentials, and change passwords.

---

## 4. Support Agent Features

### Ticket Queue
- **Assigned Ticket Queue**: Support Agent views a dedicated queue displaying only support tickets assigned to their account.
- **Ticket Summary Information**: Agent sees ticket subject, unique ticket number, customer ID, priority badge, current status badge, and relative submission time.
- **Pagination**: Agent can navigate paginated lists of assigned tickets.

### Ticket Details
- **Open Ticket**: Agent can click any ticket to open the full detail view.
- **Customer Context**: Agent reads the customer's initial problem description, ticket number, and creation timestamp.
- **Priority & Status Badges**: Agent views current priority (`Low`, `Medium`, `High`, `Urgent`) and status (`Open`, `In Progress`, `Pending Customer`, `Resolved`).

### Customer Communication
- **Conversation Thread**: Agent views the full chronological messaging history between customer, agents, and admin.
- **Send Agent Reply**: Agent can type response messages and submit them directly into the conversation thread.
- **Visual Sender Identification**: Messages clearly indicate sender role and timestamp so conversation context is unambiguous.

### Ticket Status
- **Update Status**: Support Agent can update the ticket status as progress occurs:
  - Set status to **In Progress** while actively investigating.
  - Set status to **Pending Customer** when waiting for customer information.
  - Set status to **Resolved** when the issue has been successfully resolved.
- **Resolution Confirmation**: Once resolved, the resolution propagates to the customer's portal.

### Access Restrictions
- **Role Isolation**: Support Agent has access **only** to support ticket queues and ticket conversation features (`/support/tickets/**`).
- **No Admin Dashboard**: Support Agent cannot access the platform Admin Dashboard (`/admin/dashboard`).
- **No Provider Governance**: Support Agent cannot approve, reject, or suspend providers.
- **No Help Center CMS**: Support Agent cannot create, edit, or delete Help Center articles.
- **No Audit Logs**: Support Agent cannot inspect administrative audit logs.
- **Enforced Denial**: If a Support Agent attempts to navigate to any Admin-only route, the system blocks access and displays the `403 Forbidden` Access Denied screen.

---

## 5. ADMIN FEATURES

The Administrator role provides platform-wide governance, operational supervision, provider compliance, and content management.

### Admin Dashboard
- **Centralized Control Center**: Admin accesses the top-level dashboard (`/admin/dashboard`) with dedicated management navigation cards.
- **Operational Shortcuts**: One-click navigation to Providers Management, Support Tickets, Help Center, and Audit Logs.
- **System Monitoring**: Overview of platform functional areas and overall operational status.

### Provider Management
Admin has full administrative authority over service providers on the platform:
- **View All Providers**: Admin can view the complete list of all registered providers across the system.
- **Status Filtering Tabs**: Admin can filter the provider directory by status tabs:
  - **All**: View every registered provider regardless of status.
  - **Pending**: View newly registered providers awaiting verification.
  - **Approved**: View currently active, approved providers.
  - **Rejected**: View providers whose applications were declined.
  - **Suspended**: View providers whose accounts have been suspended.
- **Open Provider Profile**: Admin can open any individual provider profile (`/admin/providers/:id`) to review complete business information:
  - Business name and current status badge.
  - Contact email and phone number.
  - Physical street address, city, and state.
  - Account registration date and timestamp.
  - Business description and trade bio.
- **Approve Provider**: When a provider is in `Pending` status, Admin can click **Approve** (`APPROVED`). This activates the provider, allows their services to appear in public search, and permits them to receive customer bookings.
- **Reject Provider**: When a provider is in `Pending` status, Admin can click **Reject** (`REJECTED`) to decline the provider application.
- **Suspend Provider**: For an active `Approved` provider, Admin can click **Suspend** (`SUSPENDED`) to deactivate the provider (e.g. for violations or customer complaints), removing them from public discovery.
- **Re-approve Provider**: For any provider in `Rejected` or `Suspended` status, Admin can click **Re-approve** (`APPROVED`) to restore them to active standing.
- **Delete Provider**: Admin has backend authority to permanently delete a provider record.

### Support Ticket Management
Admin has platform-wide ticketing oversight:
- **View Global Tickets**: Admin can view all support tickets submitted by all customers across the entire platform.
- **Filter by Status**: Admin can filter tickets using status tabs (`All`, `Open`, `In Progress`, `Pending Customer`, `Resolved`, `Closed`).
- **Ticket Summary Details**: Admin views subject, unique ticket number, customer ID, priority badge, and status.
- **Open Any Ticket**: Admin can inspect the full issue description and timestamp of any platform ticket.
- **Send Admin Message**: Admin can post administrative messages into the ticket conversation thread.
- **Update Ticket Status**: Admin can update ticket status to `In Progress`, `Pending Customer`, `Resolved`, or permanently mark tickets as `Closed`.
- **Assign Tickets to Agents**: Admin can assign or reassign unassigned tickets to specific Support Agents.

### Help Center Management
Admin has complete content management authority over the Help Center knowledge base:
- **Article Repository**: Admin views a paginated table of all Help Center articles displaying title, category, URL slug, creation date, and publication status.
- **Publication Indicator**: Visual indicator shows whether an article is published (active/visible to public) or unpublished (draft/hidden).
- **Create Help Article**: Admin can open `/admin/help-center/new` to publish a new guide with:
  - Article Title
  - Unique URL Slug (e.g. `how-to-book`, `refund-policy`)
  - Category (e.g. `BOOKINGS`, `PAYMENTS`, `ACCOUNT`, `SAFETY`)
  - Display Order (numeric integer determining sorting order)
  - Content (rich guide body supporting formatted text and HTML)
  - Published toggle (checkbox to make immediately live or save as draft)
- **Edit Help Article**: Admin can open `/admin/help-center/:id` to modify titles, slugs, categories, display orders, content, or publication status.
- **Delete Help Article**: Admin can delete obsolete articles from the knowledge base with a single click.

### Audit Logs
Admin can inspect the immutable system audit trail to track operational and administrative activities:
- **Audit Log Table**: Admin views a paginated audit log table displaying:
  - **Action**: Exact operation performed (e.g. `PROVIDER_APPROVED`, `PROVIDER_SUSPENDED`, `TICKET_STATUS_UPDATED`, `ARTICLE_CREATED`, `LOGIN_SUCCESS`).
  - **Actor**: User ID and role of the user who performed the action (e.g. `#24 (ADMIN)`, `#22 (CUSTOMER)`).
  - **Resource**: The entity type affected (e.g. `PROVIDER #7`, `TICKET #8`, `HELP_ARTICLE #2`).
  - **Date**: Exact timestamp of when the action occurred.
- **Activity Monitoring**: Allows Admin to audit security events, compliance actions, and administrative changes across the system.

### Admin Access Control
- **Superuser Privileges**: Admin has access to all administrative interfaces (`/admin/**`) and all public and domain features.
- **Exclusive Functions**: Only Admin can approve/reject/suspend providers, author Help Center articles, view system audit logs, and permanently close tickets.
- **Support Agent Differentiation**: Support Agents are strictly restricted to assigned ticket communication and cannot access Admin management features.

---

## 6. Help Center Features

The Help Center operates as a self-service knowledge base:

- **Customer Capabilities**:
  - Search help articles using keyword search.
  - Browse articles by category taxonomy (`BOOKINGS`, `PAYMENTS`, `ACCOUNT`, `SAFETY`, `POLICIES`).
  - Open individual articles via clean category URLs and article slugs.
  - Read formatted guide content and troubleshooting steps.
- **Provider Capabilities**:
  - Providers can access all public Help Center articles to learn about onboarding rules, catalog standards, availability settings, and cancellation terms.
- **Support Agent Capabilities**:
  - Support Agents can reference Help Center articles when communicating with customers in support tickets.
- **Admin Capabilities**:
  - Complete authoring and publishing control: create articles, edit content, set URL slugs, assign categories, define display order sorting, toggle published/draft state, and delete articles.

---

## 7. Role Comparison

The table below summarizes the implemented capabilities across all user roles:

| Feature / Capability | Customer | Provider | Support Agent | Admin |
|:---|:---:|:---:|:---:|:---:|
| **Account Registration & Login** | ✓ | ✓ | ✓ | ✓ |
| **Manage Personal Profile** | ✓ | ✓ | ✓ | ✓ |
| **Manage Account Settings & Security** | ✓ | ✓ | ✓ | ✓ |
| **Browse Services & Search Catalog** | ✓ | ✓ | ✓ | ✓ |
| **Browse Providers & View Profiles** | ✓ | ✓ | ✓ | ✓ |
| **View Provider Portfolio Photos** | ✓ | ✓ | ✓ | ✓ |
| **View Help Center Articles** | ✓ | ✓ | ✓ | ✓ |
| **Request Service Booking** | ✓ | — | — | — |
| **Cancel Own Booking** | ✓ | — | — | — |
| **Make Simulated Service Payment** | ✓ | — | — | — |
| **Submit Service Review & Rating** | ✓ | — | — | — |
| **Create Customer Support Ticket** | ✓ | — | — | — |
| **Send Messages in Own Tickets** | ✓ | — | — | — |
| **Manage Business Profile & Bio** | — | ✓ | — | — |
| **Create, Edit & Delete Catalog Services** | — | ✓ | — | — |
| **Activate / Deactivate Services** | — | ✓ | — | — |
| **Manage Working Hours & Availability** | — | ✓ | — | — |
| **Direct Upload Portfolio Photos (Computer)**| — | ✓ | — | — |
| **Delete Own Portfolio Photos** | — | ✓ | — | — |
| **Accept / Reject Booking Requests** | — | ✓ | — | — |
| **Mark Booking as Completed** | — | ✓ | — | — |
| **View Assigned Support Tickets** | — | — | ✓ | ✓ |
| **Reply to Assigned Tickets** | — | — | ✓ | ✓ |
| **Resolve Support Tickets** | — | — | ✓ | ✓ |
| **Access Admin Dashboard** | — | — | — | ✓ |
| **View All Platform Providers** | — | — | — | ✓ |
| **Approve / Reject / Suspend Providers** | — | — | — | ✓ |
| **View All Platform Support Tickets** | — | — | — | ✓ |
| **Assign Tickets to Support Agents** | — | — | — | ✓ |
| **Close Support Tickets** | — | — | — | ✓ |
| **Create, Edit & Delete Help Articles** | — | — | — | ✓ |
| **Publish / Unpublish Help Articles** | — | — | — | ✓ |
| **View System Security Audit Logs** | — | — | — | ✓ |

---

## 8. Complete Feature List

A concise checklist of all discovered, user-facing features implemented in the application:

### Customer Features
- [x] Register account with email, password, name, phone
- [x] Login and logout with secure session
- [x] Email verification code challenge
- [x] Password reset via email code
- [x] View and edit customer profile (name, phone, address, city, state, postal code, bio, avatar)
- [x] Onboarding progress status tracker
- [x] Account preferences and notification settings
- [x] Password change and session termination
- [x] Public and authenticated service catalog browsing
- [x] Search services by keyword and category filters
- [x] View service price and duration
- [x] Provider directory with interactive map markers
- [x] Provider public profile with bio, hours, photos, ratings, services
- [x] Request service booking with date/time slot selection
- [x] View booking list filtered by status tabs
- [x] View booking detail with status tracking
- [x] Cancel pending or accepted bookings
- [x] Simulated card checkout payment
- [x] Payment confirmation and failure retry
- [x] Submit 1-to-5 star rating and review on completed booking
- [x] Unique review enforcement per booking
- [x] Create support ticket with category and priority
- [x] View submitted tickets list and status
- [x] Threaded ticket conversation messaging with support agents
- [x] Browse and search Help Center articles

### Provider Features
- [x] Register provider business account
- [x] Provider onboarding form for business setup
- [x] Onboarding completion status check
- [x] Edit business profile, bio, address, and live coordinates
- [x] Provider dashboard with booking overview
- [x] Create catalog services with price, duration, category
- [x] Edit catalog service details
- [x] Activate and deactivate catalog services
- [x] Delete catalog services
- [x] Set weekly operating hours (Monday–Sunday)
- [x] Define shift start and end times
- [x] Activate or deactivate shift days
- [x] Direct portfolio photo upload from computer (JPG, PNG, WEBP up to 5MB)
- [x] Instant client-side photo preview with filename and size
- [x] View portfolio gallery grid
- [x] Delete portfolio photos (removes from server storage)
- [x] View incoming booking requests queue
- [x] Filter booking requests by status
- [x] Inspect booking details and customer notes
- [x] Accept booking requests
- [x] Reject booking requests
- [x] Mark accepted bookings as completed
- [x] View customer reviews and rating scores
- [x] Account settings and password management

### Support Agent Features
- [x] Support agent login
- [x] View assigned support tickets queue
- [x] Filter and paginate assigned tickets
- [x] Open ticket detail view
- [x] Read customer problem description and ticket metadata
- [x] View chronological ticket message thread
- [x] Send agent reply messages into ticket thread
- [x] Update ticket status to In Progress
- [x] Update ticket status to Pending Customer
- [x] Update ticket status to Resolved
- [x] Enforced role isolation (blocked from Admin dashboard, providers, help articles, audit logs)

### Admin Features
- [x] Admin login
- [x] Admin dashboard with navigation cards
- [x] View all platform providers list
- [x] Filter providers by status (All, Pending, Approved, Rejected, Suspended)
- [x] View provider profile details (business, contact, address, description, status)
- [x] Approve pending providers
- [x] Reject pending providers
- [x] Suspend approved providers
- [x] Re-approve rejected or suspended providers
- [x] Delete provider accounts
- [x] View all support tickets across the platform
- [x] Filter global tickets by status
- [x] Open any platform ticket detail
- [x] Send administrative reply messages in ticket thread
- [x] Update ticket status (In Progress, Pending Customer, Resolved, Closed)
- [x] Assign tickets to specific support agents
- [x] View all Help Center articles with publication indicator
- [x] Create new Help Center articles (title, slug, category, display order, content, published)
- [x] Edit existing Help Center articles
- [x] Delete Help Center articles
- [x] Toggle article published / draft visibility
- [x] View system audit logs table (action, actor, role, resource, date)
- [x] Superuser access across all administrative operations

### Shared / Public Features
- [x] Public marketing landing page
- [x] Unauthenticated service catalog discovery
- [x] Unauthenticated provider directory browsing
- [x] Unauthenticated provider profile and portfolio photo viewing
- [x] Public Help Center knowledge base search and reading
- [x] Legal pages (Privacy Policy, Terms of Service, Cookies, Cancellation, Refunds)
- [x] Interactive cookie consent preferences
- [x] Standard error screens (404 Not Found, 403 Access Denied, 500 Server Error, Session Expired)

---

## 9. Main User Workflows

### Customer Workflow

```text
Customer
   ↓
Register / Login
   ↓
Browse Catalog Services
   ↓
Discover & Select Provider
   ↓
Inspect Provider Profile (Hours, Services, Photos, Reviews)
   ↓
Select Service & Choose Available Time Slot
   ↓
Submit Booking Request (Status: Pending)
   ↓
Provider Accepts (Status: Accepted)
   ↓
Service Executed & Marked Finished (Status: Completed)
   ↓
Complete Simulated Payment Checkout
   ↓
Submit 1-to-5 Star Review & Written Feedback
```

### Provider Workflow

```text
Provider
   ↓
Register Business Account
   ↓
Complete Onboarding & Profile Details
   ↓
Publish Catalog Services (Set Price, Duration, Category)
   ↓
Configure Weekly Operating Hours (Days & Shift Times)
   ↓
Upload Portfolio Photos Directly from Computer
   ↓
Receive Customer Booking Request in Queue
   ↓
Inspect Request Details & Accept Booking
   ↓
Deliver Service to Customer
   ↓
Mark Booking as Completed
   ↓
Review Customer Rating & Feedback
```

### Support Workflow

```text
Customer Submits Support Ticket (Issue Description & Priority)
   ↓
Ticket Enters Platform Queue
   ↓
Assigned to Support Agent
   ↓
Support Agent Opens Ticket Detail
   ↓
Agent Reviews Issue & Sends Threaded Message Reply
   ↓
Customer Receives Reply & Sends Follow-up
   ↓
Support Agent Sets Status to Resolved
   ↓
Customer Confirms Issue Resolution
```

### Admin Workflow

```text
Administrator
   ↓
Admin Dashboard Overview
   │
   ├── Provider Management
   │     ├── Review Pending Registrations
   │     ├── Approve Verified Providers
   │     └── Suspend Non-Compliant Accounts
   │
   ├── Ticket Management
   │     ├── Monitor Global Platform Tickets
   │     ├── Assign Tickets to Support Agents
   │     ├── Intervene & Reply to Inquiries
   │     └── Close Inactive or Finished Tickets
   │
   ├── Help Center Management
   │     ├── Author New Help Guides & FAQs
   │     ├── Update Categories & Article Slugs
   │     └── Publish or Unpublish Content
   │
   └── Security Audit Logs
         ├── Inspect System Activity Trail
         ├── Review Administrative Decisions
         └── Verify Actor Roles & Operational Timestamps
```
