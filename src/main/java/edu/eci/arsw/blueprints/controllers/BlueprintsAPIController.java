package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.dto.ApiResult;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/v1/blueprints")
@Tag(
        name = "Blueprints",
        description = "Operaciones sobre blueprints"
)
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /blueprints
    @Operation(summary = "Get all blueprints")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Blueprints retrieved successfully"
            )
    })
    @GetMapping
    public ResponseEntity<ApiResult<Set<Blueprint>>> getAll() {
        return ResponseEntity.ok(
                new ApiResult<>(
                        200,
                        "execute ok",
                        services.getAllBlueprints()
                )
        );
    }

    // GET /blueprints/{author}
    @Operation(summary = "Get the blueprints by author")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Blueprints found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found"
            )
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResult<?>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok(
                    new ApiResult<>(
                            200,
                            "execute ok",
                            services.getBlueprintsByAuthor(author)
                    )
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new ApiResult<>(
                                    404,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(summary = "Get blueprint by author and name")
    @GetMapping("/{author}/{bpname}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Blueprint found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Blueprint not found"
            )
    })
    public ResponseEntity<ApiResult<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(
                    new ApiResult<>(
                            200,
                            "execute ok",
                            services.getBlueprint(author, bpname)
                    )
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(
                            new ApiResult<>(
                                    404,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    // POST /blueprints
    @Operation(summary = "Create blueprint")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Blueprint created"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            )
    })
    @PostMapping
    public ResponseEntity<ApiResult<?>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(
                            new ApiResult<>(
                                    201,
                                    "Blueprint created",
                                    bp
                            )
            );
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.badRequest()
                    .body(
                            new ApiResult<>(
                                    400,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(summary = "Add point to blueprint")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Point added"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Blueprint not found"
            )
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResult<?>> addPoint(@PathVariable String author, @PathVariable String bpname, @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(
                            new ApiResult<>(
                                    202,
                                    "Point added",
                                    services.getBlueprint(author, bpname)
                            )
            );
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).
                    body(
                            new ApiResult<>(
                                    404,
                                    e.getMessage(),
                                    null
                            )
                    );
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) { }
}
