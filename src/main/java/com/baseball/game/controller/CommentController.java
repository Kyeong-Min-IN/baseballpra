package com.baseball.game.controller;

import com.baseball.game.dto.CommentDto;
import com.baseball.game.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

	private final CommentService service;

	@Autowired
	public CommentController(CommentService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<Void> createComment(@RequestBody CommentDto d) {
		service.comment(d);
		// 201 Created 반환
		return ResponseEntity.status(201).build();
	}

	@PutMapping("/{boardNo}/{commentId}")
	public ResponseEntity<Void> updateComment(
			@PathVariable("boardNo") int boardNo,
			@PathVariable("commentId") int commentId,
			@RequestBody CommentDto d) {
		service.modify(boardNo, commentId, d.getText());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{boardNo}/{commentId}")
	public ResponseEntity<Void> delComment(
			@PathVariable("boardNo") int boardNo,
			@PathVariable("commentId") int commentId) {
		service.delcom(boardNo, commentId);
		return ResponseEntity.ok().build();
	}
}
