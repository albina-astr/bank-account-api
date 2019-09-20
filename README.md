Bank account RESTful API
Provides methods to work with bank account: CRUD operations, transfer money between 2 accounts

Stack:
- Java 8
- JUnit 5
- Jersey 1.8
- No authorization
- No DB, all data runs in-memory


METHODS DESCRIPTION
------------------------
Resource path: /accounts
------------------------

>1. POST /create/{owner}
- Creates new account
- Response Content Type: JSON
- Status 200

>2. GET

- Returns all accounts
- Response Content Type: JSON
- Status 200

>3. GET /{accountNumber}

- Finds account by its number
- Response Content Type: JSON
- Status 200

>4. PUT /update

- Updates existing account
- Parameter type: body
- Parameter content type: JSON

- Parameter model schema:
`{
    "number": 0,
    "owner": "string",
    "balance": 0,
    "disabled": "false"
}`

- Response Content Type: JSON
- Status 200

>5. POST /top_up

- Adds money to account by its number
- Parameter type: body
- Parameter content type: JSON
- Parameter model schema:
`{
    "accountNumber": 0,
    "amount": 0
}`
- Status 204

>6. DELETE /delete/{accountNumber}

- Deletes account by its number
- Status 204

>7. POST /transfer

- Transfers money between 2 accounts
- Parameter type: body
- Parameter content type: JSON
- Parameter model schema:
`{
    "accountNumberFrom": 0,
    "accountNumberTo": 0,
    "amount": 0
}`
- Status 204
