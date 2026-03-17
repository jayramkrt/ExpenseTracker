# Personal Finance Analyzer - REST API Documentation

## Base URL
```
http://localhost:8080/api/v1
```

## Authentication
All endpoints (except auth) require the `Authorization` header with the user ID:
```
Authorization: {user-uuid}
```

---

## 📤 STATEMENTS CONTROLLER
**Base Path:** `/api/v1/statements`

### 1. Upload Bank Statement PDF
```
POST /api/v1/statements/upload
Content-Type: multipart/form-data

Parameters:
- file (multipart): PDF file to upload
- bankName (optional): Name of the bank
- accountType (optional): checking, savings, credit_card
- Authorization: {user-uuid}

Response: 201 Created
{
  "id": "uuid",
  "filename": "statement.pdf",
  "bankName": "Chase",
  "accountType": "checking",
  "processingStatus": "pending",
  "transactionCount": 0,
  "uploadedAt": "2024-01-15T10:30:00"
}
```

### 2. Get User Statements (Paginated)
```
GET /api/v1/statements?page=0&size=10
Authorization: {user-uuid}

Response: 200 OK
[
  {
    "id": "uuid",
    "filename": "statement.pdf",
    "processingStatus": "completed",
    "transactionCount": 45,
    ...
  }
]
```

### 3. Get Specific Statement
```
GET /api/v1/statements/{statementId}
Authorization: {user-uuid}

Response: 200 OK
{
  "id": "uuid",
  "filename": "statement.pdf",
  "bankName": "Chase",
  "processingStatus": "completed",
  ...
}
```

### 4. Get Statement Processing Status
```
GET /api/v1/statements/{statementId}/status
Authorization: {user-uuid}

Response: 200 OK
{
  "statementId": "uuid",
  "status": "completed",
  "transactionCount": 45,
  "errorMessage": null
}
```

### 5. Retry Failed Statement Processing
```
POST /api/v1/statements/{statementId}/retry
Authorization: {user-uuid}

Response: 200 OK
{
  "id": "uuid",
  "processingStatus": "processing"
}
```

### 6. Delete Statement
```
DELETE /api/v1/statements/{statementId}
Authorization: {user-uuid}

Response: 204 No Content
```

---

## 💳 TRANSACTIONS CONTROLLER
**Base Path:** `/api/v1/transactions`

### 1. Get Transactions (Advanced Filtering)
```
GET /api/v1/transactions?page=0&size=20&categoryId=uuid&startDate=2024-01-01&endDate=2024-01-31&merchantName=Amazon&isManual=false
Authorization: {user-uuid}

Query Parameters:
- page: Page number (default: 0)
- size: Page size (default: 20)
- categoryId: Filter by category UUID
- startDate: ISO date format (2024-01-01)
- endDate: ISO date format
- merchantName: Search by merchant
- isManual: true or false

Response: 200 OK
{
  "content": [
    {
      "id": "uuid",
      "transactionDate": "2024-01-15",
      "amount": "-45.99",
      "merchantName": "Amazon Prime",
      "categoryName": "Subscriptions",
      "categoryId": "uuid",
      "confidence_score": 0.95,
      ...
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 350,
  "totalPages": 18
}
```

### 2. Get Specific Transaction
```
GET /api/v1/transactions/{transactionId}
Authorization: {user-uuid}

Response: 200 OK
{
  "id": "uuid",
  "transactionDate": "2024-01-15",
  "amount": "-45.99",
  ...
}
```

### 3. Update Transaction (Reclassify or Add Notes)
```
PUT /api/v1/transactions/{transactionId}
Authorization: {user-uuid}
Content-Type: application/json

Request Body:
{
  "categoryId": "uuid",
  "categoryName": "Entertainment",
  "notes": "Netflix subscription"
}

Response: 200 OK
{
  "id": "uuid",
  "categoryId": "uuid",
  "categoryName": "Entertainment",
  "notes": "Netflix subscription",
  ...
}
```

### 4. Get Transactions by Category
```
GET /api/v1/transactions/category/{categoryId}?startDate=2024-01-01&endDate=2024-01-31
Authorization: {user-uuid}

Response: 200 OK
[
  { "id": "uuid", "amount": "-50.00", ... },
  { "id": "uuid", "amount": "-75.00", ... }
]
```

### 5. Search Transactions
```
GET /api/v1/transactions/search?query=amazon&page=0&size=10
Authorization: {user-uuid}

Response: 200 OK
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 15
}
```

### 6. Delete Transaction
```
DELETE /api/v1/transactions/{transactionId}
Authorization: {user-uuid}

Response: 204 No Content
```

