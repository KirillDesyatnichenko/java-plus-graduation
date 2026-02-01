package ru.yandex.practicum.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.ViewStatsDto;
import ru.yandex.practicum.model.EndpointHit;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StatsRepositoryImpl implements StatsRepository {
    private static final RowMapper<EndpointHit> HIT_ROW_MAPPER = (rs, rowNum) ->
            EndpointHit.builder()
                    .id(rs.getLong("id"))
                    .app(rs.getString("app"))
                    .uri(rs.getString("uri"))
                    .ip(rs.getString("ip"))
                    .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
                    .build();
    private static final RowMapper<ViewStatsDto> STATS_ROW_MAPPER = (rs, rowNum) ->
            ViewStatsDto.builder()
                    .app(rs.getString("app"))
                    .uri(rs.getString("uri"))
                    .hits(rs.getLong("count"))
                    .build();
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public EndpointHit save(EndpointHit hit) {
        String sql = "INSERT INTO hits (app, uri, ip, timestamp) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, hit.getApp());
            ps.setString(2, hit.getUri());
            ps.setString(3, hit.getIp());
            ps.setTimestamp(4, Timestamp.valueOf(hit.getTimestamp()));
            return ps;
        }, keyHolder);

        hit.setId(keyHolder.getKey().longValue());
        return hit;
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        StringBuilder sqlBuilder = new StringBuilder();
        if (unique) {
            sqlBuilder.append("select app, uri, count(distinct ip) as count");
        } else {
            sqlBuilder.append("select app, uri, count(ip) as count");
        }

        sqlBuilder.append(" from hits where timestamp between :start and :end");

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start", start)
                .addValue("end", end);

        if (uris != null && !uris.isEmpty()) {
            sqlBuilder.append(" and uri in (:uris)");
            params.addValue("uris", uris);
        }

        sqlBuilder.append(" group by app, uri order by count desc");


        return namedParameterJdbcTemplate.query(sqlBuilder.toString(), params, STATS_ROW_MAPPER);
    }
}