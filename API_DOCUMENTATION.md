# RecipeHub API Documentation

## Base URL
```
local test: http://localhost:8080/api
Deployment: http://recipehub-dev-env.eba-6mi9w35s.us-east-2.elasticbeanstalk.com/api
```

## Setup and Running

### Prerequisites
- Java 17 or higher
- Maven (or use the included Maven wrapper)
- PostgreSQL (for development mode)

### Database Configuration

#### Default Mode (H2 In-Memory Database)
When no profile is specified, the application uses H2 in-memory database:
```bash
./mvnw spring-boot:run
```
- **Use Case**: Quick testing, CI/CD, demo purposes
- **Data Persistence**: Data is lost when application stops
- **Configuration**: No external database setup required

#### Development Mode (PostgreSQL)
For development with persistent data storage:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
- **Use Case**: Development, testing with persistent data
- **Data Persistence**: Data persists between application restarts
- **Configuration**: Requires PostgreSQL running on localhost:5432

### Database Setup for Development

1. **Install PostgreSQL** (if not already installed)
2. **Create Database**:
   ```sql
   CREATE DATABASE recipehub_development;
   ```
3. **Update Configuration** (if needed):
   - Edit `src/main/resources/application-dev.properties`
   - Update database credentials if different from defaults

### Running the Application

#### Quick Start (H2 Database)
```bash
# Clone the repository
git clone <repository-url>
cd back-end-recipe-hub

# Run with default H2 database
./mvnw spring-boot:run
```

#### Development Mode (PostgreSQL)
```bash
# Ensure PostgreSQL is running
# Create database if needed
createdb recipehub_development

# Run with PostgreSQL
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Verification
Once running, test the API:
```bash
# Test health endpoint
curl http://localhost:8080/api/users

# Should return empty array or existing users
```

### Configuration Files

The application uses different configuration files based on the active profile:

- **`application.properties`**: Default configuration (H2 database)
- **`application-dev.properties`**: Development configuration (PostgreSQL)
- **`application-prod.properties`**: Production configuration (PostgreSQL)

#### Default Configuration (H2)
```properties
# Uses H2 in-memory database
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### Development Configuration (PostgreSQL)
```properties
# Uses PostgreSQL with persistent storage
spring.datasource.url=jdbc:postgresql://localhost:5432/recipehub_development
spring.datasource.username=postgres
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

### Troubleshooting

#### Common Issues

**1. PostgreSQL Connection Error**
```
Error: Could not create connection to database server
```
**Solution**: Ensure PostgreSQL is running and accessible on localhost:5432

**2. Database Not Found**
```
Error: database "recipehub_development" does not exist
```
**Solution**: Create the database:
```sql
CREATE DATABASE recipehub_development;
```

**3. Permission Denied**
```
Error: permission denied for database recipehub_development
```
**Solution**: Check PostgreSQL user permissions or update credentials in `application-dev.properties`

**4. Port Already in Use**
```
Error: Web server failed to start. Port 8080 was already in use
```
**Solution**: Stop other applications using port 8080 or change the port in configuration

#### Useful Commands

```bash
# Check if PostgreSQL is running
pg_isready -h localhost -p 5432

# List PostgreSQL databases
psql -U postgres -l

# Connect to PostgreSQL
psql -U postgres -d recipehub_development

# Check application logs
tail -f logs/application.log
```

## Authentication Endpoints (`/api/auth`)

### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "string (required, 3-50 characters)",
  "email": "string (required, valid email format)",
  "password": "string (required, 6-100 characters)"
}
```

