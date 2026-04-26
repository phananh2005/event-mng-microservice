# API Reference — Event MNG Microservice

## Thứ tự khởi động

```
MySQL (3306)
        ↓
service-registry (8761)
        ↓
auth-service (8081)  +  event-service (8082)  +  ticket-service (8083)
        ↓
booking-service (8084)  +  checkin-service (8085)  +  admin-service (8086)
        ↓
api-gateway (8080)
```

| Bước | Service | Port | Ghi chú |
|------|---------|------|---------|
| 1 | MySQL | 3306 | Bắt buộc trước tiên |
| 2 | service-registry | 8761 | Eureka — tất cả service register vào đây |
| 3 | event-mng *(optional)* | 8090 | Monolith fallback cho `/event-mng/**`, có thể bỏ qua |
| 4 | auth-service | 8081 | Không phụ thuộc service khác |
| 4 | event-service | 8082 | Không phụ thuộc service khác |
| 4 | ticket-service | 8083 | Không phụ thuộc service khác |
| 5 | booking-service | 8084 | Feign → ticket-service |
| 5 | checkin-service | 8085 | Feign → ticket-service |
| 5 | admin-service | 8086 | Không phụ thuộc service khác |
| 6 | api-gateway | 8080 | Chạy cuối — cần các service đã register vào Eureka |

> Sau khi start mỗi service, chờ **10–15 giây** để register xong vào Eureka.  
> Kiểm tra tại `http://localhost:8761` — service phải có trạng thái **UP** trước khi start service phụ thuộc vào nó.

---

Tat ca request/response deu boc trong envelope chung:

```json
{
  "code": 1000,
  "message": null,
  "result": { ... }
}
```

Base URL qua gateway: `http://localhost:8080`

---

## Swagger UI

Moi service expose Swagger UI rieng. Truy cap **truc tiep vao port cua service** (khong qua gateway).
Cac endpoint can auth: click **Authorize** → nhap `Bearer <token>` lay tu `POST /api/v1/auth/login`.

