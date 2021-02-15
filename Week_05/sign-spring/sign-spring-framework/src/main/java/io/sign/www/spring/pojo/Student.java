package io.sign.www.spring.pojo;


import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Component
public class Student implements Serializable {
    
    private int id;
    private String name = "@Component方式";
}
