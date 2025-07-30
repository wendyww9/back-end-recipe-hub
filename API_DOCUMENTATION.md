# RecipeHub API Documentation

## Base URL
```
local test: http://localhost:8080/api
Deployment: http://recipehub-dev-env.eba-6mi9w35s.us-east-2.elasticbeanstalk.com/api
```

## Authentication Endpoints (`/api/auth`)

### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**Response Body (201 Created):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "password": "string",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "User already exists"
}
```

---

### 2. Login User
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```
*Note: Either username or email can be used*

**Response Body (200 OK):**
```json
"Login successful"
```

**Error Response (401 Unauthorized):**
```json
"Invalid credentials"
```

---

### 3. Logout User
**POST** `/api/auth/logout`

**Request Body:** None

**Response Body (200 OK):**
```json
{
  "message": "Logout successful"
}
```

---

## User Management Endpoints (`/api/users`)

### 4. Get Current User
**GET** `/api/users/me`

**Request Body:** None

**Response Body (200 OK):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```
*Note: Password is excluded from response*

**Error Response (401 Unauthorized):**
```json
{}
```

---

### 5. Get User by ID
**GET** `/api/users/{id}`

**Request Body:** None

**Response Body (200 OK):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```

**Error Response (404 Not Found):**
```json
{}
```

---

### 6. Get All Users
**GET** `/api/users`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "username": "string",
    "email": "string",
    "createdAt": "2025-07-29T21:51:22.106186"
  },
  {
    "id": 2,
    "username": "string2",
    "email": "string2@example.com",
    "createdAt": "2025-07-29T21:52:15.123456"
  }
]
```

---

### 7. Update User Password
**PUT** `/api/users/{userId}/password`

**Request Body:**
```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```

**Response Body (200 OK):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```

**Error Response (400 Bad Request):**
```json
"Current password is incorrect"
```

---

### 8. Update User Email
**PUT** `/api/users/{userId}/email`

**Request Body:**
```json
{
  "currentPassword": "string",
  "newEmail": "string"
}
```

**Response Body (200 OK):**
```json
{
  "id": 1,
  "username": "string",
  "email": "newemail@example.com",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```

**Error Response (400 Bad Request):**
```json
"Current password is incorrect"
```

---

### 9. Delete User
**DELETE** `/api/users/{id}`

**Request Body:** None

**Response Body (204 No Content):**
```json
{}
```

---

### 10. Get User Recipes
**GET** `/api/users/{userId}/recipes`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "title": "string",
    "description": "string",
    "ingredients": [
      {
        "name": "string",
        "unit": "string",
        "quantity": 1.0
      }
    ],
    "instructions": ["string"],
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "string",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:51:22.106186",
    "updatedAt": "2025-07-29T21:51:22.108822"
  }
]
```

**Error Response (500 Internal Server Error):**
```json
{}
```

---

## Recipe Management Endpoints (`/api/recipes`)

### 11. Create Recipe
**POST** `/api/recipes`

**Request Body:**
```json
{
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "authorId": 1,
  "originalRecipeId": null
}
```

**Response Body (200 OK):**
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 0,
  "authorId": 1,
  "authorUsername": "string",
  "originalRecipeId": null,
  "createdAt": "2025-07-29T21:51:22.106186",
  "updatedAt": "2025-07-29T21:51:22.108822"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Author not found"
}
```

---

### 12. Get All Recipes (Public Only)
**GET** `/api/recipes`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "title": "string",
    "description": "string",
    "ingredients": [
      {
        "name": "string",
        "unit": "string",
        "quantity": 1.0
      }
    ],
    "instructions": ["string"],
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "string",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:51:22.106186",
    "updatedAt": "2025-07-29T21:51:22.108822"
  }
]
```

---

### 13. Get All Public Recipes
**GET** `/api/recipes/public`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "title": "string",
    "description": "string",
    "ingredients": [
      {
        "name": "string",
        "unit": "string",
        "quantity": 1.0
      }
    ],
    "instructions": ["string"],
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "string",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:51:22.106186",
    "updatedAt": "2025-07-29T21:51:22.108822"
  }
]
```

