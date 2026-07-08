package main.scoresystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import main.scoresystem.entity.WeeklyReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeeklyReportMapper extends BaseMapper<WeeklyReport> {
}
