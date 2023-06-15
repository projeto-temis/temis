import org.junit.Assert;
import org.junit.Test;

import br.jus.trf2.temis.core.util.Acentuacao;

public class AcentuacaoTest {

    @Test
    public void testAcentuarPalavras() {
        String input = "data de inscricao";
        String expectedOutput = "data de inscrição";
        String actualOutput = Acentuacao.acentuarPalavras(input);
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testAcentuarPalavras_CaseInsensitive() {
        String input = "APROVACOES DE CANDIDATOS";
        String expectedOutput = "APROVAÇÕES DE CANDIDATOS";
        String actualOutput = Acentuacao.acentuarPalavras(input);
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testAcentuarPalavras_NoAcento() {
        String input = "palavras sem acento";
        String expectedOutput = "palavras sem acento";
        String actualOutput = Acentuacao.acentuarPalavras(input);
        Assert.assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testAcentuarPalavras_SubstituicaoCompleta() {
        String input = "Codigo";
        String expectedOutput = "Código";
        String actualOutput = Acentuacao.acentuarPalavras(input);
        Assert.assertEquals(expectedOutput, actualOutput);
    }
}