---

### 14. Get Recipe by ID
**GET** `/api/recipes/{id}`

**Request Body:** None

**Response Body (200 OK):**
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 0,
  "authorId": 1,
  "authorUsername": "string",
  "originalRecipeId": null,
  "createdAt": "2025-07-29T21:51:22.106186",
  "updatedAt": "2025-07-29T21:51:22.108822"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Recipe not found"
}
```

---

### 15. Update Recipe
**PUT** `/api/recipes/{id}`

**Request Body (Partial Update Supported):**
```json
{
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "authorId": 1
}
```
*Note: All fields are optional for partial updates*

**Response Body (200 OK):**
```json
{
  "id": 1,
  "title": "Updated Title",
  "description": "Updated description",
  "ingredients": [
    {
      "name": "Updated Ingredient",
      "unit": "cups",
      "quantity": 2.0
    }
  ],
  "instructions": ["Updated step 1", "Updated step 2"],
  "isPublic": false,
  "cooked": true,
  "favourite": true,
  "likeCount": 0,
  "authorId": 1,
  "authorUsername": "string",
  "originalRecipeId": null,
  "createdAt": "2025-07-29T21:51:22.106186",
  "updatedAt": "2025-07-29T21:51:29.499094"
}
```

**Error Responses:**
- **403 Forbidden:** Unauthorized (not the recipe owner)
- **404 Not Found:** Recipe doesn't exist
- **400 Bad Request:** Invalid request data

---

### 16. Update Recipe Like Count
**PUT** `/api/recipes/{id}/likecount`

**Request Parameters:**
- `likeCount` (query parameter): The new like count value (integer)

**Request Body:** None

**Example Request:**
```
PUT /api/recipes/1/likecount?likeCount=15
```

**Response Body (200 OK):**
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 15,
  "authorId": 1,
  "authorUsername": "string",
  "originalRecipeId": null,
  "createdAt": "2025-07-29T21:51:22.106186",
  "updatedAt": "2025-07-29T21:51:22.108822"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Recipe not found"
}
```

**Notes:**
- No authorization required - anyone can update like count
- Frontend-driven: Frontend sends the final like count value
- Does not update the recipe's `updatedAt` timestamp
- Simple count update without affecting recipe content

---

## Data Transfer Objects (DTOs)

### UserDTO
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

### RecipeRequestDTO
```json
{
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "authorId": 1,
  "originalRecipeId": null
}
```

### RecipeResponseDTO
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "ingredients": [
    {
      "name": "string",
      "unit": "string",
      "quantity": 1.0
    }
  ],
  "instructions": ["string"],
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 0,
  "authorId": 1,
  "authorUsername": "string",
  "originalRecipeId": null,
  "createdAt": "2025-07-29T21:51:22.106186",
  "updatedAt": "2025-07-29T21:51:22.108822"
}
```

### IngredientDTO
```json
{
  "name": "string",
  "unit": "string",
  "quantity": 1.0
}
```

## HTTP Status Codes

- **200 OK:** Request successful
- **201 Created:** Resource created successfully
- **204 No Content:** Request successful, no content to return
- **400 Bad Request:** Invalid request data
- **401 Unauthorized:** Authentication required
- **403 Forbidden:** Access denied
- **404 Not Found:** Resource not found
- **500 Internal Server Error:** Server error

## Notes

1. **Authentication:** Currently using HTTP Basic Authentication
2. **CORS:** Enabled for all origins (`*`)
3. **Database:** Uses H2 in-memory database for development, PostgreSQL for production
4. **Partial Updates:** Recipe update endpoint supports partial updates (only send fields you want to change)
5. **Privacy:** Recipe endpoints respect privacy settings (public/private recipes)
6. **Authorization:** Recipe updates require ownership verification
7. **Timestamps:** All recipes include `createdAt` and `updatedAt` timestamps
8. **Like Count:** Separate endpoint for updating like count without authorization requirements 