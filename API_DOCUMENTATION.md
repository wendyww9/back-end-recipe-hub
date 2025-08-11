# RecipeHub API Documentation

## Base URL
```
local test: http://localhost:8080/api
Deployment: https://back-end-recipe-hub.onrender.com/api
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

**Response Body (200 OK):**
```json
{ "message": "User deleted successfully" }
```

**Behavior:**
- Performs a soft delete on the user and anonymizes credentials (username/email randomized, password scrambled).
- User is excluded from user listings and lookups (`/api/users`, `/api/users/{id}` returns 404 after deletion).
- User-owned resources (recipes, recipe books) remain available.
- Recipes authored by a deleted user will expose `authorUsername: "Deleted Account"` in responses.

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
      { "name": "Chocolate", "unit": "cups", "quantity": 2.0 }
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
  }
]
```

**Empty Response (200 OK):**
```json
[]
```

**Key Features:**
- **Public Only:** Returns only recipes where `isPublic: true`
- **Case-Insensitive:** Search is not case-sensitive (e.g., "chocolate" matches "Chocolate")
- **Partial Matching:** Uses `LIKE` query with wildcards (e.g., "choc" matches "Chocolate")
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

**Authorization:** Frontend-controlled authorization (no backend validation)

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
  "tagNames": ["string"] (optional, replaces all existing tags),
  "tagsToAdd": ["string"] (optional, adds tags without affecting existing ones),
  "tagsToDelete": ["string"] (optional, removes specific tags)
}
```

**Key Features:**
- **Partial Updates:** Only provided fields are updated; existing data is preserved for omitted fields
- **Frontend Authorization:** Authorization is controlled by the frontend
- **Granular Tag Management:** Three ways to manage tags:
  - `tagNames`: Replaces all existing tags (legacy behavior)
  - `tagsToAdd`: Adds new tags without affecting existing ones
  - `tagsToDelete`: Removes specific tags
- **Duplicate Prevention:** Tags are checked case-insensitively to prevent duplicates
- **Tag Validation:** Only predefined tags are accepted

**Example Request - Replace All Tags (Legacy):**
```json
{
  "authorId": 2,
  "tagNames": ["dinner", "quick", "healthy"]
}
```

**Example Request - Add Tags Only:**
```json
{
  "authorId": 2,
  "tagsToAdd": ["dinner", "quick"]
}
```

**Example Request - Remove Tags Only:**
```json
{
  "authorId": 2,
  "tagsToDelete": ["quick"]
}
```

**Example Request - Add and Remove Tags:**
```json
{
  "authorId": 2,
  "tagsToAdd": ["healthy", "easy"],
  "tagsToDelete": ["quick"]
}
```

**Example Request - Update Multiple Fields:**
```json
{
  "authorId": 1,
  "title": "Updated Recipe Title",
  "description": "Updated description with new details",
  "isPublic": false,
  "tagsToAdd": ["italian", "pasta"]
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
- **400 Bad Request:** `"Unknown tag: InvalidTag"` (when using invalid tag names)
- **500 Internal Server Error:** `"Query did not return a unique result"` (database issue with duplicate tags)

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

**Deletion Behavior and Soft-Delete:**
- Recipes are soft-deleted via `DELETE /api/recipes/{id}` (flagged `deleted=true`).
- Soft-deleted recipes are excluded from list and recipe book responses.
- Forked recipes remain even if the original is deleted.

---

### 21. Delete Recipe
**DELETE** `/api/recipes/{id}`

**Description:** Soft-deletes a recipe by marking it as deleted. The recipe remains in the database but is excluded from all public queries and responses.

**Path Parameters:**
- `id` (path parameter): The ID of the recipe to delete (positive integer)

**Request Body:** None

**Example Request:**
```
DELETE /api/recipes/1
```

**Response Body (200 OK):**
```json
{
  "message": "Recipe deleted successfully"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Recipe not found with id: 1"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "message": "Failed to delete recipe: [error details]"
}
```

**Notes:**
- **Soft Delete**: The recipe is not physically removed from the database
- **Data Preservation**: All recipe data is preserved for potential recovery
- **Exclusion from Queries**: Soft-deleted recipes are automatically excluded from:
  - `GET /api/recipes` (all recipes)
  - `GET /api/recipes/search` (search results)
  - Recipe book responses
  - User recipe listings
- **Forked Recipes**: If a recipe has been forked, the forked recipes remain accessible even after the original is deleted
- **No Authorization**: Currently no authorization check - any user can delete any recipe
- **Permanent**: Soft deletion is currently permanent (no restore functionality)

---

### 21.1. Fork Recipe
**POST** `/api/recipes/{id}/fork`

**Description:** Creates a copy of an existing recipe with optional modifications. The forked recipe is automatically assigned to the user performing the fork operation. Similar to GitHub's fork functionality.

**Request Body (Optional - for modifications):**
```json
{
  "authorId": "number (optional, defaults to 1)",
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
  "tagNames": ["string"] (optional, list of tag names to assign)
}
```
*Note: All fields are optional. If not provided, the forked recipe will be an exact copy of the original with the current user as the author.*

**Example Requests:**

**Minimal fork (assigns to current user):**
```
POST /api/recipes/1/fork
Content-Type: application/json

{
  "authorId": 2
}
```

**Fork with custom author assignment:**
```
POST /api/recipes/1/fork
Content-Type: application/json

{
  "authorId": 2,
  "title": "My Modified Version",
  "description": "A personalized version of the original recipe"
}
```

**Fork with custom tags:**
```
POST /api/recipes/1/fork
Content-Type: application/json

{
  "authorId": 2,
  "title": "My Carbonara",
  "tagNames": ["dinner", "italian", "pasta"]
}
```

**Fork with full modifications:**
```
POST /api/recipes/1/fork
Content-Type: application/json

{
  "authorId": 2,
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
  "tagNames": ["dessert", "baking", "chocolate"]
}
```

**Response Body (200 OK):**
```json
{
  "id": 27,
  "title": "Spaghetti Carbonara (Forked)",
  "description": "A classic Italian pasta dish",
  "ingredients": [
    {
      "name": "Pasta",
      "unit": "pounds",
      "quantity": 1.0
    },
    {
      "name": "Eggs",
      "unit": "pieces",
      "quantity": 4.0
    }
  ],
  "instructions": [
    "Boil pasta",
    "Cook bacon",
    "Mix with eggs"
  ],
  "imageUrl": null,
  "isPublic": true,
  "cooked": false,
  "favourite": false,
  "likeCount": 0,
  "authorId": 2,
  "authorUsername": "bob_smith",
  "originalRecipeId": 4,
  "tags": ["Mexican", "Dinner", "Quick", "Crowd-Pleaser"],
  "createdAt": "2025-08-08T11:21:22.259503",
  "updatedAt": "2025-08-08T11:21:22.24911"
}
```

**Error Responses:**
- **400 Bad Request:** `"Recipe to fork not found"`, `"User not found"`, or validation errors
- **404 Not Found:** `"Recipe not found with id: 1"`

**Key Features:**
- **Author Assignment:** Automatically assigns the forked recipe to the user performing the fork (via `authorId`)
- **Content Copying:** Copies all content from the recipe being forked (like GitHub)
- **Optional Modifications:** Can apply changes during forking process
- **Tag Management:** Can assign custom tags or inherit original tags
- **Original Recipe Tracking:** `originalRecipeId` always points to the root original recipe
- **Like Count Reset:** New forks start with `likeCount: 0`
- **Recipe List Integration:** Forked recipes appear in the new author's recipe list
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

### 21.6. Create Recipe Book (New Request Type)
**POST** `/api/recipebooks`

**Request Body (RecipeBookCreateRequest):**
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
Merged into GET `/api/recipebooks` which returns public recipe books.

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

### 21.10. Update Recipe Book (New Request Type)
**PUT** `/api/recipebooks/{id}`

**Request Body (RecipeBookUpdateRequest, Partial Update Supported):**
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
- **Simplified API:** No `userId` required on update.

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

**Behavior:**
- Recipe books are hard-deleted. The junction table entries are removed, but the recipes themselves remain.

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

### 31. Tag Initialization
There is no API to create or initialize tags. Tags are predefined and managed outside of the application. Endpoints accept only existing tag names; unknown tags result in a 400 error.

Notes:
- Use `/api/tags`, `/api/tags/popular`, and `/api/tags/categories` to discover available tags.
- When updating recipes, duplicate tag names in the request (case-insensitive) are rejected with 400.

---

## Enhanced Recipe Search Endpoints (`/api/recipes`)

### 32. Enhanced Recipe Search
**GET** `/api/recipes/search`

**Request Parameters (All Optional):**
- `title` (string): Search for recipes with title containing this text
- `tags` (array): Search for recipes with any of these tags
- `author` (string): Search for recipes by author username (Public-only enforced server-side)
- `authorId` (long): Search for recipes and recipe books by author ID (Public-only enforced server-side)
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

**Search Behavior:**
- **Author-Only Search:** When only `authorId` or `author` parameters are provided (no other filters), returns both recipes and recipe books by that author in `AuthorSearchResponse` format
- **Filtered Search:** When any other parameters are provided (title, tags, cuisine, etc.), returns only filtered recipes in `List<RecipeResponseDTO>` format, even if `authorId` or `author` is also specified
- **Case-Insensitive:** All text-based searches (title, author, tags, ingredient, etc.) are case-insensitive
- **Public Only:** Returns only public recipes and recipe books (where `isPublic: true`)
- **Ingredient Search:** Searches within the JSON ingredients field for ingredient names
- **Smart Filtering:** Combines multiple filters using AND logic

**Example Requests:**

**Author-only search (returns recipes + recipe books):**
```
GET /api/recipes/search?authorId=1
```

**Author-only search by username (returns recipes + recipe books):**
```
GET /api/recipes/search?author=alice123
```

**Basic title search (returns filtered recipes only):**
```
GET /api/recipes/search?title=pasta
```

**Single tag search (returns filtered recipes only):**
```
GET /api/recipes/search?tags=Italian
```

**Multiple tags search (returns filtered recipes only):**
```
GET /api/recipes/search?tags=Quick&tags=Easy
```

**Author with filters (returns filtered recipes only):**
```
GET /api/recipes/search?authorId=1&tags=Easy
GET /api/recipes/search?author=alice123&title=Chocolate
```

**Cuisine filter (returns filtered recipes only):**
```
GET /api/recipes/search?cuisine=Mexican
```

**Complex search (returns filtered recipes only):**
```
GET /api/recipes/search?title=pasta&cuisine=Italian&difficulty=Easy&tags=Quick
```

**Ingredient search (returns filtered recipes only):**
```
GET /api/recipes/search?ingredient=flour
GET /api/recipes/search?ingredient=sugar
```

**Combined filters with ingredient (returns filtered recipes only):**
```
GET /api/recipes/search?authorId=1&ingredient=flour
GET /api/recipes/search?title=chocolate&ingredient=flour
GET /api/recipes/search?ingredient=flour&tags=Easy
```

**Response Body (200 OK) - When searching by authorId/author only:**
```json
{
  "authorId": 1,
  "recipes": [
    {
      "id": 1,
      "title": "Quick Italian Pasta",
      "description": "A quick and easy Italian pasta dish",
      "ingredients": [
        {
          "name": "Pasta",
          "unit": "lb",
          "quantity": 1.0
        }
      ],
      "instructions": [
        "Boil pasta according to package directions",
        "Heat olive oil in pan"
      ],
      "isPublic": true,
      "cooked": false,
      "favourite": false,
      "likeCount": 0,
      "authorId": 1,
      "authorUsername": "alice123",
      "originalRecipeId": null,
      "tags": ["Italian", "Quick"],
      "createdAt": "2025-08-06T00:46:55.063737",
      "updatedAt": "2025-08-06T00:46:55.056021"
    }
  ],
  "recipeBooks": [
    {
      "id": 1,
      "name": "My Italian Collection",
      "description": "Collection of Italian recipes",
      "isPublic": true,
      "userId": 1,
      "recipeIds": [1, 2, 3]
    }
  ],
  "totalRecipes": 1,
  "totalRecipeBooks": 1
```

**Response Body (200 OK) - When searching with filters (returns filtered recipes only):**
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
      }
    ],
    "instructions": [
      "Boil pasta according to package directions",
      "Heat olive oil in pan"
    ],
    "isPublic": true,
    "cooked": false,
    "favourite": false,
    "likeCount": 0,
    "authorId": 1,
    "authorUsername": "alice123",
    "originalRecipeId": null,
    "tags": ["Italian", "Quick"],
    "createdAt": "2025-08-06T00:46:55.063737",
    "updatedAt": "2025-08-06T00:46:55.056021"
  }
]
```



**Response Body (200 OK) - When using other filters:**
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
- **Smart Response Format:** Automatically chooses response format based on search parameters
- **Database-Level Filtering:** Uses JPA Specifications for efficient SQL queries
- **Multiple Criteria:** Can combine any combination of search parameters
- **Case-Insensitive:** All text searches are case-insensitive
- **Public-Only:** All searches return only public content 