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

### 7. Update User
**PUT** `/api/users/{userId}`

**Request Body:**
```json
{
  "currentPassword": "string (required only for password changes)",
  "username": "string (optional)",
  "email": "string (optional, valid email format)",
  "newPassword": "string (optional, 6-100 characters)"
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
- **400 Bad Request:** `"Current password is required for password changes"` or `"New password must be at least 6 characters long"`
- **401 Unauthorized:** `"Current password is incorrect"`
- **409 Conflict:** `"Username already exists"` or `"Email already exists"`
- **404 Not Found:** `"User not found with id: {userId}"`

**Notes:**
- At least one field (username, email, or newPassword) must be provided
- **currentPassword is only required when changing password**
- Username and email can be updated without password verification
- Username and email uniqueness is validated
- Password changes require current password verification for security

---

### 8. Delete User
**DELETE** `/api/users/{id}`

**Request Body:** None

**Response Body (204 No Content):**
```json
{}
```

---

### 9. Get User Recipes
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

### 10. Get User Cooked Recipes
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

### 11. Get User Favourite Recipes
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

### 12. Get User Recipe Books
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

### 14. Create Recipe (Supports both with and without image)
**POST** `/api/recipes`

**Content-Type:** `multipart/form-data`

**Parameters:**
- `file` (optional): Image file to upload (max 5MB, image types only)
- `title` (required): Recipe title (1-255 characters)
- `description` (optional): Recipe description (max 1000 characters)
- `ingredients` (required): JSON array of ingredients
- `instructions` (required): JSON array of instructions
- `authorId` (required): User ID of the author
- `isPublic` (optional): Whether recipe is public (default: false)
- `cooked` (optional): Whether user has cooked this (default: false)
- `favourite` (optional): Whether user has favorited this (default: false)
- `tagNames` (optional): JSON array of tag names (e.g., ["Italian", "Quick", "Easy"])

**Note:** This endpoint handles both creating recipes with and without images. The `file` parameter is optional - if provided, the image will be uploaded to S3 and the `imageUrl` will be set in the response.


**Example without image:**
```bash
curl -X POST http://localhost:8080/api/recipes \
  -F "title=Chocolate Cake" \
  -F "description=A delicious chocolate cake recipe" \
  -F "ingredients=[{\"name\":\"Flour\",\"unit\":\"cups\",\"quantity\":2.0},{\"name\":\"Sugar\",\"unit\":\"cups\",\"quantity\":1.5}]" \
  -F "instructions=[\"Mix dry ingredients\",\"Mix wet ingredients\",\"Bake at 350F\"]" \
  -F "authorId=1" \
  -F "isPublic=true" \
  -F "tagNames=[\"Dessert\",\"Baking\",\"Sweet\"]" \

```

**Example with image:**
```bash
curl -X POST http://localhost:8080/api/recipes \
  -F "file=@image.jpg" \
  -F "title=Spaghetti Carbonara" \
  -F "description=A classic Italian pasta dish" \
  -F "ingredients=[{\"name\":\"Pasta\",\"unit\":\"pounds\",\"quantity\":1.0},{\"name\":\"Eggs\",\"unit\":\"pieces\",\"quantity\":4.0}]" \
  -F "instructions=[\"Boil pasta\",\"Cook bacon\",\"Mix with eggs\"]" \
  -F "authorId=1" \
  -F "isPublic=true" \
  -F "tagNames=[\"Italian\",\"Pasta\",\"Quick\"]" \

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
  "tags": ["Dessert", "Baking", "Sweet"],
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

**Authorization:** Only the recipe owner (author) can update the recipe.

**Request Body (Partial Update Supported):**
```json
{
  "authorId": 1,
  "title": "string (optional, 1-255 characters)",
  "description": "string (optional, max 1000 characters)",
  "ingredients": [
    {
      "name": "string (1-100 characters)",
      "unit": "string (1-50 characters)",
      "quantity": 1.0 (minimum 0.1)
    }
  ] (optional),
  "instructions": ["string"] (optional),
  "imageUrl": "string (optional, valid HTTP/HTTPS URL or S3 path)",
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 0,
  "tagNames": ["string"] (optional, replaces all existing tags)
}
```

