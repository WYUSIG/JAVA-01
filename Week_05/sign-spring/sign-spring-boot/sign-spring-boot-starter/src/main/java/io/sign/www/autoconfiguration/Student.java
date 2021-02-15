package io.sign.www.autoconfiguration;


import lombok.*;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class Student implements Serializable {
    
    private int id;
    private String name = "@Component方式";
}