**Response Body (201 Created):**
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```
*Note: Password is excluded from response*

**Error Response (400 Bad Request):**
```json
{
  "message": "User already exists"
}
```

**Validation Errors:**
```json
{
  "message": "Validation error",
  "details": "username: Username must be between 3 and 50 characters, email: must be a valid email address, password: Password must be between 6 and 100 characters"
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
{
  "message": "Authentication required"
}
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
{
  "message": "User not found with id: 1"
}
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
  "currentPassword": "string (required)",
  "newPassword": "string (required, 6-100 characters)"
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

**Error Responses:**
- **400 Bad Request:** `"Current password and new password are required"`
- **401 Unauthorized:** `"Current password is incorrect"`

---

### 8. Update User Email
**PUT** `/api/users/{userId}/email`

**Request Body:**
```json
{
  "currentPassword": "string (required)",
  "newEmail": "string (required, valid email format)"
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

**Error Responses:**
- **400 Bad Request:** `"Current password and new email are required"`
- **401 Unauthorized:** `"Current password is incorrect"`
- **409 Conflict:** `"Email already exists"`

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

**Error Response (404 Not Found):**
```json
{
  "message": "User not found with id: 1"
}
```

---

### 11. Get User Cooked Recipes
**GET** `/api/users/{userId}/recipes/cooked`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 3,
    "title": "Avocado Toast",
    "description": "Simple and delicious breakfast",
    "ingredients": [
      {
        "name": "Avocado",
        "unit": "pcs",
        "quantity": 1.0
      },
      {
        "name": "Bread",
        "unit": "slices",
        "quantity": 2.0
      },
      {
        "name": "Salt",
        "unit": "tsp",
        "quantity": 0.5
      }
    ],
    "instructions": [
      "Toast the bread.",
      "Mash the avocado and spread it on the toast.",
      "Sprinkle with salt and serve."
    ],
    "isPublic": false,
    "cooked": true,
    "favourite": true,
    "likeCount": 0,
    "authorId": 2,
    "authorUsername": "david",
    "originalRecipeId": null,
    "createdAt": "2025-07-24T11:49:14.582689",
    "updatedAt": "2025-07-24T11:49:14.611497"
  }
]
```

**Key Features:**
- **Filtered Results:** Returns only recipes where `cooked: true`
- **User-Specific:** Only returns recipes owned by the specified user
- **DTO Format:** Returns `RecipeResponseDTO` objects with complete recipe information

---

### 12. Get User Favourite Recipes
**GET** `/api/users/{userId}/recipes/favourite`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Avocado Toast Remix Deluxe",
    "description": "A variation with chili flakes and lemon.",
    "ingredients": [
      {
        "name": "Bread",
        "unit": "slices",
        "quantity": 2.0
      },
      {
        "name": "Avocado",
        "unit": "whole",
        "quantity": 2.0
      },
      {
        "name": "Chili Flakes",
        "unit": "tsp",
        "quantity": 0.5
      },
      {
        "name": "Lemon Juice",
        "unit": "tsp",
        "quantity": 1.0
      }
    ],
    "instructions": [
      "Toast the bread slices.",
      "Mash the avocado and spread on toast.",
      "Sprinkle chili flakes and drizzle lemon juice on top."
    ],
    "isPublic": false,
    "cooked": false,
    "favourite": true,
    "likeCount": 5,
    "authorId": 1,
    "authorUsername": "alice",
    "originalRecipeId": null,
    "createdAt": "2025-07-24T11:22:10.63407",
    "updatedAt": "2025-07-30T11:34:42.626025"
  }
]
```

**Key Features:**
- **Filtered Results:** Returns only recipes where `favourite: true`
- **User-Specific:** Only returns recipes owned by the specified user
- **DTO Format:** Returns `RecipeResponseDTO` objects with complete recipe information

---

### 13. Get User Recipe Books
**GET** `/api/users/{userId}/recipe-books`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "name": "My Breakfast Recipes",
    "description": "Collection of my favorite breakfast recipes",
    "isPublic": true,
    "userId": 1,
    "recipeIds": [1, 2, 3]
  },
  {
    "id": 2,
    "name": "Private Collection",
    "description": "My private recipe collection",
    "isPublic": false,
    "userId": 1,
    "recipeIds": [4, 5]
  }
]
```

**Error Response (404 Not Found):**
```json
{
  "message": "User not found with id: 1"
}
```

---

## Recipe Management Endpoints (`/api/recipes`)

### 14. Create Recipe
**POST** `/api/recipes`

