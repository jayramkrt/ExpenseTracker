# Personal Finance Analyzer - Architecture
## Local LLM + Spring Boot + React Stack

---

##  System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      REACT FRONTEND                          │
│  (Dashboard, Upload, Manual Entry, Analytics Visualization) │
└────────────────────────┬────────────────────────────────────┘
                         │ (REST API / JSON)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   SPRING BOOT BACKEND                        │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ File Processing  │  │ Transaction  │  │ Analytics    │  │
│  │ (PDF/CSV Parser) │  │ Controller   │  │ Service      │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ LLM Integration  │  │ Category     │  │ Auth Service │  │
│  │ (Ollama/LM Stud) │  │ Service      │  │              │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘  │
└────────────────────────┬────────────────────────────────────┘
                         │ (JDBC / Queries)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              DATABASE (PostgreSQL/MySQL)                     │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Users            │  │ Transactions │  │ Categories   │  │
│  │ (Encrypted creds)│  │ (Raw + Class)│  │ (Predefined) │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Statements       │  │ Manual       │  │ Analytics    │  │
│  │ (Metadata)       │  │ Entries      │  │ (Cache)      │  │
│  └──────────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
                         │ (Network)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│         LOCAL LLM (Ollama / LM Studio / Similar)            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Model: Mistral / Llama2 / Neural Chat (or your pick) │  │
│  │ Task: Extract & Classify transactions from PDFs/Text │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

##  Data Flow - PDF Upload & Classification

### Step 1: Frontend Upload
```
User selects PDF → React sends to /api/statements/upload
```

### Step 2: Backend Processing
```
POST /api/statements/upload
├─ Validate file (PDF only, size limits)
├─ Store file locally (e.g., /data/statements/{user_id}/{timestamp}.pdf)
├─ Create bank_statements record (status = 'processing')
├─ Queue async job: extractAndClassifyTransactions()
└─ Return: { statement_id, status: 'processing' }
```

### Step 3: Extract Text from PDF
```
Java Service: PDFParsingService
├─ Use library: Apache PDFBox or iText
├─ Extract all text lines
├─ Identify table structure (dates, amounts, descriptions)
├─ Output: List<RawTransaction>
  {
    date: "2024-01-15",
    amount: "-45.99",
    description: "AMAZON PRIME MEMBERSHIP",
    ...
  }
```

### Step 4: LLM Classification
```
Java Service: LLMClassificationService
├─ For each RawTransaction:
│  ├─ Build prompt with transaction data
│  ├─ Call local LLM (HTTP to Ollama:11434)
│  ├─ Parse response: { category, confidence, notes }
│  └─ Store in DB with confidence score
└─ Update statement status = 'completed'

Example Prompt:

Classify the following transaction into ONE of these categories:
Groceries, Utilities, Entertainment, Transportation, Healthcare, Salary/Income, Subscriptions, Shopping, Transfer, Other

Transaction:
Date: 2024-01-15
Amount: -45.99
Description: AMAZON PRIME MEMBERSHIP

Respond in JSON format only:
{
  "category": "Subscriptions",
  "confidence": 0.95,
  "reasoning": "Amazon Prime is a subscription service"
}
```

### Step 5: Store & Notify
```
✓ Save transactions to DB
✓ Update bank_statements status = 'completed'
✓ Return success to frontend (WebSocket or polling)
✓ Frontend refreshes dashboard with new data
```

---

## Privacy & Security

### Local-First Design
- Bank statements stored **locally only** (no cloud upload)
- LLM runs on your machine (no external API calls)
- Data never leaves your network

### Additional Measures
1. **Encryption at Rest**: Encrypt PDF files and sensitive DB fields
2. **Password Hashing**: Use bcrypt for user passwords
3. **File Cleanup**: Auto-delete PDFs after processing (optional)
4. **DB Encryption**: Use column-level encryption for amounts/descriptions
5. **No Logging**: Avoid logging sensitive transaction details

---

## Analytics Dashboard Features

### What to Display
1. **Monthly Overview**
   - Total income vs. expenses
   - Net balance
   - Trend line (last 6 months)

2. **Category Breakdown**
   - Pie/Donut chart: % of spending by category
   - Bar chart: Top categories

3. **Recurring Patterns**
   - Subscriptions detected
   - Monthly averages per category
   - Anomalies (unusual transactions)

4. **Filters & Drill-Down**
   - By date range
   - By category
   - By account/statement
   - Search transactions

5. **Export**
   - PDF report
   - CSV export

---

## Class Diagram

```
Controller (HTTP)
    ↓ delegates
Service (Business Logic)
    ├─ Repository (Data Access)
    ├─ Mapper (Entity ↔ DTO)
    ├─ Validator (Rules)
    └─ Other Services (Cross-cutting)
    ↓ uses
Entity (Domain Model)
    ↓ persisted by
Repository (JPA)
    ↓ reads/writes
Database
```

---