**Key Features:**
- **Partial Updates:** Only provided fields are updated; existing data is preserved for omitted fields
- **Authorization Required:** `authorId` must match the recipe owner
- **Tag Management:** If `tagNames` is provided, it replaces all existing tags; if omitted, existing tags are preserved
- **No Validation Requirements:** Only `authorId` is required for authorization

**Example Request - Add Tags Only:**
```json
{
  "authorId": 2,
  "tagNames": ["dinner", "quick", "healthy"]
}
```

**Example Request - Update Multiple Fields:**
```json
{
  "authorId": 1,
  "title": "Updated Recipe Title",
  "description": "Updated description with new details",
  "isPublic": false,
  "tagNames": ["italian", "pasta", "dinner"]
}
```

**Response Body (200 OK):**
```json
{
  "id": 1,
  "title": "Updated Recipe Title",
  "description": "Updated description with new details",
  "ingredients": [
    {
      "name": "Pasta",
      "unit": "cups",
      "quantity": 2.0
    }
  ],
  "instructions": ["Boil water", "Cook pasta", "Serve hot"],
  "imageUrl": "https://example.com/image.jpg",
  "isPublic": false,
  "cooked": true,
  "favourite": true,
  "likeCount": 5,
  "authorId": 1,
  "authorUsername": "chef_alice",
  "originalRecipeId": null,
  "tags": ["italian", "pasta", "dinner"],
  "createdAt": "2025-07-29T21:51:22.106186",
  "updatedAt": "2025-07-29T21:51:29.499094"
}
```

