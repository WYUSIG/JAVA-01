package io.sign.www.dao;

import io.sign.www.pojo.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDao {

    int insert(@Param("student") Student student);

    @Update("UPDATE student SET name = #{name} WHERE id = #{id}")
    int updateById(Student student);

    int deleteById(Integer id);

    @Select("SELECT * FROM student WHERE id = #{id}")
    Student findById(Integer id);
}