---

## ✏️ MANUAL ENTRIES CONTROLLER
**Base Path:** `/api/v1/manual-entries`

### 1. Create Manual Entry
```
POST /api/v1/manual-entries
Authorization: {user-uuid}
Content-Type: application/json

Request Body:
{
  "transactionDate": "2024-01-15",
  "amount": -50.00,
  "description": "Lunch with client",
  "categoryId": "uuid",
  "transactionType": "expense",
  "notes": "Business expense"
}

Response: 201 Created
{
  "id": "uuid",
  "transactionDate": "2024-01-15",
  "amount": -50.00,
  "categoryName": "Dining & Restaurants",
  ...
}
```

### 2. Get Manual Entries
```
GET /api/v1/manual-entries?page=0&size=20&categoryId=uuid&startDate=2024-01-01&endDate=2024-01-31
Authorization: {user-uuid}

Response: 200 OK
[
  { "id": "uuid", "description": "Lunch", "amount": -50.00, ... }
]
```

### 3. Get Specific Manual Entry
```
GET /api/v1/manual-entries/{entryId}
Authorization: {user-uuid}

Response: 200 OK
{
  "id": "uuid",
  "transactionDate": "2024-01-15",
  ...
}
```

### 4. Update Manual Entry
```
PUT /api/v1/manual-entries/{entryId}
Authorization: {user-uuid}
Content-Type: application/json

Request Body:
{
  "transactionDate": "2024-01-15",
  "amount": -75.00,
  "description": "Updated description",
  "categoryId": "uuid"
}

Response: 200 OK
{
  "id": "uuid",
  ...
}
```

### 5. Delete Manual Entry
```
DELETE /api/v1/manual-entries/{entryId}
Authorization: {user-uuid}

Response: 204 No Content
```

---

## 📊 ANALYTICS CONTROLLER
**Base Path:** `/api/v1/analytics`

### 1. Get Analytics Overview
```
GET /api/v1/analytics/overview?startDate=2024-01-01&endDate=2024-01-31
Authorization: {user-uuid}

Response: 200 OK
{
  "totalIncome": 5000.00,
  "totalExpenses": -1500.00,
  "netBalance": 3500.00,
  "averageMonthlyIncome": 5000.00,
  "averageMonthlyExpenses": -1500.00,
  "transactionCount": 45,
  "manualEntryCount": 5,
  "monthlyTrends": [ ... ],
  "categoryBreakdown": [ ... ]
}
```

### 2. Get Category Breakdown
```
GET /api/v1/analytics/category-breakdown?startDate=2024-01-01&endDate=2024-01-31
Authorization: {user-uuid}

Response: 200 OK
[
  {
    "categoryId": "uuid",
    "categoryName": "Groceries",
    "icon": "🛒",
    "totalAmount": -450.00,
    "percentage": 30.0,
    "transactionCount": 12,
    "averagePerTransaction": -37.50
  },
  {
    "categoryId": "uuid",
    "categoryName": "Utilities",
    "icon": "💡",
    "totalAmount": -200.00,
    "percentage": 13.3,
    "transactionCount": 4,
    "averagePerTransaction": -50.00
  }
]
```

### 3. Get Monthly Trends
```
GET /api/v1/analytics/monthly-trends?months=12
Authorization: {user-uuid}

Response: 200 OK
[
  {
    "month": "2024-01",
    "income": 5000.00,
    "expenses": -1500.00,
    "netBalance": 3500.00,
    "transactionCount": 45
  },
  {
    "month": "2024-02",
    "income": 5000.00,
    "expenses": -1400.00,
    "netBalance": 3600.00,
    "transactionCount": 42
  }
]
```

### 4. Get Top Merchants
```
GET /api/v1/analytics/top-merchants?limit=10&startDate=2024-01-01&endDate=2024-01-31
Authorization: {user-uuid}

Response: 200 OK
[
  {
    "merchantName": "Amazon",
    "totalSpending": -250.00,
    "transactionCount": 8,
    "averagePerTransaction": -31.25
  },
  {
    "merchantName": "Whole Foods",
    "totalSpending": -180.00,
    "transactionCount": 6,
    "averagePerTransaction": -30.00
  }
]
```

### 5. Get Recurring Transactions
```
GET /api/v1/analytics/recurring-transactions
Authorization: {user-uuid}

Response: 200 OK
[
  {
    "merchantName": "Netflix",
    "amount": -15.99,
    "frequency": "monthly",
    "lastOccurrence": "2024-01-15",
    "occurrenceCount": 12
  },
  {
    "merchantName": "Gym",
    "amount": -50.00,
    "frequency": "monthly",
    "lastOccurrence": "2024-01-01",
    "occurrenceCount": 6
  }
]
```

