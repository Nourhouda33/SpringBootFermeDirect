package com.FermeDirecte.FermeDirecte.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // S'exécute AVANT le DataInitializer
public class DatabaseMigrationConfig implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        supprimerIndexUniqueNomProduit();
    }

    private void supprimerIndexUniqueNomProduit() {
        try {
            // Récupérer tous les index UNIQUE sur la colonne 'nom' de la table 'products'
            var indexes = jdbcTemplate.queryForList(
                "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'products' " +
                "AND COLUMN_NAME = 'nom' " +
                "AND NON_UNIQUE = 0 " +
                "AND INDEX_NAME != 'PRIMARY'"
            );

            for (var index : indexes) {
                String indexName = (String) index.get("INDEX_NAME");
                jdbcTemplate.execute("ALTER TABLE products DROP INDEX `" + indexName + "`");
                log.info("✅ Index unique '{}' supprimé de la table products", indexName);
            }

            if (indexes.isEmpty()) {
                log.debug("Aucun index unique sur products.nom — rien à faire.");
            }

        } catch (Exception e) {
            log.warn("Impossible de vérifier/supprimer l'index unique sur products.nom : {}", e.getMessage());
        }
    }
}
