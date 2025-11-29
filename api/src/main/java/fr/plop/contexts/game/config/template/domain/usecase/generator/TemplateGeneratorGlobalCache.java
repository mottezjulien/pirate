package fr.plop.contexts.game.config.template.domain.usecase.generator;

import java.util.*;
import java.util.function.Consumer;

/**
 * Context pour gérer les références pendant le parsing du template.
 * Permet de définir des références et de les résoudre plus tard.
 */
public class TemplateGeneratorGlobalCache {

    private final Map<String, Object> references = new HashMap<>();
    private final Map<String, List<Consumer<Object>>> pendingResolvers = new HashMap<>();
    private final Set<String> unresolvedReferences = new HashSet<>();

    /**
     * Enregistre une référence avec son objet associé.
     * Résout automatiquement les références en attente.
     */
    public void registerReference(String referenceName, Object object) {
        if (referenceName == null || object == null) {
            return;
        }

        references.put(referenceName, object);
        unresolvedReferences.remove(referenceName);

        // Résoudre les références en attente
        List<Consumer<Object>> resolvers = pendingResolvers.remove(referenceName);
        if (resolvers != null) {
            resolvers.forEach(resolver -> resolver.accept(object));
        }
    }

    /**
     * Demande la résolution d'une référence.
     * Si la référence est déjà disponible, la résout immédiatement.
     * Sinon, la met en attente.
     */
    public void requestReference(String referenceName, Consumer<Object> onResolved) {
        if (referenceName == null || onResolved == null) {
            return;
        }

        if (references.containsKey(referenceName)) {
            // Référence déjà disponible
            onResolved.accept(references.get(referenceName));
        } else {
            // Mettre en attente
            pendingResolvers.computeIfAbsent(referenceName, k -> new ArrayList<>()).add(onResolved);
            unresolvedReferences.add(referenceName);
        }
    }

    /**
     * Essaie de résoudre une référence sans la marquer comme "non résolue" si elle n'existe pas.
     * Utile pour les cas où on n'est pas sûr que c'est une vraie référence.
     */
    public void tryRequestReference(String referenceName, Consumer<Object> onResolved) {
        if (referenceName == null || onResolved == null) {
            return;
        }

        if (references.containsKey(referenceName)) {
            // Référence déjà disponible
            onResolved.accept(references.get(referenceName));
        } else {
            // Mettre en attente sans marquer comme "non résolue"
            pendingResolvers.computeIfAbsent(referenceName, k -> new ArrayList<>()).add(onResolved);
            // Ne pas ajouter à unresolvedReferences
        }
    }

    /**
     * Vérifie s'il reste des références non résolues.
     */
    public boolean hasUnresolvedReferences() {
        return !unresolvedReferences.isEmpty();
    }

    /**
     * Retourne les noms des références non résolues.
     */
    public Set<String> getUnresolvedReferences() {
        return new HashSet<>(unresolvedReferences);
    }

    /**
     * Obtient directement une référence si elle existe.
     */
    public <T> Optional<T> getReference(String referenceName, Class<T> type) {
        Object ref = references.get(referenceName);
        if (type.isInstance(ref)) {
            return Optional.of(type.cast(ref));
        }
        return Optional.empty();
    }

    /**
     * Obtient toutes les références d'un type donné.
     */
    public <T> List<T> getAllReferences(Class<T> type) {
        return references.values().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

}