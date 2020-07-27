package com.sujunggu.domain.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {

    @Query("SELECT b FROM Board b")
    List<Board> findAll();

    Board findOneByBoardNo(int boardNo);
}
