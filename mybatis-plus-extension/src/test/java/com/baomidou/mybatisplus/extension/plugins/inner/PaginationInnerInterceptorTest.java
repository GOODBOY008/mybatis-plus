package com.baomidou.mybatisplus.extension.plugins.inner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author miemie
 * @since 2020-06-28
 */
class PaginationInnerInterceptorTest {

    private final PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();

    @Test
    void optimizeCount() {
        /* 能进行优化的 SQL */
        asserts("select * from user u LEFT JOIN role r ON r.id = u.role_id",
            "SELECT COUNT(1) FROM user u");

        asserts("select * from user u LEFT JOIN role r ON r.id = u.role_id WHERE u.xx = ?",
            "SELECT COUNT(1) FROM user u WHERE u.xx = ?");

        asserts("select * from user u LEFT JOIN role r ON r.id = u.role_id LEFT JOIN permission p on p.id = u.per_id",
            "SELECT COUNT(1) FROM user u");

        asserts("select * from user u LEFT JOIN role r ON r.id = u.role_id LEFT JOIN permission p on p.id = u.per_id WHERE u.xx = ?",
            "SELECT COUNT(1) FROM user u WHERE u.xx = ?");
    }

    @Test
    void notOptimizeCount() {
        /* 不能进行优化的 SQL */
        asserts("select * from user u LEFT JOIN role r ON r.id = u.role_id AND r.name = ? where u.xx = ?",
            "SELECT COUNT(1) FROM user u LEFT JOIN role r ON r.id = u.role_id AND r.name = ? WHERE u.xx = ?");

        asserts("select * from user u LEFT JOIN role r ON r.id = u.role_id WHERE u.xax = ? AND r.cc = ? AND r.qq = ?",
            "SELECT COUNT(1) FROM user u LEFT JOIN role r ON r.id = u.role_id WHERE u.xax = ? AND r.cc = ? AND r.qq = ?");
    }

    @Test
    void optimizeCountOrderBy() {
        /* order by 里不带参数,去除order by */
        asserts("SELECT * FROM comment ORDER BY name",
            "SELECT COUNT(1) FROM comment");

        /* order by 里带参数,不去除order by */
        asserts("SELECT * FROM comment ORDER BY (CASE WHEN creator = ? THEN 0 ELSE 1 END)",
            "SELECT COUNT(1) FROM comment ORDER BY (CASE WHEN creator = ? THEN 0 ELSE 1 END)");
    }

    void asserts(String sql, String targetSql) {
        assertThat(interceptor.autoCountSql(true, sql)).isEqualTo(targetSql);
    }
}
