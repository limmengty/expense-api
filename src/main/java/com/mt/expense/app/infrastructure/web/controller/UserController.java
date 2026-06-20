package com.mt.expense.app.infrastructure.web.controller;

import com.mt.expense.app.application.port.out.UserRepositoryPort;
import com.mt.expense.app.infrastructure.web.dto.response.UserSearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User search for group member selection")
public class UserController {

    private static final int MAX_SEARCH_RESULTS = 10;
    private static final int MIN_QUERY_LENGTH = 2;

    private final UserRepositoryPort userRepository;

    public UserController(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Operation(
            summary = "Search users",
            description = "Find users by email or name for adding to a group")
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(
            @Parameter(description = "Search query (min 2 characters)") @RequestParam String q) {

        if (q == null || q.trim().length() < MIN_QUERY_LENGTH) {
            return ResponseEntity.ok(List.of());
        }

        List<UserSearchResult> results =
                userRepository.searchByEmailOrName(q.trim(), MAX_SEARCH_RESULTS).stream()
                        .map(UserSearchResult::from)
                        .toList();

        return ResponseEntity.ok(results);
    }
}
