package main.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import main.scoresystem.entity.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
