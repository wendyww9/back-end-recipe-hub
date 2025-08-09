package com.recipehub.backendrecipehub.specification;

import com.recipehub.backendrecipehub.model.Recipe;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class RecipeSpecification {

    public static Specification<Recipe> hasTitle(String title) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Recipe> isPublic(Boolean isPublic) {
        return (root, query, cb) ->
                cb.equal(root.get("isPublic"), isPublic);
    }

    public static Specification<Recipe> isCooked(Boolean cooked) {
        return (root, query, cb) ->
                cb.equal(root.get("cooked"), cooked);
    }

    public static Specification<Recipe> isFavourite(Boolean favourite) {
        return (root, query, cb) ->
                cb.equal(root.get("favourite"), favourite);
    }

    public static Specification<Recipe> hasAuthor(String authorName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("author").get("username")), authorName.toLowerCase());
    }

    public static Specification<Recipe> hasTag(String tagName) {
        return (root, query, cb) -> {
            Join<Object, Object> tags = root.join("tags", JoinType.INNER);
            return cb.equal(cb.lower(tags.get("name")), tagName.toLowerCase());
        };
    }

    public static Specification<Recipe> hasAnyTag(List<String> tagNames) {
        return (root, query, cb) -> {
            if (tagNames == null || tagNames.isEmpty()) {
                return cb.conjunction();
            }
            Join<Object, Object> tags = root.join("tags", JoinType.INNER);
            // Case-insensitive IN: lower(tag.name) IN lower(tagNames)
            var lowered = tagNames.stream()
                    .filter(n -> n != null)
                    .map(String::toLowerCase)
                    .toList();
            CriteriaBuilder.In<String> inClause = cb.in(cb.lower(tags.get("name")));
            for (String n : lowered) {
                inClause.value(n);
            }
            return inClause;
        };
    }

    public static Specification<Recipe> hasAllTags(List<String> tagNames) {
        return (root, query, cb) -> {
            if (tagNames == null || tagNames.isEmpty()) {
                return cb.conjunction();
            }
            query.distinct(true);
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Recipe> subRoot = subquery.from(Recipe.class);
            Join<Object, Object> tagJoin = subRoot.join("tags");

            var lowered = tagNames.stream()
                    .filter(n -> n != null)
                    .map(String::toLowerCase)
                    .toList();

            subquery.select(subRoot.get("id"))
                    .where(cb.lower(tagJoin.get("name")).in(lowered))
                    .groupBy(subRoot.get("id"))
                    .having(cb.equal(cb.countDistinct(cb.lower(tagJoin.get("name"))), lowered.size()));

            return cb.in(root.get("id")).value(subquery);
        };
    }

    // Specific tag category filters
    public static Specification<Recipe> hasCuisine(String cuisine) {
        return hasTag(cuisine);
    }

    public static Specification<Recipe> hasDifficulty(String difficulty) {
        return hasTag(difficulty);
    }

    public static Specification<Recipe> hasMealType(String mealType) {
        return hasTag(mealType);
    }

    public static Specification<Recipe> hasDietary(String dietary) {
        return hasTag(dietary);
    }

    public static Specification<Recipe> hasCookingMethod(String cookingMethod) {
        return hasTag(cookingMethod);
    }

    public static Specification<Recipe> hasOccasion(String occasion) {
        return hasTag(occasion);
    }

    public static Specification<Recipe> hasSeason(String season) {
        return hasTag(season);
    }

    public static Specification<Recipe> hasHealth(String health) {
        return hasTag(health);
    }

    public static Specification<Recipe> hasIngredient(String ingredient) {
        return hasTag(ingredient);
    }

    public static Specification<Recipe> hasSpecialFeature(String specialFeature) {
        return hasTag(specialFeature);
    }
} 