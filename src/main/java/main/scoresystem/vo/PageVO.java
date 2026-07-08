package main.scoresystem.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageVO<T> {
    private long total;
    private long pages;
    private long current;
    private long size;
    private List<T> records;

    public static <T, E> PageVO<T> of(IPage<E> page, Function<E, T> converter) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotal(page.getTotal());
        vo.setPages(page.getPages());
        vo.setCurrent(page.getCurrent());
        vo.setSize(page.getSize());
        vo.setRecords(page.getRecords().stream().map(converter).collect(Collectors.toList()));
        return vo;
    }
}
