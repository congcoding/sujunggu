package com.sujunggu.domain.board;

import com.sujunggu.domain.department.Department;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Getter
@NoArgsConstructor
@Entity
public class Board {

    @Id
    private int boardNo;

    private String name;
    private String address;

    @OneToOne
    @JoinColumn(name="address", insertable = false, updatable = false)
    private Department department;

    @Builder
    public Board(int boardNo, String name, String address) {
        this.boardNo = boardNo;
        this.name = name;
        this.address = address;
    }
}