**Error Responses:**
- **404 Not Found:** `"Recipe not found with id: 1"`
- **401 Unauthorized:** `"User not authorized to update this recipe"` (when authorId doesn't match recipe owner)
- **400 Bad Request:** `"Internal server error"` with validation details (rare, only for malformed data)

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

### 21.1. Fork Recipe
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

### 21.5. Recipe Image Management

**Note:** The main `POST /api/recipes` endpoint supports both creating recipes with and without images. For existing recipes, use the separate image management endpoints below.

#### Upload Image to Existing Recipe

---

#### Upload Image to Existing Recipe
**POST** `/api/recipes/{id}/image`

**Description:** Upload an image for an existing recipe

**Content-Type:** `multipart/form-data`

**Request Parameters:**
- `file` (required): Image file (max 5MB, image types only)

**Example Request:**
```
POST /api/recipes/1/image
Content-Type: multipart/form-data

file: [image file]
```

**Response Body (200 OK):**
```json
{
  "recipe": {
    "id": 1,
    "title": "Chocolate Cake",
    "description": "A delicious chocolate cake recipe",
    "ingredients": [
      {
        "name": "Flour",
        "unit": "cups",
        "quantity": 2.0
      }
    ],
    "instructions": ["Mix ingredients", "Bake at 350F"],
    "imageUrl": "https://s3.amazonaws.com/bucket/recipe-images/new-uuid.jpg",
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "chef123",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:51:22.106186",
    "updatedAt": "2025-07-29T21:51:22.108822"
  },
  "imageUrl": "https://s3.amazonaws.com/bucket/recipe-images/new-uuid.jpg",
  "fileName": "recipe-images/new-uuid.jpg",
  "message": "Recipe image updated successfully"
}
```

**Error Responses:**
- **400 Bad Request:** `{"error": "File is empty"}` or `{"error": "File must be an image"}` or `{"error": "File size must be less than 5MB"}`
- **404 Not Found:** `{"error": "Recipe not found with id: 1"}`
- **500 Internal Server Error:** `{"error": "Failed to upload image: [error details]"}` or `{"error": "Failed to update recipe: [error details]"}`

---

#### Delete Image from Recipe
**DELETE** `/api/recipes/{id}/image`

**Description:** Delete the image from an existing recipe

**Request Body:** None

**Response Body (200 OK):**
```json
{
  "recipe": {
    "id": 1,
    "title": "Chocolate Cake",
    "description": "A delicious chocolate cake recipe",
    "ingredients": [
      {
        "name": "Flour",
        "unit": "cups",
        "quantity": 2.0
      }
    ],
    "instructions": ["Mix ingredients", "Bake at 350F"],
    "imageUrl": null,
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "chef123",
    "originalRecipeId": null,
    "createdAt": "2025-07-29T21:51:22.106186",
    "updatedAt": "2025-07-29T21:51:22.108822"
  },
  "deletedFileName": "recipe-images/uuid.jpg",
  "message": "Recipe image deleted successfully"
}
```

**Error Responses:**
- **400 Bad Request:** `{"error": "Recipe does not have an image to delete"}` or `{"error": "Invalid image URL format"}`
- **404 Not Found:** `{"error": "Recipe not found with id: 1"}`
- **500 Internal Server Error:** `{"error": "Failed to delete recipe image: [error details]"}`



## Recipe Book Management Endpoints (`/api/recipebooks`)

### 21.6. Create Recipe Book
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

### 21.7. Get All Recipe Books
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

### 21.8. Get All Public Recipe Books
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

### 21.9. Get Recipe Book by ID
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

### 21.10. Update Recipe Book
**PUT** `/api/recipebooks/{id}`

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
- **404 Not Found:** `"Recipe book not found with id: 1"` or `"Recipe not found with id: 1"`
- **400 Bad Request:** `"Validation error"` with specific field validation details

**Key Features:**
- **Frontend Authorization:** Users can only access their own recipe books through "My books"
- **Recipe Management:** The `recipeIds` array completely replaces the current recipe list
- **Partial Updates:** Only send the fields you want to change
- **Simplified API:** No userId parameter required - frontend ensures ownership

---

### 21.11. Delete Recipe Book
**DELETE** `/api/recipebooks/{id}`

**Request Body:** None

**Response Body (204 No Content):**
```json
{}
```

**Error Responses:**
- **404 Not Found:** `"Recipe book not found with id: 1"`

**Key Features:**
- **Frontend Authorization:** Users can only access their own recipe books through "My books"
- **Simplified API:** No userId parameter required - frontend ensures ownership

---

## Tag Management Endpoints (`/api/tags`)

### 28. Get All Tags
**GET** `/api/tags`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Italian",
    "recipeCount": 1
  },
  {
    "id": 2,
    "name": "Mexican",
    "recipeCount": 1
  },
  {
    "id": 3,
    "name": "Asian",
    "recipeCount": 0
  }
]
```

**Key Features:**
- **All Tags:** Returns all available tags with recipe counts
- **Sorted by ID:** Tags are returned in order of creation
- **Recipe Count:** Shows how many recipes use each tag

---

### 29. Get Popular Tags
**GET** `/api/tags/popular`

**Request Parameters:**
- `limit` (query parameter): Maximum number of tags to return (optional, default: 10)

**Request Body:** None

**Example Request:**
```
GET /api/tags/popular?limit=5
```

**Response Body (200 OK):**
```json
[
  {
    "id": 42,
    "name": "Quick",
    "recipeCount": 2
  },
  {
    "id": 1,
    "name": "Italian",
    "recipeCount": 1
  },
  {
    "id": 2,
    "name": "Mexican",
    "recipeCount": 1
  },
  {
    "id": 13,
    "name": "Dinner",
    "recipeCount": 1
  },
  {
    "id": 37,
    "name": "Easy",
    "recipeCount": 1
  }
]
```

**Key Features:**
- **Sorted by Popularity:** Tags are sorted by recipe count (descending)
- **Configurable Limit:** Can specify how many tags to return
- **Recipe Count:** Shows how many recipes use each tag

---

### 30. Get Category Tags
**GET** `/api/tags/categories`

**Request Body:** None

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Italian",
    "recipeCount": 1
  },
  {
    "id": 2,
    "name": "Mexican",
    "recipeCount": 1
  },
  {
    "id": 11,
    "name": "Breakfast",
    "recipeCount": 0
  },
  {
    "id": 12,
    "name": "Lunch",
    "recipeCount": 0
  },
  {
    "id": 13,
    "name": "Dinner",
    "recipeCount": 1
  },
  {
    "id": 19,
    "name": "Vegetarian",
    "recipeCount": 0
  },
  {
    "id": 37,
    "name": "Easy",
    "recipeCount": 1
  },
  {
    "id": 42,
    "name": "Quick",
    "recipeCount": 2
  },
  {
    "id": 57,
    "name": "Healthy",
    "recipeCount": 0
  }
]
```

