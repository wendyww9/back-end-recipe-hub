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

### 14. Search Recipes by Title
**GET** `/api/recipes/search`

**Request Parameters:**
- `title` (query parameter): The search term to find in recipe titles (string)

**Request Body:** None

**Example Request:**
```
GET /api/recipes/search?title=chocolate
```

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Chocolate Cake",
    "description": "A delicious chocolate cake recipe",
    "ingredients": [
      {
        "name": "Chocolate",
        "unit": "cups",
        "quantity": 2.0
      }
    ],
    "instructions": ["Mix chocolate", "Bake at 350F"],
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "string",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:51:22.106186",
    "updatedAt": "2025-07-29T21:51:22.108822"
  },
  {
    "id": 2,
    "title": "Hot Chocolate",
    "description": "Warm chocolate drink",
    "ingredients": [
      {
        "name": "Cocoa Powder",
        "unit": "tbsp",
        "quantity": 2.0
      }
    ],
    "instructions": ["Mix cocoa with hot milk"],
    "isPublic": false,
    "cooked": true,
    "favourite": true,
    "likeCount": 5,
    "authorId": 2,
    "authorUsername": "string2",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:52:15.123456",
    "updatedAt": "2025-07-29T21:52:15.123456"
  }
]
```

**Empty Response (200 OK):**
```json
[]
```

**Key Features:**
- **Case-Insensitive:** Search is not case-sensitive (e.g., "chocolate" matches "Chocolate")
- **Partial Matching:** Uses `LIKE` query with wildcards (e.g., "choc" matches "Chocolate")
- **All Recipes:** Returns both public and private recipes that match the search term
- **Empty Results:** Returns empty array `[]` when no matches are found

---

### 15. Get Recipe by ID
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

### 16. Update Recipe
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

### 17. Update Recipe Like Count
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

### 18. Fork Recipe
**POST** `/api/recipes/{id}/fork`

**Description:** Creates a copy of an existing recipe with optional modifications. Similar to GitHub's fork functionality.

**Request Body (Optional - for modifications):**
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
  "favourite": false
}
```
*Note: All fields are optional. If not provided, the forked recipe will be an exact copy of the original.*

**Example Requests:**

**Fork without modifications:**
```
POST /api/recipes/1/fork
Content-Type: application/json

{}
```

**Fork with modifications:**
```
POST /api/recipes/1/fork
Content-Type: application/json

{
  "title": "My Modified Version",
  "description": "A personalized version of the original recipe",
  "ingredients": [
    {
      "name": "Flour",
      "unit": "cups",
      "quantity": 3.0
    },
    {
      "name": "Sugar",
      "unit": "cups", 
      "quantity": 1.5
    },
    {
      "name": "Vanilla",
      "unit": "tsp",
      "quantity": 1.0
    }
  ],
  "instructions": [
    "Mix flour and sugar",
    "Add vanilla extract",
    "Bake at 375F for 25 minutes"
  ],
  "isPublic": false,
  "cooked": true,
  "favourite": true
}
```

**Response Body (200 OK):**
```json
{
  "id": 2,
  "title": "My Modified Version",
  "description": "A personalized version of the original recipe",
  "ingredients": [
    {
      "name": "Flour",
      "unit": "cups",
      "quantity": 3.0
    },
    {
      "name": "Sugar",
      "unit": "cups",
      "quantity": 1.5
    },
    {
      "name": "Vanilla",
      "unit": "tsp",
      "quantity": 1.0
    }
  ],
  "instructions": [
    "Mix flour and sugar",
    "Add vanilla extract", 
    "Bake at 375F for 25 minutes"
  ],
  "isPublic": false,
  "cooked": true,
  "favourite": true,
  "likeCount": 0,
  "authorId": 1,
  "authorUsername": "testuser",
  "originalRecipeId": 1,
  "createdAt": "2025-07-30T09:13:23.133444",
  "updatedAt": "2025-07-30T09:13:23.136118"
}
```

**Error Responses:**
- **400 Bad Request:** Recipe to fork not found, User not found, or invalid request data
- **404 Not Found:** Recipe with specified ID doesn't exist

**Key Features:**
- **Content Copying:** Copies all content from the recipe being forked (like GitHub)
- **Optional Modifications:** Can apply changes during forking process
- **Original Recipe Tracking:** `originalRecipeId` always points to the root original recipe
- **Like Count Reset:** New forks start with `likeCount: 0`
- **Author Assignment:** Forked recipe is assigned to the user performing the fork
- **Fork Chain Support:** Can fork any recipe in a chain, always pointing back to the original

**Fork Chain Example:**
```
Original Recipe (ID: 1) ← originalRecipeId: null
├── Fork 1 (ID: 2) ← originalRecipeId: 1
├── Modified Fork (ID: 3) ← originalRecipeId: 1
└── Fork of Fork (ID: 4) ← originalRecipeId: 1
```

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
9. **Recipe Forking:** Fork functionality allows creating copies of recipes with optional modifications, similar to GitHub's fork feature 