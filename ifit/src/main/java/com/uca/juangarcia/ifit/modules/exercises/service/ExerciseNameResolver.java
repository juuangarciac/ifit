package com.uca.juangarcia.ifit.modules.exercises.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseNameRef;
import com.uca.juangarcia.ifit.modules.exercises.repository.ExerciseCatalogRepository;

/**
 * Reconcilia los nombres de ejercicio generados por la IA con los nombres
 * reales del catálogo ({@code exercise_catalog}, importado del CSV de Ronnie).
 *
 * <p>El modelo de IA y el catálogo usan vocabularios distintos: el LLM produce
 * nombres curados ("Dominadas", "Peso muerto") que no siempre existen literalmente
 * en el catálogo ("Dominadas con peso", "Peso muerto con barra"). Sin esta capa,
 * el usuario no encuentra en el catálogo los ejercicios de su rutina.
 *
 * <p>Estrategia de coincidencia, en orden (gana la primera con confianza):
 * <ol>
 *   <li>Coincidencia exacta (ignorando mayúsculas).</li>
 *   <li>Coincidencia exacta tras normalizar (sin acentos, sin paréntesis,
 *       minúsculas, espacios colapsados).</li>
 *   <li>Prefijo: el nombre de la IA es prefijo de uno del catálogo, o viceversa
 *       (se elige el más corto / más parecido en número de palabras).</li>
 *   <li>Solapamiento de tokens (Jaccard) por encima de un umbral y compartiendo
 *       la palabra principal.</li>
 * </ol>
 * Si nada supera el umbral, no se fuerza ninguna coincidencia.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Service
public class ExerciseNameResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseNameResolver.class);

    /** Palabras vacías que no aportan a la identidad del ejercicio. */
    private static final Set<String> STOPWORDS = Set.of(
            "con", "de", "del", "la", "el", "los", "las", "en", "a", "y", "o",
            "un", "una", "sin", "para", "por", "al", "su", "sus");

    /** Umbral mínimo de solapamiento de tokens para aceptar una coincidencia difusa. */
    private static final double MIN_TOKEN_SCORE = 0.5;

    private final ExerciseCatalogRepository repository;

    /** Índice cacheado del catálogo. Carga perezosa y thread-safe (double-checked). */
    private volatile List<IndexedExercise> index;

    public ExerciseNameResolver(ExerciseCatalogRepository repository) {
        this.repository = repository;
    }

    /**
     * Resultado de una reconciliación con éxito.
     *
     * @param id            id del ejercicio en el catálogo
     * @param canonicalName nombre canónico tal y como está en el catálogo
     */
    public record Match(Long id, String canonicalName) {
    }

    /** Entrada del índice con sus formas precomputadas para acelerar la comparación. */
    private record IndexedExercise(Long id, String original, String normalized, Set<String> tokens) {
    }

    /**
     * Busca el ejercicio del catálogo que mejor corresponde a un nombre crudo.
     *
     * @param rawName nombre tal y como lo devolvió la IA
     * @return el {@link Match} canónico si hay coincidencia con confianza; vacío si no
     */
    public Optional<Match> resolve(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return Optional.empty();
        }

        List<IndexedExercise> catalog = ensureIndex();
        if (catalog.isEmpty()) {
            return Optional.empty();
        }

        String trimmed = rawName.trim();
        String norm = normalize(trimmed);
        Set<String> rawTokens = tokenize(norm);
        if (rawTokens.isEmpty()) {
            return Optional.empty();
        }

        // 1. Exacta (ignorando mayúsculas).
        for (IndexedExercise e : catalog) {
            if (e.original().equalsIgnoreCase(trimmed)) {
                return Optional.of(new Match(e.id(), e.original()));
            }
        }

        // 2. Exacta tras normalizar.
        for (IndexedExercise e : catalog) {
            if (e.normalized().equals(norm)) {
                return Optional.of(new Match(e.id(), e.original()));
            }
        }

        // 3. Prefijo (en cualquier dirección). Preferimos el candidato con menos
        //    palabras "sobrantes" respecto al nombre de la IA.
        IndexedExercise bestPrefix = null;
        int bestPrefixGap = Integer.MAX_VALUE;
        for (IndexedExercise e : catalog) {
            boolean prefix = e.normalized().startsWith(norm + " ")
                    || norm.startsWith(e.normalized() + " ");
            if (prefix) {
                int gap = Math.abs(e.tokens().size() - rawTokens.size());
                if (gap < bestPrefixGap) {
                    bestPrefixGap = gap;
                    bestPrefix = e;
                }
            }
        }
        if (bestPrefix != null) {
            return Optional.of(new Match(bestPrefix.id(), bestPrefix.original()));
        }

        // 4. Solapamiento de tokens (Jaccard) compartiendo la palabra principal.
        String head = head(norm);
        IndexedExercise bestFuzzy = null;
        double bestScore = 0.0;
        for (IndexedExercise e : catalog) {
            if (head != null && !head.equals(head(e.normalized()))) {
                continue;
            }
            double score = jaccard(rawTokens, e.tokens());
            if (score > bestScore) {
                bestScore = score;
                bestFuzzy = e;
            }
        }
        if (bestFuzzy != null && bestScore >= MIN_TOKEN_SCORE) {
            return Optional.of(new Match(bestFuzzy.id(), bestFuzzy.original()));
        }

        return Optional.empty();
    }

    // -------------------------------------------------------------------------
    // Índice
    // -------------------------------------------------------------------------

    private List<IndexedExercise> ensureIndex() {
        List<IndexedExercise> local = index;
        if (local == null) {
            synchronized (this) {
                local = index;
                if (local == null) {
                    local = buildIndex();
                    index = local;
                }
            }
        }
        return local;
    }

    private List<IndexedExercise> buildIndex() {
        List<ExerciseNameRef> refs = repository.findAllNameRefs();
        List<IndexedExercise> built = new ArrayList<>(refs.size());
        for (ExerciseNameRef ref : refs) {
            if (ref.name() == null || ref.name().isBlank()) {
                continue;
            }
            String original = ref.name().trim();
            String norm = normalize(original);
            built.add(new IndexedExercise(ref.id(), original, norm, tokenize(norm)));
        }
        logger.info("ExerciseNameResolver index built with {} catalog entries", built.size());
        return built;
    }

    // -------------------------------------------------------------------------
    // Utilidades de normalización
    // -------------------------------------------------------------------------

    /**
     * Normaliza un nombre: minúsculas, sin acentos, sin contenido entre paréntesis
     * y con la puntuación convertida en espacios.
     */
    static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");          // quita marcas diacríticas (acentos, ñ→n)
        n = n.toLowerCase(Locale.ROOT);
        n = n.replaceAll("\\(.*?\\)", " ");          // elimina descriptores entre paréntesis
        n = n.replaceAll("[^a-z0-9 ]", " ");         // puntuación → espacio
        n = n.replaceAll("\\s+", " ").trim();
        return n;
    }

    private static Set<String> tokenize(String normalized) {
        Set<String> tokens = new HashSet<>();
        for (String token : normalized.split(" ")) {
            if (!token.isBlank() && !STOPWORDS.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /** Primera palabra significativa (no stopword) del nombre normalizado. */
    private static String head(String normalized) {
        return Arrays.stream(normalized.split(" "))
                .filter(t -> !t.isBlank() && !STOPWORDS.contains(t))
                .findFirst()
                .orElse(null);
    }

    private static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Set<String> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return (double) intersection.size() / union.size();
    }
}