**Key Features:**
- **Main Categories:** Returns curated list of main category tags
- **Cross-Category:** Includes tags from different categories (cuisine, meal type, dietary, etc.)
- **Recipe Count:** Shows how many recipes use each tag

---

### 31. Initialize Predefined Tags
**POST** `/api/tags/initialize`

**Request Body:** None

**Response Body (200 OK):**
```json
"Predefined tags initialized successfully"
```

**Key Features:**
- **One-Time Setup:** Populates database with 80+ predefined tags
- **Categories Included:** Cuisine types, meal types, dietary restrictions, cooking methods, difficulty levels, occasions, seasons, health categories, ingredient types, and special features
- **Safe Operation:** Won't duplicate tags if already initialized

**Tag Categories:**
- **Cuisine Types:** Italian, Mexican, Asian, French, Indian, Mediterranean, American, Thai, Japanese, Chinese
- **Meal Types:** Breakfast, Lunch, Dinner, Dessert, Appetizer, Snack, Brunch, Late Night
- **Dietary:** Vegetarian, Vegan, Gluten-Free, Dairy-Free, Low-Carb, Keto, Paleo, Halal, Kosher
- **Cooking Methods:** Baked, Grilled, Fried, Steamed, Roasted, Slow Cooker, Instant Pot, Air Fryer, Smoked
- **Difficulty:** Easy, Medium, Hard, Beginner, Advanced, Quick, 30-Minute Meals
- **Occasions:** Holiday, Birthday, Anniversary, Party, Date Night, Family Dinner, Potluck, Picnic
- **Seasons:** Spring, Summer, Fall, Winter, Seasonal
- **Health:** Healthy, Low-Calorie, High-Protein, Low-Sodium, Heart-Healthy, Anti-Inflammatory
- **Ingredients:** Chicken, Beef, Pork, Fish, Seafood, Pasta, Rice, Vegetables, Fruits, Nuts, Cheese
- **Special Features:** One-Pot, Make-Ahead, Freezer-Friendly, Kid-Friendly, Crowd-Pleaser, Comfort Food, Gourmet

---

## Enhanced Recipe Search Endpoints (`/api/recipes`)

### 32. Enhanced Recipe Search
**GET** `/api/recipes/search`

**Request Parameters (All Optional):**
- `title` (string): Search for recipes with title containing this text
- `tags` (array): Search for recipes with any of these tags
- `author` (string): Search for recipes by author username
- `isPublic` (boolean): Filter by public/private status
- `cooked` (boolean): Filter by cooked status
- `favourite` (boolean): Filter by favourite status
- `cuisine` (string): Filter by cuisine type (e.g., "Italian", "Mexican")
- `difficulty` (string): Filter by difficulty level (e.g., "Easy", "Medium", "Hard")
- `mealType` (string): Filter by meal type (e.g., "Breakfast", "Dinner")
- `dietary` (string): Filter by dietary restriction (e.g., "Vegetarian", "Vegan")
- `cookingMethod` (string): Filter by cooking method (e.g., "Baked", "Grilled")
- `occasion` (string): Filter by occasion (e.g., "Holiday", "Party")
- `season` (string): Filter by season (e.g., "Summer", "Winter")
- `health` (string): Filter by health category (e.g., "Healthy", "Low-Calorie")
- `ingredient` (string): Filter by ingredient type (e.g., "Chicken", "Pasta")
- `specialFeature` (string): Filter by special feature (e.g., "Quick", "One-Pot")

**Example Requests:**

**Basic title search:**
```
GET /api/recipes/search?title=pasta
```

**Single tag search:**
```
GET /api/recipes/search?tags=Italian
```