**Request Body:**
```json
{
  "title": "string (required, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "ingredients": [
    {
      "name": "string (required, 1-100 characters)",
      "unit": "string (required, 1-50 characters)",
      "quantity": 1.0 (required, minimum 0.1)
    }
  ] (required, at least 1 ingredient),
  "instructions": ["string"] (required, at least 1 instruction),
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "authorId": 1 (required),
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

**Error Responses:**
- **400 Bad Request:** `"User not found"` or validation errors
- **400 Bad Request:** `"Validation error"` with specific field validation details

---

### 15. Get All Recipes
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

**Key Features:**
- **All Recipes:** Returns both public and private recipes
- **DTO Format:** Returns `RecipeResponseDTO` objects with complete recipe information
- **User Information:** Includes user details for each recipe

---

### 16. Get All Public Recipes
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

**Key Features:**
- **Public Only:** Returns only recipes where `isPublic: true`
- **DTO Format:** Returns `RecipeResponseDTO` objects with complete recipe information
- **User Information:** Includes user details for each recipe

---

### 17. Search Recipes by Title
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
- **DTO Format:** Returns `RecipeResponseDTO` objects with complete recipe information

---

### 18. Get Recipe by ID
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
  "message": "Recipe not found with id: 1"
}
```

---

### 19. Update Recipe
**PUT** `/api/recipes/{id}`

**Request Body (Partial Update Supported):**
```json
{
  "title": "string (optional, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "ingredients": [
    {
      "name": "string (required, 1-100 characters)",
      "unit": "string (required, 1-50 characters)",
      "quantity": 1.0 (required, minimum 0.1)
    }
  ] (optional, at least 1 ingredient if provided),
  "instructions": ["string"] (optional, at least 1 instruction if provided),
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "userId": 1
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
- **403 Forbidden:** `"Only the recipe owner can update this recipe"`
- **404 Not Found:** `"Recipe not found with id: 1"`
- **400 Bad Request:** `"Validation error"` with specific field validation details

---

### 20. Update Recipe Like Count
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
  "message": "Recipe not found with id: 1"
}
```

**Notes:**
- No authorization required - anyone can update like count
- Frontend-driven: Frontend sends the final like count value
- Does not update the recipe's `updatedAt` timestamp
- Simple count update without affecting recipe content

---

### 21. Fork Recipe
**POST** `/api/recipes/{id}/fork`

**Description:** Creates a copy of an existing recipe with optional modifications. Similar to GitHub's fork functionality.

**Request Body (Optional - for modifications):**
```json
{
  "title": "string (optional, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "ingredients": [
    {
      "name": "string (required, 1-100 characters)",
      "unit": "string (required, 1-50 characters)",
      "quantity": 1.0 (required, minimum 0.1)
    }
  ] (optional, at least 1 ingredient if provided),
  "instructions": ["string"] (optional, at least 1 instruction if provided),
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
- **400 Bad Request:** `"Recipe to fork not found"`, `"User not found"`, or validation errors
- **404 Not Found:** `"Recipe not found with id: 1"`

**Key Features:**
- **Content Copying:** Copies all content from the recipe being forked (like GitHub)
- **Optional Modifications:** Can apply changes during forking process
- **Original Recipe Tracking:** `originalRecipeId` always points to the root original recipe
- **Like Count Reset:** New forks start with `likeCount: 0`
- **User Assignment:** Forked recipe is assigned to the user performing the fork
- **Fork Chain Support:** Can fork any recipe in a chain, always pointing back to the original

**Fork Chain Example:**
```
Original Recipe (ID: 1) ← originalRecipeId: null
├── Fork 1 (ID: 2) ← originalRecipeId: 1
├── Modified Fork (ID: 3) ← originalRecipeId: 1
└── Fork of Fork (ID: 4) ← originalRecipeId: 1
```

---

## Recipe Book Management Endpoints (`/api/recipebooks`)

### 22. Create Recipe Book
**POST** `/api/recipebooks`

**Request Body:**
```json
{
  "name": "string (required, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "isPublic": true,
  "userId": 1,
  "recipeIds": [1, 2, 3]
}
```

**Response Body (200 OK):**
```json
{
  "id": 1,
  "name": "My Breakfast Recipes",
  "description": "Collection of my favorite breakfast recipes",
  "isPublic": true,
  "userId": 1,
  "recipeIds": [1, 2, 3]
}
```

**Error Responses:**
- **400 Bad Request:** `"User not found with id: 1"` or validation errors
- **400 Bad Request:** `"Validation error"` with specific field validation details

---

### 23. Get All Recipe Books
**GET** `/api/recipebooks`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "name": "My Breakfast Recipes",
    "description": "Collection of my favorite breakfast recipes",
    "isPublic": true,
    "userId": 1,
    "recipeIds": [1, 2, 3]
  },
  {
    "id": 2,
    "name": "Private Collection",
    "description": "My private recipe collection",
    "isPublic": false,
    "userId": 1,
    "recipeIds": [4, 5]
  }
]
```

