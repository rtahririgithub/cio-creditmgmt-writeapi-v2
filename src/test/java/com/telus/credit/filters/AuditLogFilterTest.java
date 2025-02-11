package com.telus.credit.filters;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telus.credit.crypto.service.CryptoService;

@ExtendWith(MockitoExtension.class)
class AuditLogFilterTest {

    @Mock
    private CryptoService cryptoService;

    private ObjectMapper objectMapper;

    private AuditLogFilter underTest;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = new ObjectMapper();
        doAnswer((Answer<Object>) invocation -> {
            Object argument = invocation.getArgument(0);
            return argument != null ? "ENC: " + argument.toString() : null;
        }).when(cryptoService).encrypt(anyString());

        underTest = new AuditLogFilter(objectMapper, cryptoService);
    }
    /*
    @Test
    void testEncryptedFieldsIndividual() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.PATCH.name());
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("create-profile-request-individual-multi.json");
        request.setContent(IOUtils.toByteArray(inputStream));
        request.setContentType(JSON_CONTENT_TYPE);

        underTest.doFilter(request, Mockito.mock(ServletResponse.class), Mockito.mock(FilterChain.class));

        InputStream outputStream = this.getClass().getClassLoader().getResourceAsStream("audit-filter/profile-request-individual-multi.json");
        Map<String, Object> expect = objectMapper.readValue(outputStream, new TypeReference<Map<String, Object>>() {
        });
        assertEquals(objectMapper.writeValueAsString(expect), AuditService.auditContext().getInputRequest());
    }
    */
/*
    @Test
    void testEncryptedFieldsOrganization() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.PATCH.name());
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("create-profile-request-organization-multi.json");
        request.setContent(IOUtils.toByteArray(inputStream));
        request.setContentType(JSON_CONTENT_TYPE);

        underTest.doFilter(request, Mockito.mock(ServletResponse.class), Mockito.mock(FilterChain.class));

        InputStream outputStream = this.getClass().getClassLoader().getResourceAsStream("audit-filter/profile-request-organization-multi.json");
        Map<String, Object> expect = objectMapper.readValue(outputStream, new TypeReference<Map<String, Object>>() {
        });
        assertEquals(objectMapper.writeValueAsString(expect), AuditService.auditContext().getInputRequest());
    }
    */
}