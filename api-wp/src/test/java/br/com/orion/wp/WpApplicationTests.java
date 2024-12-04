import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

class LoggingAspectUtilsTest {

    private LoggingAspectUtils loggingAspectUtils;

    @Mock
    private Logger log;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loggingAspectUtils = new LoggingAspectUtils();

        // Mockando o comportamento do Exchange e Message
        when(exchange.getIn()).thenReturn(message);
        when(message.getHeader("x-traceId")).thenReturn("12345");
        when(message.getHeaders()).thenReturn(Map.of("key", "value"));
        when(message.getBody(Object.class)).thenReturn("Body Content");
        when(message.getHeader(Exchange.HTTP_QUERY)).thenReturn("param=value");
        when(exchange.getProperties()).thenReturn(Map.of("propertyKey", "propertyValue"));

        // Mockando JoinPoint
        when(joinPoint.getSignature().getDeclaringTypeName()).thenReturn("com.santander.chk_int.processor.SampleProcessor");
    }

    @Test
    void testLogBeforeMethodExecution() {
        // Mockando a chamada estática do logger
        Logger staticLogger = mock(Logger.class);
        LoggingAspectUtils.log = staticLogger;

        // Executar o método
        loggingAspectUtils.logBeforeMethodExecution(joinPoint, exchange);

        // Verificar se o log foi chamado
        verify(staticLogger).info(contains("Process started from class"),
                eq("com.santander.chk_int.processor.SampleProcessor"),
                any(),
                eq("12345"),
                eq(Map.of("key", "value")),
                eq("Body Content"),
                eq("param=value"),
                eq(Map.of("propertyKey", "propertyValue")));
    }

    @Test
    void testLogAfter() {
        // Mockando a chamada estática do logger
        Logger staticLogger = mock(Logger.class);
        LoggingAspectUtils.log = staticLogger;

        // Executar o método
        loggingAspectUtils.logAfter(joinPoint, exchange);

        // Verificar se o log foi chamado
        verify(staticLogger).info(contains("Process finished successfully from class"),
                eq("com.santander.chk_int.processor.SampleProcessor"),
                any(),
                eq("12345"),
                eq(Map.of("key", "value")),
                eq("Body Content"),
                eq("param=value"),
                eq(Map.of("propertyKey", "propertyValue")));
    }

    @Test
    void testLogThrow() {
        // Mockando a chamada estática do logger
        Logger staticLogger = mock(Logger.class);
        LoggingAspectUtils.log = staticLogger;

        // Criar uma exceção simulada
        RuntimeException exception = new RuntimeException("Test exception");
        StackTraceElement stackTraceElement = new StackTraceElement("ClassName", "methodName", "FileName.java", 42);
        exception.setStackTrace(new StackTraceElement[]{stackTraceElement});

        // Executar o método
        loggingAspectUtils.logThrow(joinPoint, exchange, exception);

        // Verificar se o log foi chamado
        verify(staticLogger).error(contains("Process throw exception from class"),
                eq("com.santander.chk_int.processor.SampleProcessor"),
                any(),
                eq("12345"),
                eq(Map.of("key", "value")),
                eq("Body Content"),
                eq("param=value"),
                eq(Map.of("propertyKey", "propertyValue")),
                eq("java.lang.RuntimeException"),
                eq("Test exception"),
                eq(42));
    }
}
