package main.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import main.scoresystem.entity.Attendance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
}
