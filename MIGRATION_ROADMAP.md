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
- [ ] Tach event write APIs (create/update/status).
- [ ] Add smoke test cho 2 service tren gateway.

### Sprint 2 - Ticket + Booking

- [ ] Chot boundary `TicketType` (definition vs inventory).
- [ ] Tao API reserve/confirm/release giua booking va ticket.
- [ ] Dam bao rollback ton kho khi payment fail.

### Sprint 3 - Checkin + Admin

- [ ] Chuyen check-in qua `checkin-service`.
- [ ] Chuyen thong ke tong hop qua `admin-service`.
- [ ] Chot dashboard chi doc tu service moi.

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
