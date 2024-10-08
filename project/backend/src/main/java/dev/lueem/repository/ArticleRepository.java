package dev.lueem.repository;

import dev.lueem.clients.FirebaseClient;
import dev.lueem.model.Article;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository for managing {@link Article} entities.
 */
@ApplicationScoped
public class ArticleRepository {

    private static final Logger LOGGER = Logger.getLogger(ArticleRepository.class.getName());

    @Inject
    FirebaseClient firbaseClient;

    /**
     * Persists a list of articles to the database.
     *
     * @param articles the list of articles to save
     * @throws IllegalArgumentException if the articles list is null or empty
     * @throws RepositoryException      if an error occurs during persistence
     */
    @Transactional
    public void saveAll(List<Article> articles) {
        validateArticles(articles);
        try {
            LOGGER.info("Sending " + articles.size() + " articles to Firebase.");
            List<Article> savedArticles = firbaseClient.insertArticles(articles);
            LOGGER.info(String.format("Successfully saved %d articles to Firebase.", savedArticles.size()));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save articles.", e);
            throw new RepositoryException("Error saving articles.", e);
        }
    }

    /**
     * Validates the list of articles before persistence.
     *
     * @param articles the list of articles to validate
     * @throws IllegalArgumentException if the list is null or empty
     */
    private void validateArticles(List<Article> articles) {
        if (Objects.isNull(articles) || articles.isEmpty()) {
            LOGGER.warning("Attempted to save a null or empty list of articles.");
            throw new IllegalArgumentException("Article list cannot be null or empty.");
        }
    }

    /**
     * Custom exception to represent repository-related errors.
     */
    public static class RepositoryException extends RuntimeException {
        public RepositoryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