---

### 24. Get All Public Recipe Books
**GET** `/api/recipebooks/public`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "name": "My Breakfast Recipes",
    "description": "Collection of my favorite breakfast recipes",
    "isPublic": true,
    "userId": 1,
    "recipeIds": [1, 2, 3]
  }
]
```

**Key Features:**
- **Public Only:** Returns only recipe books where `isPublic: true`
- **DTO Format:** Returns `RecipeBookDTO` objects with complete recipe book information

---

### 25. Get Recipe Book by ID
**GET** `/api/recipebooks/{id}`

**Request Body:** None

**Response Body (200 OK):**
```json
{
  "id": 1,
  "name": "My Breakfast Recipes",
  "description": "Collection of my favorite breakfast recipes",
  "isPublic": true,
  "userId": 1,
  "recipeIds": [1, 2, 3]
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Recipe book not found with id: 1"
}
```

---

### 26. Update Recipe Book
**PUT** `/api/recipebooks/{id}`

**Request Parameters:**
- `userId` (query parameter): The ID of the user updating the recipe book (optional, positive integer)

**Request Body (Partial Update Supported):**
```json
{
  "name": "string (optional, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "isPublic": true,
  "recipeIds": [1, 2, 3] (optional, array of recipe IDs)
}
```
*Note: All fields are optional for partial updates. The recipeIds array replaces the entire list of recipes in the book.*

**Response Body (200 OK):**
```json
{
  "id": 1,
  "name": "Updated Recipe Book Name",
  "description": "Updated description",
  "isPublic": false,
  "userId": 1,
  "recipeIds": [1, 2, 4, 5]
}
```

**Error Responses:**
- **403 Forbidden:** `"Only the recipe book owner can update this recipe book"`
- **404 Not Found:** `"Recipe book not found with id: 1"` or `"Recipe not found with id: 1"`
- **400 Bad Request:** `"Validation error"` with specific field validation details

**Key Features:**
- **Authorization Required:** Only the recipe book owner can update it
- **Recipe Management:** The `recipeIds` array completely replaces the current recipe list
- **Partial Updates:** Only send the fields you want to change

---

### 27. Delete Recipe Book
**DELETE** `/api/recipebooks/{id}`

**Request Parameters:**
- `userId` (query parameter): The ID of the user deleting the recipe book (required, positive integer)

**Request Body:** None

**Response Body (204 No Content):**
```json
{}
```

**Error Responses:**
- **403 Forbidden:** `"Only the recipe book owner can delete this recipe book"`
- **404 Not Found:** `"Recipe book not found with id: 1"`

---

## Data Transfer Objects (DTOs)

### UserRequestDTO
```json
{
  "username": "string (required, 3-50 characters)",
  "email": "string (required, valid email format)",
  "password": "string (required, 6-100 characters)"
}
```

### UserResponseDTO
```json
{
  "id": 1,
  "username": "string",
  "email": "string",
  "createdAt": "2025-07-29T21:51:22.106186"
}
```
*Note: Password is never included in response DTOs for security reasons*

### RecipeRequestDTO
```json
{
  "title": "string (required, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "ingredients": [
    {
      "name": "string (required, 1-100 characters)",
      "unit": "string (required, 1-50 characters)",
      "quantity": 1.0 (required, minimum 0.1)
    }
  ] (required, at least 1 ingredient),
  "instructions": ["string"] (required, at least 1 instruction),
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "authorId": 1 (required),
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
  "name": "string (required, 1-100 characters)",
  "unit": "string (required, 1-50 characters)",
  "quantity": 1.0 (required, minimum 0.1)
}
```

### RecipeBookDTO
```json
{
  "id": 1,
  "name": "string (required, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "isPublic": true,
  "userId": 1 (required),
  "recipeIds": [1, 2, 3] (optional, array of recipe IDs)
}
```

### PasswordUpdateDTO
```json
{
  "currentPassword": "string (required)",
  "newPassword": "string (required, 6-100 characters)"
}
```

### EmailUpdateDTO
```json
{
  "currentPassword": "string (required)",
  "newEmail": "string (required, valid email format)"
}
```

## Validation Rules

### User Validation
- **username:** Required, 3-50 characters
- **email:** Required, valid email format
- **password:** Required, 6-100 characters

### Recipe Validation
- **title:** Required, 1-255 characters
- **description:** Optional, max 1000 characters
- **ingredients:** Required, at least 1 ingredient
  - **name:** Required, 1-100 characters
  - **unit:** Required, 1-50 characters
  - **quantity:** Required, minimum 0.1
- **instructions:** Required, at least 1 instruction
- **authorId:** Required, positive integer

### Recipe Book Validation
- **name:** Required, 1-255 characters
- **description:** Optional, max 1000 characters
- **userId:** Required, positive integer

### Password/Email Update Validation
- **currentPassword:** Required
- **newPassword:** Required, 6-100 characters
- **newEmail:** Required, valid email format

## HTTP Status Codes

- **200 OK:** Request successful
- **201 Created:** Resource created successfully
- **204 No Content:** Request successful, no content to return
- **400 Bad Request:** Invalid request data or validation errors
- **401 Unauthorized:** Authentication required or invalid credentials
- **403 Forbidden:** Access denied (not the owner)
- **404 Not Found:** Resource not found
- **409 Conflict:** Resource already exists (e.g., duplicate email)
- **500 Internal Server Error:** Server error

## Error Response Format

All error responses follow a consistent format:

```json
{
  "message": "Error type",
  "details": "Detailed error message",
  "timestamp": "2025-07-31T00:37:15.044Z",
  "status": 400
}
```

## Notes

1. **Authentication:** Currently using HTTP Basic Authentication
2. **Password Security:** Passwords are never returned in any API responses for security reasons
3. **User DTOs:** User endpoints use separate RequestDTO (for input) and ResponseDTO (for output) to ensure password security
4. **CORS:** Enabled for all origins (`*`)
5. **Database:** Uses H2 in-memory database for development, PostgreSQL for production
6. **Partial Updates:** Recipe and Recipe Book update endpoints support partial updates (only send fields you want to change)
7. **Privacy:** Recipe and Recipe Book endpoints respect privacy settings (public/private)
8. **Authorization:** Recipe and Recipe Book updates require ownership verification
9. **Timestamps:** All recipes include `createdAt` and `updatedAt` timestamps
10. **Like Count:** Separate endpoint for updating like count without authorization requirements
11. **Recipe Forking:** Fork functionality allows creating copies of recipes with optional modifications, similar to GitHub's fork feature
12. **DTO Consistency:** All endpoints now return consistent DTO objects
13. **User Recipe Filtering:** Dedicated endpoints for cooked and favourite recipes
14. **Search Functionality:** Case-insensitive search with partial matching support
15. **Recipe Books:** New feature for organizing recipes into collections
16. **Validation:** Comprehensive input validation with detailed error messages
17. **Error Handling:** Global exception handling with consistent error response format
18. **Business Logic Separation:** Controllers are now purely REST endpoints with business logic moved to services 