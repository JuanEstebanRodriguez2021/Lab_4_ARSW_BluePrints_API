package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final JdbcTemplate jdbc;

    public PostgresBlueprintPersistence(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveBlueprint(Blueprint bp)
            throws BlueprintPersistenceException {

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM blueprint
                WHERE author = ?
                  AND name = ?
                """,
                Integer.class,
                bp.getAuthor(),
                bp.getName()
        );

        String key = bp.getAuthor() + ":" + bp.getName();

        if (count != null && count > 0) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: " + key
            );
        }

        jdbc.update(
                """
                INSERT INTO blueprint(author, name)
                VALUES (?, ?)
                """,
                bp.getAuthor(),
                bp.getName()
        );

        for (Point p : bp.getPoints()) {
            jdbc.update(
                    """
                    INSERT INTO point
                    (author, blueprint_name, x, y)
                    VALUES (?, ?, ?, ?)
                    """,
                    bp.getAuthor(),
                    bp.getName(),
                    p.x(),
                    p.y()
            );
        }
    }

    @Override
    public Blueprint getBlueprint(
            String author,
            String name)
            throws BlueprintNotFoundException {

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM blueprint
                WHERE author = ?
                  AND name = ?
                """,
                Integer.class,
                author,
                name
        );

        if (count == null || count == 0) {
            throw new BlueprintNotFoundException(
                    "Blueprint not found: "
                            + author
                            + "/"
                            + name
            );
        }

        List<Point> points = jdbc.query(
                """
                SELECT x, y
                FROM point
                WHERE author = ?
                  AND blueprint_name = ?
                ORDER BY id
                """,
                (rs, rowNum) ->
                        new Point(
                                rs.getInt("x"),
                                rs.getInt("y")
                        ),
                author,
                name
        );

        return new Blueprint(
                author,
                name,
                points
        );
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(
            String author)
            throws BlueprintNotFoundException {

        List<String> names = jdbc.query(
                """
                SELECT name
                FROM blueprint
                WHERE author = ?
                """,
                (rs, rowNum) ->
                        rs.getString("name"),
                author
        );

        if (names.isEmpty()) {
            throw new BlueprintNotFoundException(
                    "No blueprints for author: " + author
            );
        }

        Set<Blueprint> result = new HashSet<>();

        for (String name : names) {
            result.add(
                    getBlueprint(author, name)
            );
        }

        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {

        List<String[]> rows = jdbc.query(
                """
                SELECT author, name
                FROM blueprint
                """,
                (rs, rowNum) ->
                        new String[]{
                                rs.getString("author"),
                                rs.getString("name")
                        }
        );

        Set<Blueprint> result = new HashSet<>();

        for (String[] row : rows) {
            try {
                result.add(
                        getBlueprint(
                                row[0],
                                row[1]
                        )
                );
            } catch (BlueprintNotFoundException ignored) {
            }
        }

        return result;
    }

    @Override
    public void addPoint(
            String author,
            String name,
            int x,
            int y)
            throws BlueprintNotFoundException {

        getBlueprint(author, name);

        jdbc.update(
                """
                INSERT INTO point
                (author, blueprint_name, x, y)
                VALUES (?, ?, ?, ?)
                """,
                author,
                name,
                x,
                y
        );
    }
}