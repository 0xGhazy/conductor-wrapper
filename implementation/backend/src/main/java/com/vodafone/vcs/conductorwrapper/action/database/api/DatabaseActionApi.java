package com.vodafone.vcs.conductorwrapper.action.database.api;

import com.vodafone.vcs.conductorwrapper.action.database.dto.Query;
import com.vodafone.vcs.conductorwrapper.action.database.dto.QueryResult;
import com.vodafone.vcs.conductorwrapper.action.database.entity.QueryStore;
import com.vodafone.vcs.conductorwrapper.action.database.enums.ErrorCode;
import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryExecStatus;
import com.vodafone.vcs.conductorwrapper.action.database.enums.QueryType;
import com.vodafone.vcs.conductorwrapper.action.database.service.DatasourceService;
import com.vodafone.vcs.conductorwrapper.action.database.service.QueryService;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.*;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.SQLTimeoutException;
import java.util.Map;

@Log4j2
@Component
public class DatabaseActionApi {

    @Autowired private DatasourceService datasourceService;
    @Autowired private QueryService queryService;

    @Transactional
    public QueryResult run(Query query) {
        log.info("Attempting to execute query: {}", query);
        String queryId = query.getId();
        QueryResult out = QueryResult.builder().queryId(queryId).status(QueryExecStatus.FAILED).build();

        QueryStore fetchedQuery = queryService.readFromCache(queryId);
        if (fetchedQuery == null) {
            out.setErrorCode(ErrorCode.QUERY_NOT_FOUND);
            return out;
        }
        log.debug("Query [{}] fetched successfully", queryId);

        Map<String, Object> params = query.getParams();
        QueryType queryType = fetchedQuery.getQueryType();
        String datasource = fetchedQuery.getDataSource();
        NamedParameterJdbcTemplate template = datasourceService.getJdbcTemplate(datasource);
        log.debug("Datasource JDBC template acquired successfully");

        String sql = fetchedQuery.getSqlQuery().trim();

        // apply per-call timeout (seconds); reset afterward to avoid cross-thread bleed
        int prevTimeout = template.getJdbcTemplate().getQueryTimeout();
        int timeoutSec = fetchedQuery.getTimeoutSeconds();
        if (timeoutSec > 0) {
            template.getJdbcTemplate().setQueryTimeout(timeoutSec);
            out.setTimeout(timeoutSec);
        }

        String queryContext = String.format("queryId=%s datasource=%s params=%s", queryId, datasource, params);
        boolean errorOccurFlag = true;

        long start = System.currentTimeMillis();
        try {
            if (QueryType.SELECT.equals(queryType)) {
                var rs = template.queryForList(sql, params);
                out.setResultSet(rs);
                out.setSize(rs.size());
            } else {
                int affected = template.update(sql, new MapSqlParameterSource(params));
                out.setAffectedRowsCount(affected);
            }
            out.setStatus(QueryExecStatus.SUCCESS);
            log.debug("Resultset: {}", out);
            errorOccurFlag = false;
            return out;

        } catch (QueryTimeoutException e) {
            log.error("Query timeout on queryId={} datasource={}, params={}. Execution exceeded {} seconds.",
                    queryId, datasource, params, timeoutSec);
            out.setErrorCode(ErrorCode.QUERY_TIMEOUT);
            return out;
        }
        catch (CannotGetJdbcConnectionException | TransientDataAccessResourceException e) {
            log.error("Application cannot establish or maintain a connection with datasource: {}", datasource, e);
            out.setErrorCode(ErrorCode.CONNECTION_FAILURE);
            return out;
        }
        catch (DuplicateKeyException e) {
            log.error("Duplicate key violation on {}. Insert/update attempted to insert a primary key or unique constraint value that already exists.", queryContext, e);
            out.setErrorCode(ErrorCode.DUPLICATE_KEY_VIOLATION);
            return out;
        }
        catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation on {}. Insert/update operation violated a database constraint (e.g., foreign key, NOT NULL, or check constraint).", queryContext, e);
            out.setErrorCode(ErrorCode.INTEGRITY_VIOLATION);
            return out;
        }
        catch (BadSqlGrammarException e) {
            log.error("SQL syntax error on {}. Invalid SQL statement or object reference.", queryContext, e);
            out.setErrorCode(ErrorCode.SYNTAX_ERROR);
            return out;
        }
        catch (IncorrectResultSizeDataAccessException e) {
            log.error(e);
            log.error("Result size mismatch on {}. Query returned an unexpected number of rows.", queryContext, e);
            out.setErrorCode(ErrorCode.RESULT_SIZE_MISMATCH);
            return out;
        }
        catch (PermissionDeniedDataAccessException e) {
            log.error("Permission denied on {}. Database user lacks required privileges.", queryContext, e);
            out.setErrorCode(ErrorCode.PERMISSION_DENIED);
            return out;
        }
        catch (UncategorizedDataAccessException e) {
            if (e.getCause() instanceof SQLTimeoutException) {
                log.error("SQL timeout detected on queryId={} datasource={} params={}.",
                        queryId, datasource, params, e);
                out.setErrorCode(ErrorCode.QUERY_TIMEOUT);
            } else {
                log.error("Uncategorized data access error ...", e);
                out.setErrorCode(ErrorCode.UNCLASSIFIED_DAO);
            }
            return out;
        }
        catch (DataAccessException e) {
            log.error("Generic data access error on {}.", queryContext, e);
            out.setErrorCode(ErrorCode.DATA_ACCESS_ERROR);
            return out;
        }
        finally {
            if (timeoutSec > 0) {
                if (prevTimeout <= 0)
                    template.getJdbcTemplate().setQueryTimeout(-1);
                else
                    template.getJdbcTemplate().setQueryTimeout(prevTimeout);
            }
            if (!errorOccurFlag){
                long elapsedMs = (System.currentTimeMillis() - start);
                log.info("Query {} executed in {} ms", queryId, elapsedMs);
            }
        }
    }
}
