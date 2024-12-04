import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExceptionProcessorTest {

    @InjectMocks
    private ExceptionProcessor exceptionProcessor; // Classe que estamos testando

    @Mock
    private ArsenalDefaultExceptionHandler handler; // Mock do handler usado na classe

    @Mock
    private Exchange exchange; // Mock do Camel Exchange

    @Mock
    private Message message; // Mock do Camel Message

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configurar mocks para o Exchange e Message
        when(exchange.getIn()).thenReturn(message);
    }

    @Test
    void testProcessWithBusinessException() throws Exception {
        // Simular propriedades do Exchange
        BusinessException businessException = new BusinessException("ERROR_CODE", "Detalhes do erro");
        SimpleEntity simpleEntity = new SimpleEntity();
        simpleEntity.setCode(1);

        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, BusinessException.class)).thenReturn(businessException);
        when(exchange.getProperty("simpleEntity", SimpleEntity.class)).thenReturn(simpleEntity);

        // Simular retorno do handler
        List<ArsenalErrorCode> errorCodes = List.of(new ArsenalErrorCode("App", 1, simpleEntity, simpleEntity, simpleEntity, simpleEntity, simpleEntity));
        when(handler.handleExceptions(businessException, simpleEntity)).thenReturn(errorCodes);

        ApiErrorResponseDTO responseDTO = new ApiErrorResponseDTO("Erro tratado", errorCodes);
        when(handler.buildResponse(errorCodes)).thenReturn(responseDTO);

        // Executar o processador
        exceptionProcessor.process(exchange);

        // Verificar se o corpo da resposta foi configurado corretamente
        verify(message).setBody(responseDTO);
        assertEquals(responseDTO, message.getBody(ApiErrorResponseDTO.class));
        assertEquals("Erro tratado", responseDTO.getMessage());
        assertEquals(1, responseDTO.getErrors().size());
    }

    @Test
    void testProcessWithoutBusinessException() throws Exception {
        // Caso o Exchange não contenha uma exceção
        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, BusinessException.class)).thenReturn(null);

        // Executar o processador
        exceptionProcessor.process(exchange);

        // Verificar se nenhum comportamento indevido ocorreu
        verify(handler, never()).handleExceptions(any(), any());
        verify(message, never()).setBody(any());
    }

    @Test
void testProcessWithBusinessException() throws Exception {
    // Simular BusinessException com apenas uma mensagem
    BusinessException businessException = new BusinessException("Erro de validação");
    when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, BusinessException.class)).thenReturn(businessException);

    // Executar o processador
    exceptionProcessor.process(exchange);

    // Capturar o corpo configurado no Exchange
    verify(message).setBody(any(ApiErrorResponseDTO.class));
    ApiErrorResponseDTO responseDTO = message.getBody(ApiErrorResponseDTO.class);

    // Validar o conteúdo do ApiErrorResponseDTO
    assertEquals("Erro tratado", responseDTO.getMessage());
    assertEquals(1, responseDTO.getErrors().size());

    // Validar o único erro no ApiErrorResponseDTO
    ErrorDetailDTO errorDetail = responseDTO.getErrors().get(0);
    assertEquals("Erro de validação", errorDetail.getMessage());
    assertEquals("ERROR", errorDetail.getLevel());
    assertEquals("Erro de validação ocorrido no processamento", errorDetail.getDescription());
}
}
