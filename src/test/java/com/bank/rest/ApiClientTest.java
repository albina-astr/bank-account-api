package com.bank.rest;

import com.bank.rest.dto.TopUpDto;
import com.bank.rest.dto.TransferMoneyDto;
import com.bank.rest.model.Account;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.net.httpserver.HttpServer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;

import static org.junit.Assert.*;

public class ApiClientTest {

    private HttpServer server;
    private Client client;
    private WebResource webResource;
    private ObjectMapper objectMapper;


    @Before
    public void setUp() throws IOException {
        client = Client.create();
        webResource = client.resource(getURI()).path("accounts");

        objectMapper = new ObjectMapper();

        server = createHttpServer();
        server.start();
    }

    @After
    public void tearDown() {
        server.stop(1);
    }

    @Test
    public void create_Test_Successful() throws Exception {
        String accountOwnerPathVariable = "Tom";

        ClientResponse response = webResource.path("create").path(accountOwnerPathVariable)
                .post(ClientResponse.class);
        String output = response.getEntity(String.class);

        assertEquals(200, response.getStatus());
        Account account = objectMapper.readValue(output, Account.class);
        assertEquals(accountOwnerPathVariable, account.getOwner());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertNotNull(account.getNumber());
        assertFalse(account.isDisabled());
    }

    @Test
    public void getInfo_Test_Successful() throws Exception {
        Account expected = createAccountFor("Tom");
        String accountNumber = String.valueOf(expected.getNumber());

        ClientResponse response = webResource.path(accountNumber).get(ClientResponse.class);
        String output = response.getEntity(String.class);

        Account result = objectMapper.readValue(output, Account.class);
        assertEquals(200, response.getStatus());
        compareAccounts(expected, result);
    }

    @Test
    public void getInfo_Test_ExceptionGivenAccountNumberDoesNotExist() {
        ClientResponse response = webResource.path(String.valueOf(1)).get(ClientResponse.class);
        String output = response.getEntity(String.class);

        assertEquals(500, response.getStatus());
        assertEquals("No account found with number 1", output);
    }

    @Test
    public void update_Test_Successful() throws Exception {
        Account expected = createAccountFor("Tom");
        expected.setOwner("Tommy");

        ClientResponse response = webResource.path("update").type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, objectMapper.writeValueAsString(expected));
        String output = response.getEntity(String.class);

