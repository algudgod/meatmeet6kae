package com.meatmeet6kae.service.comment;

import com.meatmeet6kae.dto.comment.CommentDto;
import com.meatmeet6kae.entity.board.Board;
import com.meatmeet6kae.entity.comment.Comment;
import com.meatmeet6kae.entity.user.User;
import com.meatmeet6kae.repository.comment.CommentRepository;
import com.meatmeet6kae.service.board.BoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/* 역할: 핵심 비즈니스 로직을 수행 */
@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final BoardService boardService;

    public CommentService(CommentRepository commentRepository, BoardService boardService) {
        this.commentRepository = commentRepository;
        this.boardService = boardService;
    }

    // 댓글 추가(생성)
    public CommentDto addComment(CommentDto commentDto, User user){

        Board board = boardService.getBoardByBoardNo(commentDto.getBoardNo());

        Comment comment = new Comment();
        comment.setBoard(board);
        comment.setUser(user);
        comment.setContent(commentDto.getContent());
        comment.setCreateDate(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        // 저장된 엔티티를 그대로 반환하면 보안상 위험이 생길 수 있어 Dto로 변환 후 반환
        CommentDto savedCommentDto = new CommentDto();
        savedCommentDto.setCommentNo(savedComment.getCommentNo());
        savedCommentDto.setContent(savedComment.getContent());
        savedCommentDto.setBoardNo(savedComment.getBoard().getBoardNo());
        savedCommentDto.setNickname(savedComment.getUser().getNickname());
        savedCommentDto.setCreateDate(savedComment.getCreateDate());

        return savedCommentDto;

    }

    // 댓글 목록: 댓글 목록을 특정 게시글 번호로 조회, DTO로 변환하여 반환
    public List<CommentDto> getCommentsListByBoardNo(int boardNo){
        List<Comment> comments = commentRepository.findByBoard_BoardNo(boardNo);
        List<CommentDto> commentDtos = new ArrayList<>();
        for(Comment comment : comments) {
            CommentDto dto = new CommentDto();
            dto.setCommentNo(comment.getCommentNo());
            dto.setContent(comment.getContent());
            dto.setBoardNo(comment.getBoard().getBoardNo());
            dto.setNickname(comment.getUser().getNickname());
            dto.setCreateDate(comment.getCreateDate());

            commentDtos.add(dto);
        }
        return commentDtos;
    }


    // 댓글 수정
    public CommentDto updateComment(CommentDto commentDto, User user){

        // 1. 유효성 검증
        // 1-1. 댓글 번호로 특정 댓글을 조회하고 null check
        Comment comment = commentRepository.findById(commentDto.getCommentNo())
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 1-2 댓글의 작성자와 수정자의 검증
        if(!comment.getUser().getLoginId().equals(user.getLoginId())) {
            throw new IllegalArgumentException("작성자가 아니므로 수정할 수 없습니다.");
        }

        // 댓글 내용 수정, updateDate 기록
        comment.setContent(commentDto.getContent());
        comment.setUpdateDate(LocalDateTime.now());

        // 수정된 댓글 내용 저장
        Comment updatedComment = commentRepository.save(comment);

        // 엔티티를 Dto로 변환하여 반환
        CommentDto updatedCommentDto = new CommentDto();
        updatedCommentDto.setCommentNo(updatedComment.getCommentNo());
        updatedCommentDto.setContent(updatedComment.getContent());
        updatedCommentDto.setBoardNo(updatedCommentDto.getBoardNo());
        updatedCommentDto.setNickname(updatedCommentDto.getNickname());
        updatedCommentDto.setUpdateDate(updatedComment.getUpdateDate());

        return updatedCommentDto;
    }

    // 댓글 삭제
    public void deleteComment(int commentNo, User user){
    // 1. 유효성 검증
    // 1-1. 댓글 번호로 특정 댓글을 조회하고 null check
        Comment comment = commentRepository.findById(commentNo)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
        // 1-2 댓글의 작성자와 수정자의 검증
        if(!comment.getUser().getLoginId().equals(user.getLoginId())){
            throw new IllegalArgumentException("작성자가 아니므로 삭제할 수 없습니다.");
        }

        commentRepository.delete(comment);

    }

}
