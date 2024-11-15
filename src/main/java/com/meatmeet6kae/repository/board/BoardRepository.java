package com.meatmeet6kae.repository.board;

import com.meatmeet6kae.entity.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
    List<Board>findByBoardCategory(String boardCategory);
}