**Multiple tags search:**
```
GET /api/recipes/search?tags=Quick&tags=Easy
```

**Cuisine filter:**
```
GET /api/recipes/search?cuisine=Mexican
```

**Complex search:**
```
GET /api/recipes/search?title=pasta&cuisine=Italian&difficulty=Easy&tags=Quick
```

**Response Body (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Quick Italian Pasta",
    "description": "A quick and easy Italian pasta dish",
    "ingredients": [
      {
        "name": "Pasta",
        "unit": "lb",
        "quantity": 1.0
      },
      {
        "name": "Olive oil",
        "unit": "tbsp",
        "quantity": 2.0
      }
    ],
    "instructions": [
      "Boil pasta according to package directions",
      "Heat olive oil in pan",
      "Add minced garlic and cook until fragrant"
    ],
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "testuser",
    "originalRecipeId": null,
    "tags": [
      "Italian",
      "Quick",
      "Easy",
      "Pasta"
    ],
    "createdAt": "2025-08-06T00:46:55.063737",
    "updatedAt": "2025-08-06T00:46:55.056021"
  }
]
```

**Empty Response (200 OK):**
```json
[]
```

**Key Features:**
- **Database-Level Filtering:** Uses JPA Specifications for efficient SQL queries
- **Multiple Criteria:** Can combine any combination of search parameters
- **Case-Insensitive:** All text searches are case-insensitive
- **Tag Categories:** Support for specific tag categories (cuisine, difficulty, etc.)
- **Generic Tags:** Support for generic tag search with multiple values
- **Performance:** Efficient database queries instead of in-memory filtering
- **Flexible:** Can search with any combination of parameters

**Search Examples:**

**Find all Italian recipes:**
```
GET /api/recipes/search?cuisine=Italian
```

**Find quick and easy recipes:**
```
GET /api/recipes/search?tags=Quick&tags=Easy
```

**Find dinner recipes with pasta:**
```
GET /api/recipes/search?mealType=Dinner&ingredient=Pasta
```

**Find healthy vegetarian recipes:**
```
GET /api/recipes/search?dietary=Vegetarian&health=Healthy
```

**Find recipes by specific author:**
```
GET /api/recipes/search?author=testuser
```

**Find only public recipes:**
```
GET /api/recipes/search?isPublic=true
```

**Find recipes with multiple criteria:**
```
GET /api/recipes/search?title=pasta&cuisine=Italian&difficulty=Easy&tags=Quick&mealType=Dinner
```

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
  "imageUrl": "string (optional, max 1000 characters, S3 URL or HTTP/HTTPS URL)",
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "authorId": 1 (required),
  "originalRecipeId": null,
  "tagNames": ["Italian", "Quick", "Easy"] (optional, array of tag names)
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
  "imageUrl": "string (S3 URL or HTTP/HTTPS URL)",
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 0,
  "authorId": 1,
  "authorUsername": "string",
  "originalRecipeId": null,
  "tags": ["Italian", "Quick", "Easy"],
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
  "userId": 1,
  "recipeIds": [1, 2, 3] (optional, array of recipe IDs)
}
```

### TagDTO
```json
{
  "id": 1,
  "name": "string",
  "recipeCount": 5
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
- **imageUrl:** Optional, max 1000 characters, must be valid HTTP/HTTPS URL or S3 path
- **tagNames:** Optional, array of tag names (strings)
- **authorId:** Required, positive integer

### Recipe Book Validation
- **name:** Required, 1-255 characters
- **description:** Optional, max 1000 characters
- **userId:** Included in response, managed by backend

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
- **403 Forbidden:** Access denied (legacy - not used in current implementation)
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
8. **Authorization:** Frontend controls access to user's own recipes and recipe books
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
19. **Tag System:** Comprehensive tag system for recipe categorization and filtering
20. **Enhanced Search:** Database-level filtering using JPA Specifications for efficient search
21. **Tag Categories:** Support for specific tag categories (cuisine, difficulty, meal type, etc.)
22. **Predefined Tags:** 80+ predefined tags across multiple categories for consistent categorization 