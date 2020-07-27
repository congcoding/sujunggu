package com.sujunggu.domain.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Embeddable // 복합키 설정
@NoArgsConstructor
@AllArgsConstructor
public class PostPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private int boardNo;
    private int postNo;
}
