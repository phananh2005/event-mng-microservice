# Event MNG Microservice Migration Roadmap

Tai lieu nay giu huong di tach monolith `event-mng` sang microservices theo tung buoc, khong pha vo he thong hien tai.

## Muc tieu

- Giu nguyen project `event-mng` de lam fallback trong qua trinh chuyen doi.
- Chuyen dan endpoint sang 6 service chinh qua `api-gateway`.
- Migrate theo chieu doc (vertical slice), uu tien chuc nang doc lap truoc.

## Mapping 6 module

1. `auth-service` - Authentication & Security
2. `event-service` - Event Management
3. `ticket-service` - Ticket Management
4. `booking-service` - Booking & Cart
5. `checkin-service` - Ticket Validation
6. `admin-service` - Admin Dashboard

## Gateway strategy (strangler pattern)

- Endpoint moi: `/api/v1/**` se route toi microservice tuong ung.
- Endpoint cu: `/event-mng/**` se route ve monolith `event-mng`.
- Khi endpoint da tach xong, frontend doi sang `/api/v1/**`, sau do moi tat endpoint cu.

## Sprint de xuat

### Sprint 1 - Auth va Event (nen tang)

- [x] Tach contract auth: login/register/refresh/logout/introspect.
- [x] Chuyen register/verify email sang `auth-service`.
- [x] Tach contract category doc (GET all, GET by id) sang `event-service` voi endpoint `/api/v1/categories/**`.
- [x] Tach category write APIs (POST/PUT/DELETE) sang `event-service`.
- [x] Tach event read APIs (GET list, GET by id) sang `event-service`.
- [x] Tach event write APIs (create/update/status).
- [x] Add smoke test cho 2 service tren gateway.

### Sprint 2 - Ticket + Booking

- [x] Chot boundary `TicketType` (definition vs inventory).
- [x] Tao API reserve/confirm/release giua booking va ticket.
- [x] Dam bao rollback ton kho khi payment fail.

### Sprint 3 - Checkin + Admin

- [x] Chuyen check-in qua `checkin-service`.
- [x] Chuyen thong ke tong hop qua `admin-service`.
- [x] Chot dashboard chi doc tu service moi.

## Data migration notes

- Auth data -> `auth_db`
- Event data -> `event_db`
- Ticket data -> `ticket_db`
- Booking data -> `booking_db`
- Check-in data -> `checkin_db`
- Admin aggregate data -> `admin_db`
- Monolith (`event_mng`) duoc giu trong giai doan chuyen tiep de tranh downtime.

## Tinh trang hien tai

- Da bo sung fallback route `/event-mng/**` trong `api-gateway`.
- Da route `/api/v1/categories/**` ve `event-service` (day du GET/POST/PUT/DELETE).
- Da route `/api/v1/events/**` ve `event-service` cho read APIs.
- Da bo sung service `event-mng` vao `docker-compose.yml` de chay song song voi he microservice.
- Da tach event write APIs (POST create, PUT update, PATCH status, GET admin/all, GET organizer/my-events) sang `event-service`.
  - OrganizerId lay tu JWT `sub` claim (khong can User entity trong event-service).
  - Ownership guard: ORGANIZER chi update event cua chinh minh, ADMIN bypass.
  - File upload giu nguyen local storage (`uploads/`) nhu monolith.
- Da them `GatewaySmokeTest` trong `api-gateway` (WireMock stub, WebTestClient):
  - Kiem tra route `/api/v1/auth/**` -> `auth-service`.
  - Kiem tra route `/api/v1/events/**` -> `event-service`.
  - Kiem tra route `/api/v1/categories/**` -> `event-service`.
  - Kiem tra fallback `/event-mng/**` -> legacy monolith.
- Da them `SecurityConfig` vao `api-gateway` (permit all — JWT validation thuoc ve tung downstream service).
- Sprint 2 hoan thanh:
  - `ticket-service`: owns `TicketType` (inventory) + `Ticket` (issued). APIs: GET ticket-types/event, GET/POST ticket-types/internal/reserve|release, GET my-tickets, POST check-in, POST tickets/internal/issue.
  - `booking-service`: owns `Cart`, `CartItem`, `Order`, `OrderItem`. APIs: full cart CRUD + checkout + checkout-selected + order history.
  - Feign client `TicketServiceClient` trong `booking-service` goi `ticket-service` de reserve stock truoc khi tao order, issue tickets sau khi thanh toan, va release neu reserve that bai (rollback).
  - Boundary: `ticketTypeId` la Long trong booking-service (khong co TicketType entity), price snapshot luu vao CartItem/OrderItem tai thoi diem add-to-cart.
  - Tests: `TicketTypeServiceTest` (reserve/release/not-enough), `OrderServiceTest` (checkout happy path, rollback, empty cart).
- Sprint 3 hoan thanh:
  - `checkin-service`: owns `CheckinLog` entity (audit trail moi lan quet). Goi `ticket-service` qua Feign de mark USED. Log luon duoc luu bat ke ket qua (SUCCESS/ALREADY_USED/INVALID/NOT_FOUND). APIs: `POST /api/v1/checkin/scan`, `GET /api/v1/checkin/event/{eventId}`.
  - `admin-service`: read-only dashboard. Owns `admin_db` voi native SQL queries tren orders/events/ticket_types. APIs: 4 stats endpoints duoi `/api/v1/admin/statistics/**` (by-status, by-temporal, revenue/organizer, revenue/admin). Tat ca ADMIN-only qua `@PreAuthorize`.
  - Tests: `CheckinServiceTest` (4 outcomes + always-persist), `StatisticsServiceTest` (percentage calc, zero-total, day-name mapping, monthly rollup).
