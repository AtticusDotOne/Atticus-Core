package one.atticus.core.services;

import one.atticus.core.config.AppConfig;
import one.atticus.core.resources.Contract;
import org.codehaus.plexus.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static one.atticus.core.services.PackageUtils.authenticate;
import static one.atticus.core.services.PackageUtils.prepareStatement;

/**
 * @author vgorin
 * file created on 12/6/18 2:08 PM
 */


@Service
@Path("/contract")
public class ContractService {
    private final JdbcTemplate jdbc;
    private final AppConfig queries;

    @Autowired
    public ContractService(JdbcTemplate jdbc, AppConfig queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public int create(@Context SecurityContext context, Contract contract) {
        int accountId = authenticate(context);

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(
                                queries.getQuery("create_contract"),
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setInt(1, accountId);
                        ps.setString(2, contract.memo);
                        ps.setString(3, contract.body);
                        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                        return ps;
                    },
                    keyHolder
            );

            // TODO: replace with 201 Created
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @GET
    @Path("/{contractId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Contract retrieve(@Context SecurityContext context, @PathParam("contractId") int contractId) {
        int accountId = authenticate(context);

        Contract contract = jdbc.query(
                c -> prepareStatement(c, queries.getQuery("get_contract"), contractId, accountId),
                PackageUtils::getContract
        );
        if(contract == null) {
            throw new NotFoundException("contract doesn't exist / deleted");
        }
        return contract;
    }

    @PUT
    @Path("/{contractId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public void update(@Context SecurityContext context, @PathParam("contractId") int contractId, Contract contract) {
        int accountId = authenticate(context);

        try {
            int rowsUpdated = jdbc.update(
                    c -> {
                        PreparedStatement ps = c.prepareStatement(queries.getQuery("update_contract"));
                        ps.setString(1, contract.memo);
                        ps.setString(2, contract.body);
                        ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(4, contractId);
                        ps.setInt(5, accountId);
                        return ps;
                    }
            );
            if(rowsUpdated == 0) {
                throw new NotFoundException("contract doesn't exist, deleted or is not editable (proposed)");
            }
        }
        catch(DuplicateKeyException e) {
            throw new ClientErrorException(ExceptionUtils.getRootCause(e).getMessage(), Response.Status.CONFLICT, e);
        }
    }

    @DELETE
    @Path("/{contractId}")
    public void delete(@Context SecurityContext context, @PathParam("contractId") int contractId, Contract contract) {
        int accountId = authenticate(context);
        int rowsUpdated = jdbc.update(c -> prepareStatement(c, queries.getQuery("delete_contract"), contractId, accountId));
        if(rowsUpdated == 0) {
            throw new NotFoundException("contract doesn't exist or already deleted");
        }
    }

    @GET
    @Path("/list")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<Contract> listContracts(@Context SecurityContext context, @QueryParam("type") String type) {
        int accountId = authenticate(context);

        if("draft".equals(type)) {
            return jdbc.query(
                    c -> prepareStatement(c, queries.getQuery("list_draft_contracts"), accountId),
                    PackageUtils::getContracts
            );
        }
        if("proposed".equals(type)) {
            return jdbc.query(
                    c -> prepareStatement(c, queries.getQuery("list_proposed_contracts"), accountId),
                    PackageUtils::getContracts
            );
        }
        return jdbc.query(
                c -> prepareStatement(c, queries.getQuery("list_contracts"), accountId),
                PackageUtils::getContracts
        );
    }

}