| Service | Port | Swagger UI | API Docs (JSON) |
|---------|------|------------|-----------------|
| auth-service | 8081 | [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html) | [http://localhost:8081/api-docs](http://localhost:8081/api-docs) |
| event-service | 8082 | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) | [http://localhost:8082/api-docs](http://localhost:8082/api-docs) |
| ticket-service | 8083 | [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) | [http://localhost:8083/api-docs](http://localhost:8083/api-docs) |
| booking-service | 8084 | [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html) | [http://localhost:8084/api-docs](http://localhost:8084/api-docs) |
| checkin-service | 8085 | [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html) | [http://localhost:8085/api-docs](http://localhost:8085/api-docs) |
| admin-service | 8086 | [http://localhost:8086/swagger-ui.html](http://localhost:8086/swagger-ui.html) | [http://localhost:8086/api-docs](http://localhost:8086/api-docs) |

---

## Muc luc

1. [Auth Service](#1-auth-service--port-8081)
2. [Event Service — Categories](#2-event-service--categories--port-8082)
3. [Event Service — Events](#3-event-service--events--port-8082)
4. [Ticket Service — Ticket Types](#4-ticket-service--ticket-types--port-8083)
5. [Ticket Service — Tickets](#5-ticket-service--tickets--port-8083)
6. [Booking Service — Cart](#6-booking-service--cart--port-8084)
7. [Booking Service — Orders](#7-booking-service--orders--port-8084)
8. [Checkin Service](#8-checkin-service--port-8085)
9. [Admin Service — Statistics](#9-admin-service--statistics--port-8086)
10. [Internal APIs](#10-internal-apis-service-to-service)

---

## 1. Auth Service — port 8081

Base path: `/api/v1/auth`

### POST `/api/v1/auth/login`

Dang nhap, tra ve JWT access token va refresh token.

**Auth:** Public

**Request body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Response `result`:**
```json
{
  "token": "eyJhbGci..."
}
```

---

### POST `/api/v1/auth/register`

Dang ky tai khoan moi. Gui email xac thuc sau khi tao.

**Auth:** Public

**Request body:**
```json
{
  "username": "string (3–50 chars, required)",
  "password": "string (6–100 chars, required)",
  "email":    "string (valid email, required)",
  "fullName": "string (max 100, required)",
  "phone":    "string (10–11 digits, optional)",
  "address":  "string (max 255, optional)",
  "role":     "CUSTOMER | ORGANIZER (optional, default CUSTOMER)"
}
```

**Response `result`:** `"Please check your email to verify your account"`

---

### GET `/api/v1/auth/verify?token={token}`

Xac thuc email sau khi dang ky.

**Auth:** Public

**Query params:**

| Param | Type | Required |
|-------|------|----------|
| `token` | string | yes |

**Response:** Plain text `"Email verified successfully"`

---

### POST `/api/v1/auth/introspect`

Kiem tra token con hieu luc khong.

**Auth:** Public

**Request body:**
```json
{ "token": "eyJhbGci..." }
```

**Response `result`:**
```json
{ "valid": true }
```

---

### POST `/api/v1/auth/refresh`

Lay access token moi tu refresh token.

**Auth:** Public

**Request body:**
```json
{ "token": "eyJhbGci..." }
```

**Response `result`:**
```json
{ "token": "eyJhbGci..." }
```

---

### POST `/api/v1/auth/logout`

Huy hieu luc token hien tai.

**Auth:** Bearer token

**Request body:**
```json
{ "token": "eyJhbGci..." }
```

**Response `result`:** `null`

---

## 2. Event Service — Categories — port 8082

Base path: `/api/v1/categories`

### GET `/api/v1/categories`

Lay danh sach tat ca danh muc.

**Auth:** Public

**Response `result`:**
```json
[
  { "id": 1, "name": "Music", "description": "..." }
]
```

---

### GET `/api/v1/categories/{id}`

Lay chi tiet mot danh muc.

**Auth:** Public

**Path params:** `id` (Long)

**Response `result`:**
```json
{ "id": 1, "name": "Music", "description": "..." }
```

---

### POST `/api/v1/categories`

Tao danh muc moi.

**Auth:** Bearer token — ADMIN

**Request body:**
```json
{
  "name":        "string (min 5 chars, required)",
  "description": "string (optional)"
}
```

**Response `result`:** Category object

---

### PUT `/api/v1/categories/{id}`

Cap nhat danh muc.

**Auth:** Bearer token — ADMIN

**Path params:** `id` (Long)

**Request body:** Same as POST

**Response `result`:** Updated category object

---

### DELETE `/api/v1/categories/{id}`

Xoa danh muc.

**Auth:** Bearer token — ADMIN

**Path params:** `id` (Long)

**Response `result`:** `null`

---

## 3. Event Service — Events — port 8082

Base path: `/api/v1/events`

### GET `/api/v1/events`

Lay danh sach su kien da phat hanh (UPCOMING / OPENING / CLOSED), phan trang.

**Auth:** Public

**Query params:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 1 | So trang (bat dau tu 1) |
| `size` | int | 10 | So phan tu moi trang |

**Response `result`:** Page of event objects

---

### GET `/api/v1/events/{id}`

Lay chi tiet su kien.

**Auth:** Public

**Path params:** `id` (Long)

**Response `result`:**
```json
{
  "id": 1,
  "name": "string",
  "categoryName": "string",
  "organizerName": "string",
  "location": "string",
  "startTime": "2025-01-01T09:00:00",
  "endTime": "2025-01-01T18:00:00",
  "saleStartDate": "2024-12-01T00:00:00",
  "saleEndDate": "2025-01-01T08:00:00",
  "description": "string",
  "status": "UPCOMING",
  "imageUrls": ["https://..."],
  "ticketTypes": [
    { "id": 1, "name": "VIP", "price": 500000, "totalQuantity": 100, "remainingQuantity": 80, "description": "..." }
  ],
  "createdAt": "2024-11-01T10:00:00",
  "updatedAt": "2024-11-15T12:00:00"
}
```

---

### POST `/api/v1/events`

Tao su kien moi. Multipart form data.

**Auth:** Bearer token — ORGANIZER

**Content-Type:** `multipart/form-data`

**Form fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | yes | Ten su kien |
| `categoryId` | Long | yes | ID danh muc |
| `location` | string | no | Dia diem |
| `startTime` | ISO datetime | yes | `2025-01-01T09:00:00` |
| `endTime` | ISO datetime | yes | `2025-01-01T18:00:00` |
| `saleStartDate` | ISO datetime | no | Bat dau ban ve |
| `saleEndDate` | ISO datetime | no | Ket thuc ban ve |
| `description` | string | no | Mo ta |
| `status` | EventStatus | no | Default: `PENDING` |
| `files` | MultipartFile[] | no | Anh su kien |
| `ticketTypes[0].name` | string | no | Ten loai ve |
| `ticketTypes[0].price` | decimal | no | Gia ve |
| `ticketTypes[0].totalQuantity` | int | no | So luong |
| `ticketTypes[0].description` | string | no | Mo ta loai ve |

**Response `result`:** Event object

---

### PUT `/api/v1/events/{id}`

Cap nhat su kien. Multipart form data.

**Auth:** Bearer token — ORGANIZER (chu su kien) hoac ADMIN

**Path params:** `id` (Long)

**Form fields:** Same as POST

**Response `result`:** Updated event object

---

### PATCH `/api/v1/events/{id}/status`

Cap nhat trang thai su kien.

**Auth:** Bearer token — ADMIN

**Path params:** `id` (Long)

**Query params:**

| Param | Type | Values |
|-------|------|--------|
| `status` | EventStatus | `PENDING` \| `UPCOMING` \| `OPENING` \| `CLOSED` \| `COMPLETED` \| `CANCELLED` |

**Response `result`:** Updated event object

---

### GET `/api/v1/events/admin/all`

Lay tat ca su kien voi bo loc (danh cho admin).

**Auth:** Bearer token — ADMIN

**Query params:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `page` | int | 1 | |
| `size` | int | 12 | |
| `search` | string | `""` | Tim kiem theo ten |
| `status` | EventStatus | null | Loc theo trang thai |

**Response `result`:** Page of event objects

---

### GET `/api/v1/events/organizer/my-events`

Lay danh sach su kien cua organizer dang dang nhap.

**Auth:** Bearer token — ORGANIZER

**Query params:** `page` (default 1), `size` (default 10)

**Response `result`:** Page of event objects

---

## 4. Ticket Service — Ticket Types — port 8083

Base path: `/api/v1/ticket-types`

### GET `/api/v1/ticket-types/event/{eventId}`

Lay danh sach loai ve theo su kien.

**Auth:** Public

**Path params:** `eventId` (Long)

**Response `result`:**
```json
[
  {
    "id": 1,
    "eventId": 10,
    "name": "VIP",
    "price": 500000,
    "totalQuantity": 100,
    "remainingQuantity": 75,
    "description": "..."
  }
]
```

---

### GET `/api/v1/ticket-types/{id}`

Lay chi tiet mot loai ve.

**Auth:** Public

**Path params:** `id` (Long)

**Response `result`:** TicketType object

---

## 5. Ticket Service — Tickets — port 8083

Base path: `/api/v1/tickets`

### GET `/api/v1/tickets/my-tickets`

Lay danh sach ve da mua cua nguoi dung dang dang nhap.

**Auth:** Bearer token — any authenticated user

**Response `result`:**
```json
[
  {
    "id": 1,
    "orderId": 5,
    "ticketTypeName": "VIP",
    "eventName": null,
    "ticketCode": "TKT-A1B2C3D4",
    "qrCode": "https://api.qrserver.com/...",
    "status": "VALID",
    "usedAt": null
  }
]
```

---

### POST `/api/v1/tickets/check-in`

Quet ma ve de check-in.

**Auth:** Bearer token — ADMIN hoac ORGANIZER

**Query params:**

| Param | Type | Required |
|-------|------|----------|
| `ticketCode` | string | yes |

**Response `result`:** Ticket object voi `status: "USED"`

**Error codes:**

| HTTP | Mo ta |
|------|-------|
| 409 | Ticket da duoc su dung (`TICKET_USED`) |
| 400 | Ticket khong hop le (`TICKET_INVALID`) |
| 404 | Khong tim thay ticket (`TICKET_NOT_FOUND`) |

---

## 6. Booking Service — Cart — port 8084

Base path: `/api/v1/cart`

### GET `/api/v1/cart`

Lay gio hang hien tai.

**Auth:** Bearer token — any authenticated user

**Response `result`:**
```json
{
  "id": 1,
  "items": [
    {
      "id": 1,
      "ticketTypeId": 3,
      "ticketTypeName": "VIP",
      "eventName": "Music Show",
      "quantity": 2,
      "unitPrice": 500000,
      "subtotal": 1000000
    }
  ],
  "totalAmount": 1000000
}
```

---

### POST `/api/v1/cart/add`

Them ve vao gio hang. Kiem tra ton kho truoc khi them.

**Auth:** Bearer token — any authenticated user

**Request body:**
```json
{
  "ticketTypeId": 3,
  "quantity": 2
}
```

**Response `result`:** Cart object

**Error codes:**

| HTTP | Mo ta |
|------|-------|
| 409 | Khong du ve (`TICKET_NOT_ENOUGH`) |
| 404 | Khong tim thay loai ve (`TICKET_TYPE_NOT_FOUND`) |

---

### PUT `/api/v1/cart/items/{itemId}`

Cap nhat so luong ve trong gio. Neu `quantity <= 0` thi xoa item.

**Auth:** Bearer token — any authenticated user

**Path params:** `itemId` (Long)

**Query params:**

| Param | Type | Required |
|-------|------|----------|
| `quantity` | int | yes |

**Response `result`:** Updated cart object

---

### DELETE `/api/v1/cart/items/{itemId}`

Xoa mot item khoi gio hang.

**Auth:** Bearer token — any authenticated user

**Path params:** `itemId` (Long)

**Response `result`:** Updated cart object

---

### DELETE `/api/v1/cart/clear`

Xoa toan bo gio hang.

**Auth:** Bearer token — any authenticated user

**Response `result`:** `null`

---

## 7. Booking Service — Orders — port 8084

Base path: `/api/v1/bookings`

### POST `/api/v1/bookings/checkout`

Thanh toan toan bo gio hang. Flow: reserve stock → tao order → issue tickets → xoa items khoi gio.

**Auth:** Bearer token — any authenticated user

**Query params:**

| Param | Type | Values |
|-------|------|--------|
| `paymentMethod` | PaymentMethod | `MOMO` \| `VNPAY` \| `BANKING` |

**Response `result`:**
```json
{
  "id": 99,
  "totalAmount": 1000000,
  "serviceFee": 250000,
  "organizerAmount": 750000,
  "platformFeeRate": 0.25,
  "paymentMethod": "BANKING",
  "paymentStatus": "PAID",
  "orderStatus": "CONFIRMED",
  "orderDate": "2025-01-15T10:30:00"
}
```

**Error codes:**

| HTTP | Mo ta |
|------|-------|
| 400 | Gio hang trong (`CART_EMPTY`) |
| 409 | Khong du ve khi reserve (`TICKET_NOT_ENOUGH`) |

---

### POST `/api/v1/bookings/checkout-selected`

Thanh toan cac item duoc chon trong gio hang.

**Auth:** Bearer token — any authenticated user

**Query params:** `paymentMethod` (same as above)

**Request body:**
```json
[1, 2, 3]
```
*(Danh sach cart item ID)*

**Response `result`:** Order object

---

### GET `/api/v1/bookings`

Lay lich su don hang cua nguoi dung dang dang nhap.

**Auth:** Bearer token — any authenticated user

**Query params:** `page` (default 1), `size` (default 10)

**Response `result`:** Page of order objects

---

## 8. Checkin Service — port 8085

Base path: `/api/v1/checkin`

### POST `/api/v1/checkin/scan`

Quet ma QR de check-in. Ket qua luon duoc luu vao log du thanh cong hay that bai.

**Auth:** Bearer token — ADMIN hoac ORGANIZER

**Request body:**
```json
{
  "ticketCode": "TKT-A1B2C3D4",
  "eventId": 10
}
```

**Response `result`:**
```json
{
  "id": 1,
  "ticketCode": "TKT-A1B2C3D4",
  "eventId": 10,
  "scannedBy": 42,
  "result": "SUCCESS",
  "message": "Check-in successful",
  "scannedAt": "2025-01-15T14:30:00"
}
```

**Gia tri `result`:**

| Value | Mo ta |
|-------|-------|
| `SUCCESS` | Check-in thanh cong |
| `ALREADY_USED` | Ve da duoc su dung truoc do |
| `INVALID` | Ve khong hop le |
| `NOT_FOUND` | Khong tim thay ma ve |

---

### GET `/api/v1/checkin/event/{eventId}`

Lay toan bo lich su quet ve cua mot su kien, sap xep moi nhat truoc.

**Auth:** Bearer token — ADMIN hoac ORGANIZER

**Path params:** `eventId` (Long)

**Response `result`:** List of checkin log objects

---

## 9. Admin Service — Statistics — port 8086

Base path: `/api/v1/admin/statistics`

> Tat ca endpoint yeu cau role **ADMIN**.

### GET `/api/v1/admin/statistics/by-status/{quarter}/{year}`

Phan bo trang thai su kien theo quy.

**Auth:** Bearer token — ADMIN

**Path params:**

| Param | Type | Description |
|-------|------|-------------|
| `quarter` | Long | Quy (1–4) |
| `year` | Long | Nam (vd: 2025) |

**Response `result`:**
```json
{
  "quarter": 1,
  "year": 2025,
  "total": 20,
  "eventStatusStatsDetail": [
    { "status": "UPCOMING", "percentage": 50.0, "countEvents": 10 },
    { "status": "OPENING",  "percentage": 25.0, "countEvents": 5  },
    { "status": "COMPLETED","percentage": 25.0, "countEvents": 5  }
  ]
}
```

---

### GET `/api/v1/admin/statistics/by-temporal/{dayOfWeek}`

Thong ke so luong su kien bat dau theo gio trong ngay, loc theo thu trong tuan.

**Auth:** Bearer token — ADMIN

**Path params:**

| Value | Day |
|-------|-----|
| 1 | Sunday |
| 2 | Monday |
| 3 | Tuesday |
| 4 | Wednesday |
| 5 | Thursday |
| 6 | Friday |
| 7 | Saturday |

**Response `result`:**
```json
{
  "day": "Monday",
  "eventTemporalStatsDetail": [
    {
      "hourOfDay": 9,
      "countEvents": 5,
      "totalTickets": 500,
      "ticketsSold": 420,
      "percentageOfTicketsSold": 84.0
    }
  ]
}
```

---

### GET `/api/v1/admin/statistics/revenue/organizer/{organizerId}`

Thong ke doanh thu tung su kien cua mot organizer.

**Auth:** Bearer token — ADMIN

**Path params:** `organizerId` (Long)

**Response `result`:**
```json
[
  {
    "eventName": "Music Show 2025",
    "totalRevenue": 5000000,
    "ticketsSold": 80,
    "percentageOfTicketsSold": 80.0
  }
]
```

---

### GET `/api/v1/admin/statistics/revenue/admin`

Thong ke tong doanh thu nen tang (service fee) va phan bo theo thang (6 thang gan nhat).

**Auth:** Bearer token — ADMIN

**Response `result`:**
```json
{
  "totalRevenue": 12500000,
  "monthlyRevenues": [
    { "year": 2024, "month": 8,  "revenue": 1000000 },
    { "year": 2024, "month": 9,  "revenue": 2500000 },
    { "year": 2024, "month": 10, "revenue": 3000000 },
    { "year": 2024, "month": 11, "revenue": 2000000 },
    { "year": 2024, "month": 12, "revenue": 2500000 },
    { "year": 2025, "month": 1,  "revenue": 1500000 }
  ]
}
```

---

## 10. Internal APIs (service-to-service)

Cac endpoint nay khong di qua gateway, chi duoc goi noi bo giua cac service qua Feign client. Khong expose ra ngoai.

### ticket-service

| Method | Path | Caller | Mo ta |
|--------|------|--------|-------|
| `POST` | `/api/v1/ticket-types/internal/reserve` | booking-service | Tru ton kho truoc khi tao order |
| `POST` | `/api/v1/ticket-types/internal/release` | booking-service | Hoan tra ton kho khi payment that bai |
| `POST` | `/api/v1/tickets/internal/issue` | booking-service | Phat hanh ve sau khi thanh toan thanh cong |

**Reserve / Release body:**
```json
{ "ticketTypeId": 3, "quantity": 2 }
```

**Issue body:**
```json
{
  "orderId": 99,
  "customerId": 1,
  "ticketTypeId": 3,
  "quantity": 2
}
```

---

## Error Response Format

```json
{
  "code": 5002,
  "message": "Not enough tickets available",
  "result": null
}
```

**Common error codes:**

| Code | HTTP | Mo ta |
|------|------|-------|
| 1000 | 200 | Thanh cong |
| 2001 | 404 | Category not found |
| 3001 | 404 | Event not found |
| 3006 | 403 | Khong phai chu su kien |
| 5001 | 404 | Ticket type not found |
| 5002 | 409 | Khong du ve |
| 5004 | 400 | Ticket khong hop le |
| 5005 | 409 | Ticket da duoc su dung |
| 6001 | 400 | Gio hang trong |
| 6004 | 409 | Khong du ve (booking) |
| 4007 | 403 | Khong co quyen |
| 9999 | 500 | Loi he thong |