### 6. Detect Spending Anomalies
```
GET /api/v1/analytics/anomalies?threshold=2.0
Authorization: {user-uuid}

Response: 200 OK
[
  {
    "transactionId": "uuid",
    "merchantName": "Best Buy",
    "amount": -1500.00,
    "categoryName": "Shopping",
    "date": "2024-01-15",
    "deviation": 5.2,
    "reason": "Amount is 5.2x above category average"
  }
]
```

### 7. Export Analytics
```
GET /api/v1/analytics/export?startDate=2024-01-01&endDate=2024-01-31
Authorization: {user-uuid}

Response: 200 OK
{
  "summary": { ... },
  "transactions": [ ... ],
  "manualEntries": [ ... ],
  "categoryBreakdown": [ ... ],
  "monthlyTrends": [ ... ],
  "topMerchants": [ ... ],
  "recurringTransactions": [ ... ]
}
```

---

## 🏷️ CATEGORIES CONTROLLER
**Base Path:** `/api/v1/categories`

### 1. Get All Categories
```
GET /api/v1/categories

Response: 200 OK
[
  {
    "id": "uuid",
    "name": "Groceries",
    "description": "Food & grocery shopping",
    "icon": "🛒",
    "color": "#2ECC71",
    "isSystem": true
  },
  ...
]
```

### 2. Get Specific Category
```
GET /api/v1/categories/{categoryId}

Response: 200 OK
{
  "id": "uuid",
  "name": "Groceries",
  ...
}
```

### 3. Get System Categories
```
GET /api/v1/categories/system

Response: 200 OK
[
  { "id": "uuid", "name": "Groceries", ... },
  { "id": "uuid", "name": "Utilities", ... },
  ...
]
```

---

## ❌ Error Responses

### 400 Bad Request - Validation Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "message": "Validation failed",
  "error": "VALIDATION_ERROR",
  "fieldErrors": {
    "amount": "Amount must be valid",
    "categoryId": "Category ID cannot be null"
  }
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "message": "User not authenticated or invalid token",
  "error": "UNAUTHORIZED"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "message": "Transaction with id: uuid not found",
  "error": "NOT_FOUND"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 500,
  "message": "Internal server error",
  "error": "INTERNAL_ERROR"
}
```

---

## 📝 Key Features

✅ **Pagination**: All list endpoints support `page` and `size` parameters  
✅ **Filtering**: Advanced filters by date range, category, merchant, etc.  
✅ **Soft Deletes**: Records are logically deleted, not hard-deleted  
✅ **Timestamps**: All resources include `createdAt` and `updatedAt`  
✅ **Validation**: Request bodies are validated with detailed error messages  
✅ **Authorization**: User-scoped data access via Authorization header  
✅ **Async Processing**: PDF extraction and LLM classification run asynchronously

---

## 🔄 Common Workflows

### Workflow 1: Upload Statement → Monitor Progress → View Transactions
```
1. POST /api/v1/statements/upload → Returns statement with status "pending"
2. Poll GET /api/v1/statements/{statementId}/status → Waits for "completed"
3. GET /api/v1/transactions?page=0 → View extracted transactions
4. PUT /api/v1/transactions/{id} → Reclassify if needed
```

### Workflow 2: Add Manual Entry → View in Analytics
```
1. POST /api/v1/manual-entries → Create expense
2. GET /api/v1/analytics/overview → See updated totals
3. GET /api/v1/analytics/category-breakdown → See category impact
```

### Workflow 3: Analyze Spending
```
1. GET /api/v1/analytics/monthly-trends → See spending trends
2. GET /api/v1/analytics/category-breakdown → See category breakdown
3. GET /api/v1/analytics/top-merchants → See where most money goes
4. GET /api/v1/analytics/recurring-transactions → Identify subscriptions
5. GET /api/v1/analytics/anomalies → Find unusual spending
```

---

## 🛠️ Development Notes

- All endpoints are protected by Spring Security (requires Authorization header)
- PDF files are stored in `./data/statements/` directory
- LLM classification happens asynchronously in a processing queue
- Analytics are cached for performance (expires after 1 hour by default)
- UUIDs are used for all IDs (better than auto-increment)

---

**Next Steps:**
1. Implement Spring Data JPA Repositories
2. Implement Service Layer (business logic)
3. Integrate Ollama/LLM for classification
4. Build React Frontend
5. Deploy to production