        Account result = objectMapper.readValue(output, Account.class);
        assertEquals(200, response.getStatus());
        assertEquals(expected.getNumber(), result.getNumber());
        assertEquals(expected.getBalance(), result.getBalance());
        assertEquals("Tommy", expected.getOwner());
    }

    @Test
    public void update_Test_ExceptionGivenAccountNumberDoesNotExist() throws Exception {
        Account expected = createAccountFor("Tom");
        expected.setNumber(0L);
        expected.setOwner("Tommy");

        ClientResponse response = webResource.path("update").type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, objectMapper.writeValueAsString(expected));
        String output = response.getEntity(String.class);

        assertEquals(500, response.getStatus());
        assertEquals("Cannot update non existing account " + expected.getNumber(), output);
    }

    @Test
    public void update_Test_ExceptionNullAccountNumber() throws Exception {
        Account expected = createAccountFor("Tom");
        expected.setNumber(null);
        expected.setOwner("Tommy");

        ClientResponse response = webResource.path("update").type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, objectMapper.writeValueAsString(expected));
        String output = response.getEntity(String.class);

        assertEquals(500, response.getStatus());
        assertEquals("Null account number is not supported fot this operation", output);
    }

    @Test
    public void update_Test_ExceptionNegativeBalance() throws Exception {
        Account expected = createAccountFor("Tom");
        expected.setBalance(BigDecimal.valueOf(-1000));

        ClientResponse response = webResource.path("update").type(MediaType.APPLICATION_JSON_TYPE)
                .put(ClientResponse.class, objectMapper.writeValueAsString(expected));
        String output = response.getEntity(String.class);

        assertEquals(500, response.getStatus());
        assertEquals("Balance cannot be negative", output);
    }

    @Test
    public void getAllAccounts_Test_Successful() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");

        ClientResponse response = webResource.get(ClientResponse.class);
        String output = response.getEntity(String.class);
        List<Account> accounts = objectMapper.readValue(output, new TypeReference<List<Account>>() {
        });

        assertEquals(200, response.getStatus());
        Account aliceAccountFound = accounts.stream()
                .filter(account -> account.getNumber().equals(aliceAccount.getNumber()))
                .findFirst().get();
        Account bobAccountFound = accounts.stream()
                .filter(account -> account.getNumber().equals(bobAccount.getNumber()))
                .findFirst().get();
        compareAccounts(aliceAccount, aliceAccountFound);
        compareAccounts(bobAccount, bobAccountFound);
    }

    @Test
    public void topUp_Test_Successful() throws Exception {
        Account tomAccount = createAccountFor("Tom");
        TopUpDto dto = new TopUpDto(tomAccount.getNumber(), BigDecimal.valueOf(1000));

        ClientResponse topUpResponse = webResource.path("top_up").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));
        String output = topUpResponse.getEntity(String.class);

        assertEquals(200, topUpResponse.getStatus());
        Account result = objectMapper.readValue(output, Account.class);
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
    }

    @Test
    public void topUp_Test_ExceptionNullAccount() throws Exception {
        TopUpDto dto = new TopUpDto(null, BigDecimal.valueOf(1000));

        ClientResponse topUpResponse = webResource.path("top_up").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));
        String output = topUpResponse.getEntity(String.class);

        assertEquals("Null account number is not supported fot this operation", output);
    }

    @Test
    public void topUp_Test_AccountNotExist() throws Exception {
        long nonExistingAccountNumber = 1L;
        TopUpDto dto = new TopUpDto(nonExistingAccountNumber, BigDecimal.valueOf(1000));

        ClientResponse topUpResponse = webResource.path("top_up").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));
        String output = topUpResponse.getEntity(String.class);

        assertEquals("No account found with number " + nonExistingAccountNumber, output);
    }

    @Test
    public void topUp_Test_ExceptionDisabledAccount() throws Exception {
        Account tomAccount = createAccountFor("Tom");
        webResource.path("delete").path(tomAccount.getNumber().toString()).delete(ClientResponse.class);
        TopUpDto dto = new TopUpDto(tomAccount.getNumber(), BigDecimal.valueOf(1000));

        ClientResponse topUpResponse = webResource.path("top_up").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));
        String output = topUpResponse.getEntity(String.class);

        assertEquals("Could not execute top up on disabled account " + tomAccount.getNumber(), output);
    }

    @Test
    public void topUp_Test_ExceptionNegativeAmount() throws Exception {
        Account tomAccount = createAccountFor("Tom");
        webResource.path("delete").path(tomAccount.getNumber().toString()).delete(ClientResponse.class);
        TopUpDto dto = new TopUpDto(tomAccount.getNumber(), BigDecimal.valueOf(-1000));

        ClientResponse topUpResponse = webResource.path("top_up").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));
        String output = topUpResponse.getEntity(String.class);


        assertEquals("Could not execute top up on disabled account " + tomAccount.getNumber(), output);
    }

    @Test
    public void delete_Test_Successful() throws Exception {
        Account tomAccount = createAccountFor("Tom");

        ClientResponse response = webResource.path("delete").path(tomAccount.getNumber().toString()).delete(ClientResponse.class);

        assertEquals(204, response.getStatus());

        Account disabledAccount = findAccount(tomAccount.getNumber());
        assertTrue(disabledAccount.isDisabled());
        assertEquals(tomAccount.getNumber(), disabledAccount.getNumber());
    }


    @Test
    public void delete_Test_ExceptionAccountNotFound() {
        long nonExistingAccountNumber = 1L;
        ClientResponse response = webResource.path("delete").path(String.valueOf(nonExistingAccountNumber)).delete(ClientResponse.class);
        String output = response.getEntity(String.class);

        assertEquals(500, response.getStatus());
        assertEquals("No account found with number " + nonExistingAccountNumber, output);
    }

    @Test
    public void delete_Test_ExceptionAccountAlreadyDisabled() throws Exception {
        Account tomAccount = createAccountFor("Tom");

        webResource.path("delete").path(tomAccount.getNumber().toString()).delete(ClientResponse.class);
        ClientResponse deleteResponse = webResource.path("delete").path(tomAccount.getNumber().toString()).delete(ClientResponse.class);

        assertEquals(500, deleteResponse.getStatus());
        assertEquals("Cannot disable disabled account " + tomAccount.getNumber(), deleteResponse.getEntity(String.class));
    }

    @Test
    public void transfer_Test_TransferAllMoneySuccessful() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");
        aliceAccount = topUp(aliceAccount, BigDecimal.valueOf(1000));
        TransferMoneyDto dto = new TransferMoneyDto(aliceAccount.getNumber(), bobAccount.getNumber(), BigDecimal.valueOf(500));

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        assertEquals(204, transferResponse.getStatus());
        Account aliceAccountAfter = findAccount(aliceAccount.getNumber());
        Account bobAccountAfter = findAccount(bobAccount.getNumber());
        assertEquals(BigDecimal.valueOf(500), aliceAccountAfter.getBalance());
        assertEquals(BigDecimal.valueOf(500), bobAccountAfter.getBalance());
    }

    @Test
    public void transfer_Test_ExceptionDisabledAccountFrom() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");
        aliceAccount = topUp(aliceAccount, BigDecimal.valueOf(1000));
        deleteAccount(aliceAccount.getNumber());
        TransferMoneyDto dto = new TransferMoneyDto(aliceAccount.getNumber(), bobAccount.getNumber(), BigDecimal.valueOf(500));

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Could not execute write off from disabled account " + aliceAccount.getNumber(), output);
    }

    @Test
    public void transfer_Test_ExceptionDisabledAccountTo() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");
        aliceAccount = topUp(aliceAccount, BigDecimal.valueOf(1000));
        deleteAccount(bobAccount.getNumber());
        TransferMoneyDto dto = new TransferMoneyDto(aliceAccount.getNumber(), bobAccount.getNumber(), BigDecimal.valueOf(500));

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Could not execute top up on disabled account " + bobAccount.getNumber(), output);
    }

    @Test
    public void transfer_Test_ExceptionNotSufficientMoneyOnAccountFrom() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");
        TransferMoneyDto dto = new TransferMoneyDto(aliceAccount.getNumber(), bobAccount.getNumber(), BigDecimal.valueOf(500));

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Not sufficient funds for write off on account " + aliceAccount.getNumber(), output);
    }

    @Test
    public void transfer_Test_ExceptionNegativeTransferAmount() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");
        TransferMoneyDto dto = new TransferMoneyDto(aliceAccount.getNumber(), bobAccount.getNumber(), BigDecimal.valueOf(-500));

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Write off amount cannot be zero or negative. Account " + aliceAccount.getNumber(), output);
    }

    @Test
    public void transfer_Test_ExceptionZeroTransferAmount() throws Exception {
        Account aliceAccount = createAccountFor("Alice");
        Account bobAccount = createAccountFor("Bob");
        TransferMoneyDto dto = new TransferMoneyDto(aliceAccount.getNumber(), bobAccount.getNumber(), BigDecimal.ZERO);

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Write off amount cannot be zero or negative. Account " + aliceAccount.getNumber(), output);
    }

    @Test
    public void transfer_Test_ExceptionNullAccountFrom() throws Exception {
        Account bobAccount = createAccountFor("Bob");
        TransferMoneyDto dto = new TransferMoneyDto(null, bobAccount.getNumber(), BigDecimal.ZERO);

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Null account number is not supported fot this operation", output);
    }

    @Test
    public void transfer_Test_ExceptionNullAccountTo() throws Exception {
        Account bobAccount = createAccountFor("Bob");
        TransferMoneyDto dto = new TransferMoneyDto(bobAccount.getNumber(), null, BigDecimal.ZERO);

        ClientResponse transferResponse = webResource.path("transfer").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto));

        String output = transferResponse.getEntity(String.class);
        assertEquals(500, transferResponse.getStatus());
        assertEquals("Null account number is not supported fot this operation", output);
    }


    private HttpServer createHttpServer() throws IOException {
        ResourceConfig resourceConfig = new PackagesResourceConfig("com.bank.rest");
        resourceConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        return HttpServerFactory.create(getURI(), resourceConfig);
    }

    private static URI getURI() {
        return UriBuilder.fromUri("http://" + getHostName() + "/").port(8085).build();
    }

    private static String getHostName() {
        String hostName = "localhost";
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostName;
    }

    private Account createAccountFor(String owner) throws IOException {
        ClientResponse response = webResource.path("create").path(owner).post(ClientResponse.class);
        String output = response.getEntity(String.class);

        return objectMapper.readValue(output, Account.class);
    }

    private void deleteAccount(Long accountNumber) {
        webResource.path("delete").path(accountNumber.toString()).delete(ClientResponse.class);
    }

    private Account topUp(Account account, BigDecimal amount) throws Exception {
        TopUpDto dto = new TopUpDto(account.getNumber(), amount);

        String output = webResource.path("top_up").type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, objectMapper.writeValueAsString(dto)).getEntity(String.class);

        return objectMapper.readValue(output, Account.class);
    }

    private Account findAccount(Long accountNumber) throws IOException {
        ClientResponse getInfoResponse = webResource.path(accountNumber.toString()).get(ClientResponse.class);
        String output = getInfoResponse.getEntity(String.class);
        return objectMapper.readValue(output, Account.class);
    }

    private void compareAccounts(Account expected, Account result) {
        assertEquals(expected.getNumber(), result.getNumber());
        assertEquals(expected.getBalance(), result.getBalance());
        assertEquals(expected.getOwner(), result.getOwner());
        assertEquals(expected.isDisabled(), result.isDisabled());
    }

}